package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.agent.AgentCursorState
import com.example.agent.AgentDetectedError
import com.example.agent.AgentEngineType
import com.example.agent.AgentModeStatus
import com.example.model.BrowserTab
import com.example.model.UserAgentPreference
import com.example.ui.components.AgentHudBar
import com.example.ui.components.AgentVirtualCursor
import com.example.viewmodel.WebNavAction
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import java.io.ByteArrayInputStream

private val AD_AND_TRACKER_DOMAINS = listOf(
    "doubleclick.net",
    "google-analytics.com",
    "googlesyndication.com",
    "facebook.net",
    "connect.facebook.net",
    "adservice.google",
    "scorecardresearch.com",
    "criteo.com",
    "hotjar.com",
    "taboola.com",
    "outbrain.com",
    "adnxs.com",
    "rubiconproject.com",
    "pubmatic.com",
    "casalemedia.com",
    "quantserve.com",
    "moatads.com"
)

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebScreen(
    tab: BrowserTab,
    navActions: SharedFlow<WebNavAction>,
    adBlockEnabled: Boolean,
    javascriptEnabled: Boolean,
    userAgentPreference: UserAgentPreference,
    isAgentModeEnabled: Boolean,
    agentStatus: AgentModeStatus,
    agentCursorState: AgentCursorState,
    agentErrors: List<AgentDetectedError>,
    isAgentRunning: Boolean,
    agentButtonsTested: Int,
    activeEngine: AgentEngineType = AgentEngineType.QA_AUDITOR,
    activeGoal: String = "",
    onSwitchEngine: (AgentEngineType) -> Unit = {},
    onDispatchOperatorGoal: (String) -> Unit = {},
    onTabStateChange: (
        url: String?,
        title: String?,
        favicon: String?,
        isLoading: Boolean?,
        progress: Int?,
        canGoBack: Boolean?,
        canGoForward: Boolean?,
        incrementTrackers: Boolean
    ) -> Unit,
    onFindResult: (activeMatch: Int, totalMatches: Int) -> Unit,
    onReaderExtracted: (Map<String, String>) -> Unit,
    onConsoleLogged: (message: String, level: String, sourceId: String, lineNumber: Int) -> Unit,
    onAgentConsoleError: (String) -> Unit,
    onRunButtonAudit: () -> Unit,
    onRunScrollTest: () -> Unit,
    onStopAgentTest: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenSandbox: () -> Unit,
    onCloseAgentMode: () -> Unit,
    onDownloadRequested: (fileName: String, url: String, sizeFormatted: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Listen to ViewModel actions for the current WebView
    LaunchedEffect(webViewInstance, tab.id) {
        val webView = webViewInstance ?: return@LaunchedEffect
        navActions.collectLatest { action ->
            when (action) {
                is WebNavAction.LoadUrl -> webView.loadUrl(action.url)
                is WebNavAction.GoBack -> if (webView.canGoBack()) webView.goBack()
                is WebNavAction.GoForward -> if (webView.canGoForward()) webView.goForward()
                is WebNavAction.Reload -> webView.reload()
                is WebNavAction.Stop -> webView.stopLoading()
                is WebNavAction.FindInPage -> {
                    webView.findAllAsync(action.query)
                }
                is WebNavAction.ClearFindMatches -> {
                    webView.clearMatches()
                }
                is WebNavAction.ExecuteJavaScript -> {
                    webView.evaluateJavascript(action.script) { result ->
                        action.onResult?.invoke(result ?: "")
                    }
                }
                is WebNavAction.ScrollPageBy -> {
                    val scrollScript = "try { window.scrollBy({ left: ${action.dx}, top: ${action.dy}, behavior: 'smooth' }); } catch(e) { window.scrollBy(${action.dx}, ${action.dy}); }"
                    webView.evaluateJavascript(scrollScript, null)
                }
                is WebNavAction.ExtractReaderContent -> {
                    val extractJs = """
                        (function() {
                            var title = document.title || '';
                            var article = document.querySelector('article') || document.querySelector('main') || document.body;
                            var paragraphs = article.querySelectorAll('p, h1, h2, h3, h4, blockquote');
                            var content = '';
                            for (var i = 0; i < paragraphs.length; i++) {
                                var text = paragraphs[i].innerText.trim();
                                if (text.length > 20) {
                                    content += text + '\n\n';
                                }
                            }
                            return JSON.stringify({ title: title, content: content });
                        })()
                    """.trimIndent()
                    webView.evaluateJavascript(extractJs) { result ->
                        if (result != null && result != "null") {
                            try {
                                val cleanJson = if (result.startsWith("\"") && result.endsWith("\"")) {
                                    result.substring(1, result.length - 1).replace("\\\"", "\"").replace("\\n", "\n")
                                } else {
                                    result
                                }
                                onReaderExtracted(mapOf("extracted" to cleanJson))
                            } catch (e: Exception) {
                                // ignore
                            }
                        }
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewInstance = this

                    settings.apply {
                        javaScriptEnabled = javascriptEnabled
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        cacheMode = WebSettings.LOAD_DEFAULT
                        databaseEnabled = true
                        setSupportZoom(true)
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = if (tab.isDesktopMode) DESKTOP_USER_AGENT else userAgentPreference.uaString
                    }

                    setFindListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->
                        onFindResult(activeMatchOrdinal, numberOfMatches)
                    }

                    setDownloadListener { downloadUrl, _, contentDisposition, mimetype, contentLength ->
                        val guessedFileName = android.webkit.URLUtil.guessFileName(downloadUrl, contentDisposition, mimetype)
                        val sizeFormatted = if (contentLength > 0) {
                            String.format(java.util.Locale.US, "%.1f MB", contentLength / (1024f * 1024f))
                        } else {
                            "Unknown size"
                        }
                        onDownloadRequested(guessedFileName, downloadUrl, sizeFormatted)
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            if (consoleMessage != null) {
                                val msg = consoleMessage.message() ?: ""
                                onConsoleLogged(
                                    msg,
                                    consoleMessage.messageLevel()?.name ?: "LOG",
                                    consoleMessage.sourceId() ?: "",
                                    consoleMessage.lineNumber()
                                )

                                if (msg.startsWith("ANTIGRAVITY_AGENT_ERROR:")) {
                                    val json = msg.removePrefix("ANTIGRAVITY_AGENT_ERROR:")
                                    onAgentConsoleError(json)
                                } else if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR && isAgentModeEnabled) {
                                    onAgentConsoleError(
                                        """{"type":"CONSOLE_ERROR","message":"${msg.replace("\"", "\\\"")}","filename":"${consoleMessage.sourceId()}","lineno":${consoleMessage.lineNumber()}}"""
                                    )
                                }
                            }
                            return super.onConsoleMessage(consoleMessage)
                        }

                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            onTabStateChange(
                                null, null, null,
                                newProgress < 100,
                                newProgress,
                                view?.canGoBack(),
                                view?.canGoForward(),
                                false
                            )
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            super.onReceivedTitle(view, title)
                            if (!title.isNullOrBlank()) {
                                onTabStateChange(
                                    null, title, null, null, null,
                                    view?.canGoBack(), view?.canGoForward(), false
                                )
                            }
                        }

                        override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                            super.onReceivedIcon(view, icon)
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            if (adBlockEnabled && request != null) {
                                val reqUrl = request.url.toString()
                                val host = request.url.host ?: ""
                                val isTracker = AD_AND_TRACKER_DOMAINS.any { domain ->
                                    host.contains(domain, ignoreCase = true)
                                }
                                if (isTracker) {
                                    onTabStateChange(
                                        null, null, null, null, null, null, null, true
                                    )
                                    return WebResourceResponse(
                                        "text/plain",
                                        "UTF-8",
                                        ByteArrayInputStream(ByteArray(0))
                                    )
                                }
                            }
                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            onTabStateChange(
                                url, null, null, true, 15,
                                view?.canGoBack(), view?.canGoForward(), false
                            )
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            onTabStateChange(
                                url, view?.title, null, false, 100,
                                view?.canGoBack(), view?.canGoForward(), false
                            )

                            // Automatically inject Agent error-catching observer if Agent Mode is active
                            if (isAgentModeEnabled) {
                                val observerJs = """
                                    (function() {
                                        if (window.__antigravityAgentInjected) return;
                                        window.__antigravityAgentInjected = true;
                                        window.addEventListener('error', function(e) {
                                            console.error('ANTIGRAVITY_AGENT_ERROR:' + JSON.stringify({
                                                type: 'JS_RUNTIME_EXCEPTION',
                                                message: e.message || 'Script error',
                                                filename: e.filename || 'inline',
                                                lineno: e.lineno || 0,
                                                stack: e.error ? e.error.stack : ''
                                            }));
                                        });
                                        window.addEventListener('unhandledrejection', function(e) {
                                            var reason = e.reason ? (e.reason.message || String(e.reason)) : 'Unhandled Promise Rejection';
                                            console.error('ANTIGRAVITY_AGENT_ERROR:' + JSON.stringify({
                                                type: 'UNHANDLED_PROMISE_REJECTION',
                                                message: reason,
                                                filename: 'promise',
                                                lineno: 0,
                                                stack: e.reason && e.reason.stack ? e.reason.stack : ''
                                            }));
                                        });
                                    })();
                                """.trimIndent()
                                view?.evaluateJavascript(observerJs, null)
                            }
                        }
                    }

                    if (tab.url.isNotBlank() && tab.url != "about:blank") {
                        loadUrl(tab.url)
                    }
                }
            },
            update = { webView ->
                webViewInstance = webView
                webView.settings.userAgentString =
                    if (tab.isDesktopMode) DESKTOP_USER_AGENT else userAgentPreference.uaString

                // If tab URL changed externally
                if (tab.url.isNotBlank() && tab.url != "about:blank" && webView.url != tab.url) {
                    webView.loadUrl(tab.url)
                }
            }
        )

        // Smooth Progress Indicator
        AnimatedVisibility(
            visible = tab.isLoading && tab.progress < 100,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxWidth()
        ) {
            LinearProgressIndicator(
                progress = { tab.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        }


        // --- Antigravity AI Agent Visual Virtual Cursor & HUD ---
        if (isAgentModeEnabled) {
            AgentVirtualCursor(
                cursorState = agentCursorState,
                modifier = Modifier.fillMaxSize()
            )

            AgentHudBar(
                status = agentStatus,
                isRunning = isAgentRunning,
                errorCount = agentErrors.size,
                activeEngine = activeEngine,
                activeGoal = activeGoal,
                onSwitchEngine = onSwitchEngine,
                onDispatchOperatorGoal = onDispatchOperatorGoal,
                onRunButtonAudit = onRunButtonAudit,
                onRunScrollTest = onRunScrollTest,
                onStopTest = onStopAgentTest,
                onOpenDiagnostics = onOpenDiagnostics,
                onOpenSandbox = onOpenSandbox,
                onCloseAgentMode = onCloseAgentMode,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    DisposableEffect(tab.id) {
        onDispose {
            webViewInstance?.stopLoading()
        }
    }
}
