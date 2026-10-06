package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.agent.AgentCursorState
import com.example.agent.AgentDetectedError
import com.example.agent.AgentModeStatus
import com.example.model.BrowserTab
import com.example.model.SearchEngine
import com.example.model.ShieldStats
import com.example.model.SiteSecurityInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Oberon Browser", appName)
    }

    @Test
    fun `search engine url resolution and localhost dev server handling`() {
        // Standard HTTPS URL
        val httpsUrl = SearchEngine.GOOGLE.buildUrl("https://example.com/test")
        assertEquals("https://example.com/test", httpsUrl)

        // Domain without protocol
        val domainUrl = SearchEngine.GOOGLE.buildUrl("github.com/test")
        assertEquals("https://github.com/test", domainUrl)

        // Localhost dev server for Antigravity Agent preview
        val localhostUrl = SearchEngine.GOOGLE.buildUrl("localhost:3000")
        assertEquals("http://localhost:3000", localhostUrl)

        val viteUrl = SearchEngine.GOOGLE.buildUrl("localhost:5173")
        assertEquals("http://localhost:5173", viteUrl)

        val ipDevUrl = SearchEngine.GOOGLE.buildUrl("127.0.0.1:8080")
        assertEquals("http://127.0.0.1:8080", ipDevUrl)

        val emulatorAliasUrl = SearchEngine.GOOGLE.buildUrl("10.0.2.2:3000")
        assertEquals("http://10.0.2.2:3000", emulatorAliasUrl)

        // Live HTML code data URI
        val dataUri = "data:text/html;charset=utf-8,%3Ch1%3ETest%3C%2Fh1%3E"
        val resolvedDataUri = SearchEngine.GOOGLE.buildUrl(dataUri)
        assertEquals(dataUri, resolvedDataUri)

        // Search Query
        val queryUrl = SearchEngine.GOOGLE.buildUrl("kotlin coroutines")
        assertTrue(queryUrl.startsWith("https://www.google.com/search?q="))
        assertTrue(queryUrl.contains("kotlin+coroutines") || queryUrl.contains("kotlin%20coroutines"))
    }

    @Test
    fun `antigravity agent bug report prompt generation`() {
        val error = AgentDetectedError(
            errorType = "JS_RUNTIME_EXCEPTION",
            message = "Uncaught ReferenceError: nonExistentComponent is not defined",
            filename = "app.js",
            lineNumber = 42,
            stackTrace = "at triggerBug (app.js:42:5)",
            brokenElementSelector = "button#btn-bug",
            actionContext = "Testing: Broken Button (button#btn-bug)",
            url = "http://localhost:3000"
        )

        val prompt = error.generateAgentFixPrompt()
        assertTrue(prompt.contains("[Antigravity Browser Agent Bug Report]"))
        assertTrue(prompt.contains("http://localhost:3000"))
        assertTrue(prompt.contains("button#btn-bug"))
        assertTrue(prompt.contains("app.js:42"))
        assertTrue(prompt.contains("Uncaught ReferenceError: nonExistentComponent is not defined"))
        assertTrue(prompt.contains("Task for Antigravity CLI / AI Coding Agent"))
    }

    @Test
    fun `browser tab state and host parsing`() {
        val blankTab = BrowserTab(url = "")
        assertTrue(blankTab.isBlank)
        assertEquals("New Tab", blankTab.displayTitle)

        val activeWebTab = BrowserTab(
            url = "https://aistudio.google.com/app",
            title = "Google AI Studio"
        )
        assertFalse(activeWebTab.isBlank)
        assertEquals("Google AI Studio", activeWebTab.displayTitle)
        assertEquals("aistudio.google.com", activeWebTab.displayHost)
    }

    @Test
    fun `shield stats start with authentic zero baselines`() {
        val stats = ShieldStats()
        assertEquals(0L, stats.trackersBlocked)
        assertEquals(0L, stats.adsBlocked)
        assertEquals(0L, stats.dataSavedKb)
        assertEquals(0L, stats.httpsUpgrades)
        assertTrue(stats.isShieldEnabled)
        assertTrue(stats.blockThirdPartyCookies)
    }

    @Test
    fun `site security info handles https and http accurately`() {
        val secureInfo = SiteSecurityInfo(
            isHttps = true,
            domain = "github.com",
            cookiesCount = 5,
            trackersBlocked = 2
        )
        assertTrue(secureInfo.isHttps)
        assertTrue(secureInfo.certificateValid)
        assertEquals("github.com", secureInfo.domain)
        assertEquals(5, secureInfo.cookiesCount)

        val insecureInfo = SiteSecurityInfo(
            isHttps = false,
            domain = "example.local",
            protocol = "Insecure HTTP",
            certificateIssuer = "None / Unencrypted",
            certificateValid = false
        )
        assertFalse(insecureInfo.isHttps)
        assertFalse(insecureInfo.certificateValid)
    }

    @Test
    fun `agent cursor state defaults and movement`() {
        val defaultState = AgentCursorState()
        assertFalse(defaultState.isVisible)
        assertFalse(defaultState.isClicking)
        assertEquals(0.5f, defaultState.xRatio)
        assertEquals(0.5f, defaultState.yRatio)

        val movingState = defaultState.copy(
            isVisible = true,
            xRatio = 0.25f,
            yRatio = 0.75f,
            actionText = "Clicking button#submit"
        )
        assertTrue(movingState.isVisible)
        assertEquals(0.25f, movingState.xRatio)
        assertEquals(0.75f, movingState.yRatio)
        assertEquals("Clicking button#submit", movingState.actionText)
    }
}
