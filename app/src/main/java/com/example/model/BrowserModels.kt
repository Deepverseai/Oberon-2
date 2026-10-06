package com.example.model

import java.util.UUID

enum class SearchEngine(
    val displayName: String,
    val queryUrl: String,
    val homeUrl: String,
    val shortName: String
) {
    GOOGLE(
        displayName = "Google",
        queryUrl = "https://www.google.com/search?q=",
        homeUrl = "https://www.google.com",
        shortName = "G"
    ),
    DUCKDUCKGO(
        displayName = "DuckDuckGo",
        queryUrl = "https://duckduckgo.com/?q=",
        homeUrl = "https://duckduckgo.com",
        shortName = "DDG"
    ),
    BRAVE(
        displayName = "Brave Search",
        queryUrl = "https://search.brave.com/search?q=",
        homeUrl = "https://search.brave.com",
        shortName = "Brave"
    ),
    BING(
        displayName = "Microsoft Bing",
        queryUrl = "https://www.bing.com/search?q=",
        homeUrl = "https://www.bing.com",
        shortName = "Bing"
    ),
    ECOSIA(
        displayName = "Ecosia",
        queryUrl = "https://www.ecosia.org/search?q=",
        homeUrl = "https://www.ecosia.org",
        shortName = "Eco"
    );

    fun buildUrl(query: String): String {
        val trimmed = query.trim()
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("data:") || trimmed.startsWith("about:") || trimmed.startsWith("file://")) {
            trimmed
        } else if (trimmed.startsWith("localhost:") || trimmed == "localhost" || trimmed.startsWith("127.0.0.1") || trimmed.startsWith("10.0.2.2")) {
            "http://$trimmed"
        } else if (trimmed.contains(".") && !trimmed.contains(" ") && !trimmed.startsWith("?")) {
            "https://$trimmed"
        } else {
            queryUrl + java.net.URLEncoder.encode(trimmed, "UTF-8")
        }
    }
}

enum class ThemePreference(val displayName: String) {
    SYSTEM("System Default"),
    ALABASTER_LIGHT("Alabaster Light"),
    TITANIUM_DARK("Titanium Obsidian"),
    AMOLED_BLACK("AMOLED Pure Black")
}

enum class UserAgentPreference(val displayName: String, val uaString: String?) {
    MOBILE("Mobile Android (Default)", null),
    DESKTOP("Desktop Chrome (Linux)", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"),
    MACOS("Desktop Safari (macOS)", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Safari/605.1.15"),
    TABLET("Tablet iPad", "Mozilla/5.0 (iPad; CPU OS 17_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Mobile/15E148 Safari/604.1")
}

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "",
    val title: String = "New Tab",
    val favicon: String? = null,
    val isIncognito: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val isDesktopMode: Boolean = false,
    val isReaderMode: Boolean = false,
    val trackersBlocked: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isBlank: Boolean
        get() = url.isBlank() || url == "about:blank"

    val displayTitle: String
        get() = if (title.isNotBlank()) title else if (url.isNotBlank()) url else "New Tab"

    val displayHost: String
        get() {
            return try {
                val uri = android.net.Uri.parse(url)
                uri.host ?: url
            } catch (e: Exception) {
                url
            }
        }
}

data class SiteSecurityInfo(
    val isHttps: Boolean = true,
    val domain: String = "",
    val protocol: String = "TLS 1.3 / AES-256-GCM",
    val certificateIssuer: String = "Valid (Secure Connection)",
    val trackersBlocked: Int = 0,
    val cookiesCount: Int = 0,
    val certificateValid: Boolean = true
)

data class ShieldStats(
    val trackersBlocked: Long = 0,
    val adsBlocked: Long = 0,
    val dataSavedKb: Long = 0,
    val httpsUpgrades: Long = 0,
    val isShieldEnabled: Boolean = true,
    val blockThirdPartyCookies: Boolean = true,
    val fingerprintingProtection: Boolean = true,
    val httpsOnlyMode: Boolean = true
)

data class DownloadItem(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val url: String,
    val fileSizeFormatted: String,
    val progress: Int = 100,
    val isCompleted: Boolean = true,
    val downloadedAt: Long = System.currentTimeMillis(),
    val fileExtension: String = "pdf"
)

data class ConsoleLogItem(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val level: String = "LOG",
    val sourceId: String = "",
    val lineNumber: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ActiveScreen {
    BROWSER,
    TAB_SWITCHER,
    BOOKMARKS,
    HISTORY,
    DOWNLOADS,
    SETTINGS,
    PRIVACY_SHIELDS,
    APPEARANCE_SETTINGS,
    DEVELOPER_FLAGS,
    APP_PREVIEW_SANDBOX
}
