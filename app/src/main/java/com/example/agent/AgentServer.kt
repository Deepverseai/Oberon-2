package com.example.agent

import com.example.viewmodel.BrowserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Embedded Agent Server running directly within Oberon-2.
 * Listens strictly on localhost (127.0.0.1:8765) to receive automation,
 * testing, and audit commands from Antigravity CLI and autonomous AI agents.
 */
class AgentServer(
    private val viewModel: BrowserViewModel,
    private val scope: CoroutineScope,
    private val port: Int = 8765,
    private val requiredToken: String = "oberon_local_secret_token"
) {
    private var serverSocket: ServerSocket? = null
    private val isRunning = AtomicBoolean(false)
    private val executor = Executors.newCachedThreadPool()

    fun start() {
        if (isRunning.getAndSet(true)) return
        executor.execute {
            try {
                val loopback = InetAddress.getByName("127.0.0.1")
                serverSocket = ServerSocket(port, 50, loopback)
                while (isRunning.get()) {
                    val clientSocket = serverSocket?.accept() ?: break
                    executor.execute { handleClient(clientSocket) }
                }
            } catch (e: Exception) {
                // Server socket closed or interrupted
            }
        }
    }

    fun stop() {
        isRunning.set(false)
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        executor.shutdownNow()
    }

    private fun handleClient(socket: Socket) {
        try {
            socket.soTimeout = 9000
            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
            val output = socket.getOutputStream()

            val requestLine = reader.readLine() ?: return
            val parts = requestLine.split(" ")
            if (parts.size < 2) return
            val method = parts[0].uppercase()

            val headers = mutableMapOf<String, String>()
            var contentLength = 0
            while (true) {
                val line = reader.readLine() ?: break
                if (line.isEmpty()) break
                val colonIdx = line.indexOf(':')
                if (colonIdx > 0) {
                    val key = line.substring(0, colonIdx).trim().lowercase()
                    val value = line.substring(colonIdx + 1).trim()
                    headers[key] = value
                    if (key == "content-length") {
                        contentLength = value.toIntOrNull() ?: 0
                    }
                }
            }

            // Verify localhost origin
            val remoteAddress = socket.inetAddress.hostAddress
            if (remoteAddress != "127.0.0.1" && remoteAddress != "localhost") {
                sendResponse(output, 403, """{"status":"error","message":"Forbidden: Localhost only"}""")
                return
            }

            // Verify Security Token Header
            val providedToken = headers["x-agent-token"]
            if (providedToken != requiredToken) {
                sendResponse(output, 401, """{"status":"error","message":"Unauthorized: Invalid X-Agent-Token"}""")
                return
            }

            if (method == "GET") {
                val pingJson = JSONObject()
                    .put("status", "ok")
                    .put("service", "Oberon Agent Bridge")
                    .put("version", "2.0.0")
                    .put("engine", "Jetpack Compose Titanium")
                    .toString()
                sendResponse(output, 200, pingJson)
                return
            }

            if (method == "POST") {
                val bodyChars = CharArray(contentLength)
                var readTotal = 0
                while (readTotal < contentLength) {
                    val read = reader.read(bodyChars, readTotal, contentLength - readTotal)
                    if (read < 0) break
                    readTotal += read
                }
                val bodyStr = String(bodyChars, 0, readTotal)
                val responseJson = handleCommand(bodyStr)
                sendResponse(output, 200, responseJson)
                return
            }

            sendResponse(output, 405, """{"status":"error","message":"Method not allowed"}""")
        } catch (e: Exception) {
            try {
                sendResponse(socket.getOutputStream(), 500, """{"status":"error","message":"${e.message}"}""")
            } catch (_: Exception) {}
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun handleCommand(body: String): String {
        return try {
            val req = JSONObject(body)
            val action = req.optString("action", "")
            when (action) {
                "navigate" -> {
                    val url = req.optString("url", "")
                    scope.launch(Dispatchers.Main) {
                        viewModel.openUrl(url)
                    }
                    JSONObject().put("status", "ok").put("url", url).toString()
                }
                "new_tab" -> {
                    val url = req.optString("url", "")
                    scope.launch(Dispatchers.Main) {
                        viewModel.createNewTab(url)
                    }
                    JSONObject().put("status", "ok").put("url", url).toString()
                }
                "click" -> {
                    val selector = req.optString("selector", "")
                    val clickRes = runBlocking {
                        viewModel.agentClickSelector(selector)
                    }
                    JSONObject()
                        .put("status", "ok")
                        .put("result", clickRes.toString())
                        .toString()
                }
                "type" -> {
                    val selector = req.optString("selector", "")
                    val text = req.optString("text", "")
                    val typeRes = runBlocking {
                        viewModel.agentTypeText(selector, text)
                    }
                    JSONObject()
                        .put("status", "ok")
                        .put("result", typeRes.toString())
                        .toString()
                }
                "scroll" -> {
                    val dy = req.optInt("dy", 400)
                    scope.launch(Dispatchers.Main) {
                        viewModel.scrollPage(0, dy)
                    }
                    JSONObject().put("status", "ok").put("dy", dy).toString()
                }
                "extract_text" -> {
                    val text = runBlocking {
                        viewModel.agentExtractText()
                    }
                    JSONObject().put("status", "ok").put("text", text).toString()
                }
                "get_html" -> {
                    val html = runBlocking {
                        viewModel.agentGetHtml()
                    }
                    JSONObject().put("status", "ok").put("html", html).toString()
                }
                "set_preview_mode" -> {
                    val mode = req.optString("mode", "website")
                    scope.launch(Dispatchers.Main) {
                        viewModel.toggleAgentMode(mode.equals("app", ignoreCase = true))
                    }
                    JSONObject().put("status", "ok").put("mode", mode).toString()
                }
                "audit" -> {
                    scope.launch(Dispatchers.Main) {
                        viewModel.runAutonomousButtonAudit()
                    }
                    JSONObject().put("status", "ok").put("message", "Autonomous button audit initiated").toString()
                }
                "get_telemetry" -> {
                    val logs = JSONArray()
                    viewModel.consoleLogs.value.forEach { logs.put("[${it.level}] ${it.message}") }
                    val errors = JSONArray()
                    viewModel.agentErrors.value.forEach { errors.put("${it.errorType}: ${it.message}") }
                    JSONObject()
                        .put("status", "ok")
                        .put("logs", logs)
                        .put("errors", errors)
                        .toString()
                }
                else -> JSONObject().put("status", "error").put("message", "Unknown action: $action").toString()
            }
        } catch (e: Exception) {
            JSONObject().put("status", "error").put("message", e.message).toString()
        }
    }

    private fun sendResponse(output: OutputStream, statusCode: Int, body: String) {
        val bodyBytes = body.toByteArray(Charsets.UTF_8)
        val statusText = when (statusCode) {
            200 -> "OK"
            401 -> "Unauthorized"
            403 -> "Forbidden"
            405 -> "Method Not Allowed"
            else -> "Internal Server Error"
        }
        val header = "HTTP/1.1 $statusCode $statusText\r\n" +
                "Content-Type: application/json; charset=utf-8\r\n" +
                "Connection: close\r\n" +
                "Content-Length: ${bodyBytes.size}\r\n\r\n"
        output.write(header.toByteArray(Charsets.UTF_8))
        output.write(bodyBytes)
        output.flush()
    }
}
