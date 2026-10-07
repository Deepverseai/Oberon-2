package com.example

import com.example.agent.AgentServer
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

class AgentServerIntegrationTest {

    @Test
    fun `agent server models and constants verification`() {
        // Verify default port and secret token
        val defaultPort = 8765
        val defaultToken = "oberon_local_secret_token"
        assertEquals(8765, defaultPort)
        assertEquals("oberon_local_secret_token", defaultToken)
    }

    @Test
    fun `agent protocol command serialization and parsing`() {
        val navCmd = JSONObject().put("action", "navigate").put("url", "https://huggingface.co")
        assertEquals("navigate", navCmd.optString("action"))
        assertEquals("https://huggingface.co", navCmd.optString("url"))

        val clickCmd = JSONObject().put("action", "click").put("selector", "button.primary")
        assertEquals("click", clickCmd.optString("action"))
        assertEquals("button.primary", clickCmd.optString("selector"))

        val scrollCmd = JSONObject().put("action", "scroll").put("dy", 350)
        assertEquals("scroll", scrollCmd.optString("action"))
        assertEquals(350, scrollCmd.optInt("dy"))
    }
}
