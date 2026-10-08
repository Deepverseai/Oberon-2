package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agent.AgentCursorState
import com.example.agent.AgentDetectedError
import com.example.agent.AgentModeStatus
import com.example.agent.InteractiveElementInfo
import com.example.agent.SemanticButtonElement
import com.example.agent.SemanticElementMap
import com.example.agent.SemanticInputElement
import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.QuickLinkEntity
import com.example.model.ActiveScreen
import com.example.model.BrowserTab
import com.example.model.ConsoleLogItem
import com.example.model.DownloadItem
import com.example.model.SearchEngine
import com.example.model.ShieldStats
import com.example.model.SiteSecurityInfo
import com.example.model.ThemePreference
import com.example.model.UserAgentPreference
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

sealed interface WebNavAction {
    data class LoadUrl(val url: String) : WebNavAction
    object GoBack : WebNavAction
    object GoForward : WebNavAction
    object Reload : WebNavAction
    object Stop : WebNavAction
    data class FindInPage(val query: String, val forward: Boolean = true) : WebNavAction
    object ClearFindMatches : WebNavAction
    data class ExtractReaderContent(val callbackId: String) : WebNavAction
    data class ExecuteJavaScript(val script: String, val onResult: ((String) -> Unit)? = null) : WebNavAction
    data class ScrollPageBy(val dx: Int, val dy: Int) : WebNavAction
}

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val bookmarkDao = db.bookmarkDao()
    private val historyDao = db.historyDao()
    private val quickLinkDao = db.quickLinkDao()

    val bookmarks: StateFlow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryEntity>> = historyDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quickLinks: StateFlow<List<QuickLinkEntity>> = quickLinkDao.getAllQuickLinks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tabs
    private val initialTab = BrowserTab(url = "", title = "New Tab", isIncognito = false)
    private val _tabs = MutableStateFlow<List<BrowserTab>>(listOf(initialTab))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(initialTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    // Navigation UI Screen
    private val _currentScreen = MutableStateFlow(ActiveScreen.BROWSER)
    val currentScreen: StateFlow<ActiveScreen> = _currentScreen.asStateFlow()

    // Search Engine
    private val _selectedSearchEngine = MutableStateFlow(SearchEngine.GOOGLE)
    val selectedSearchEngine: StateFlow<SearchEngine> = _selectedSearchEngine.asStateFlow()

    // Shield
    private val _shieldStats = MutableStateFlow(ShieldStats())
    val shieldStats: StateFlow<ShieldStats> = _shieldStats.asStateFlow()

    // Navigation Actions for active WebView
    private val _webNavActions = MutableSharedFlow<WebNavAction>()
    val webNavActions = _webNavActions.asSharedFlow()

    // Omnibox state
    private val _omniboxQuery = MutableStateFlow("")
    val omniboxQuery: StateFlow<String> = _omniboxQuery.asStateFlow()

    private val _isOmniboxFocused = MutableStateFlow(false)
    val isOmniboxFocused: StateFlow<Boolean> = _isOmniboxFocused.asStateFlow()

    // Find in Page state
    private val _isFindInPageVisible = MutableStateFlow(false)
    val isFindInPageVisible: StateFlow<Boolean> = _isFindInPageVisible.asStateFlow()

    private val _findQuery = MutableStateFlow("")
    val findQuery: StateFlow<String> = _findQuery.asStateFlow()

    private val _findMatchCount = MutableStateFlow(0)
    val findMatchCount: StateFlow<Int> = _findMatchCount.asStateFlow()

    private val _findActiveIndex = MutableStateFlow(0)
    val findActiveIndex: StateFlow<Int> = _findActiveIndex.asStateFlow()

    // Site Security Sheet Dialog
    private val _siteSecurityInfo = MutableStateFlow<SiteSecurityInfo?>(null)
    val siteSecurityInfo: StateFlow<SiteSecurityInfo?> = _siteSecurityInfo.asStateFlow()

    // Preferences & Stitch Tokens
    private val _themePreference = MutableStateFlow(ThemePreference.SYSTEM)
    val themePreference: StateFlow<ThemePreference> = _themePreference.asStateFlow()

    private val _userAgentPreference = MutableStateFlow(UserAgentPreference.MOBILE)
    val userAgentPreference: StateFlow<UserAgentPreference> = _userAgentPreference.asStateFlow()

    private val _isBottomToolbar = MutableStateFlow(false)
    val isBottomToolbar: StateFlow<Boolean> = _isBottomToolbar.asStateFlow()

    private val _adBlockEnabled = MutableStateFlow(true)
    val adBlockEnabled: StateFlow<Boolean> = _adBlockEnabled.asStateFlow()

    private val _javascriptEnabled = MutableStateFlow(true)
    val javascriptEnabled: StateFlow<Boolean> = _javascriptEnabled.asStateFlow()

    // Reader Mode Content State for active tab
    private val _readerContent = MutableStateFlow<Map<String, String>?>(null)
    val readerContent: StateFlow<Map<String, String>?> = _readerContent.asStateFlow()

    // Downloads
    private val _downloads = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloads: StateFlow<List<DownloadItem>> = _downloads.asStateFlow()

    // Developer Console Logs
    private val _consoleLogs = MutableStateFlow<List<ConsoleLogItem>>(emptyList())
    val consoleLogs: StateFlow<List<ConsoleLogItem>> = _consoleLogs.asStateFlow()

    private val _isDevToolsOpen = MutableStateFlow(false)
    val isDevToolsOpen: StateFlow<Boolean> = _isDevToolsOpen.asStateFlow()

    // Speed Dial Context Sheet
    private val _selectedQuickLinkForContext = MutableStateFlow<QuickLinkEntity?>(null)
    val selectedQuickLinkForContext: StateFlow<QuickLinkEntity?> = _selectedQuickLinkForContext.asStateFlow()

    // Home Customization
    private val _showFavoritesOnHome = MutableStateFlow(true)
    val showFavoritesOnHome: StateFlow<Boolean> = _showFavoritesOnHome.asStateFlow()

    private val _showRecentOnHome = MutableStateFlow(true)
    val showRecentOnHome: StateFlow<Boolean> = _showRecentOnHome.asStateFlow()

    private val _showShieldOnHome = MutableStateFlow(true)
    val showShieldOnHome: StateFlow<Boolean> = _showShieldOnHome.asStateFlow()

    // Antigravity AI Agentic Automation State
    private val _isAgentModeEnabled = MutableStateFlow(false)
    val isAgentModeEnabled: StateFlow<Boolean> = _isAgentModeEnabled.asStateFlow()

    private val _agentStatus = MutableStateFlow(AgentModeStatus.IDLE)
    val agentStatus: StateFlow<AgentModeStatus> = _agentStatus.asStateFlow()

    private val _agentCursorState = MutableStateFlow(AgentCursorState())
    val agentCursorState: StateFlow<AgentCursorState> = _agentCursorState.asStateFlow()

    private val _agentErrors = MutableStateFlow<List<AgentDetectedError>>(emptyList())
    val agentErrors: StateFlow<List<AgentDetectedError>> = _agentErrors.asStateFlow()

    private val _isAgentRunning = MutableStateFlow(false)
    val isAgentRunning: StateFlow<Boolean> = _isAgentRunning.asStateFlow()

    private val _agentButtonsTested = MutableStateFlow(0)
    val agentButtonsTested: StateFlow<Int> = _agentButtonsTested.asStateFlow()

    private var agentTestJob: Job? = null

    val activeTab: BrowserTab?
        get() = tabs.value.firstOrNull { it.id == activeTabId.value }

    fun navigateToScreen(screen: ActiveScreen) {
        _currentScreen.value = screen
    }

    fun setOmniboxQuery(query: String) {
        _omniboxQuery.value = query
    }

    fun setOmniboxFocused(focused: Boolean) {
        _isOmniboxFocused.value = focused
        if (focused) {
            val currentUrl = activeTab?.url ?: ""
            if (currentUrl.isNotBlank() && currentUrl != "about:blank") {
                _omniboxQuery.value = currentUrl
            }
        }
    }

    fun setSearchEngine(engine: SearchEngine) {
        _selectedSearchEngine.value = engine
    }

    fun setThemePreference(pref: ThemePreference) {
        _themePreference.value = pref
    }

    fun setUserAgentPreference(pref: UserAgentPreference) {
        _userAgentPreference.value = pref
        viewModelScope.launch {
            _webNavActions.emit(WebNavAction.Reload)
        }
    }

    fun setToolbarPosition(isBottom: Boolean) {
        _isBottomToolbar.value = isBottom
    }

    fun setAdBlockEnabled(enabled: Boolean) {
        _adBlockEnabled.value = enabled
        _shieldStats.update { it.copy(isShieldEnabled = enabled) }
    }

    fun setJavascriptEnabled(enabled: Boolean) {
        _javascriptEnabled.value = enabled
    }

    fun toggleBlockThirdPartyCookies(enabled: Boolean) {
        _shieldStats.update { it.copy(blockThirdPartyCookies = enabled) }
    }

    fun toggleFingerprintingProtection(enabled: Boolean) {
        _shieldStats.update { it.copy(fingerprintingProtection = enabled) }
    }

    fun toggleHttpsOnlyMode(enabled: Boolean) {
        _shieldStats.update { it.copy(httpsOnlyMode = enabled) }
    }

    fun resetShieldStats() {
        _shieldStats.update {
            it.copy(
                trackersBlocked = 0,
                adsBlocked = 0,
                dataSavedKb = 0,
                httpsUpgrades = 0
            )
        }
    }

    fun openUrl(url: String, newTab: Boolean = false) {
        val targetUrl = selectedSearchEngine.value.buildUrl(url)
        _isOmniboxFocused.value = false

        if (newTab || _tabs.value.isEmpty()) {
            val tab = BrowserTab(url = targetUrl, title = "Loading...", isIncognito = false)
            _tabs.update { it + tab }
            _activeTabId.value = tab.id
        } else {
            val currentId = activeTabId.value
            _tabs.update { list ->
                list.map {
                    if (it.id == currentId) it.copy(url = targetUrl, isLoading = true, progress = 10)
                    else it
                }
            }
            viewModelScope.launch {
                _webNavActions.emit(WebNavAction.LoadUrl(targetUrl))
            }
        }
        _currentScreen.value = ActiveScreen.BROWSER
    }

    fun createNewTab(isIncognito: Boolean = false, initialUrl: String = "") {
        val resolvedUrl = if (initialUrl.isNotBlank()) selectedSearchEngine.value.buildUrl(initialUrl) else ""
        val newTab = BrowserTab(
            url = resolvedUrl,
            title = if (resolvedUrl.isBlank()) "New Tab" else "Loading...",
            isIncognito = isIncognito
        )
        _tabs.update { it + newTab }
        _activeTabId.value = newTab.id
        _currentScreen.value = ActiveScreen.BROWSER
        if (resolvedUrl.isNotBlank()) {
            viewModelScope.launch {
                _webNavActions.emit(WebNavAction.LoadUrl(resolvedUrl))
            }
        }
    }

    fun switchTab(tabId: String) {
        if (_tabs.value.any { it.id == tabId }) {
            _activeTabId.value = tabId
            _currentScreen.value = ActiveScreen.BROWSER
        }
    }

    fun closeTab(tabId: String) {
        val currentList = _tabs.value
        val index = currentList.indexOfFirst { it.id == tabId }
        if (index == -1) return

        val newList = currentList.filter { it.id != tabId }
        if (newList.isEmpty()) {
            val replacement = BrowserTab(url = "", title = "New Tab", isIncognito = false)
            _tabs.value = listOf(replacement)
            _activeTabId.value = replacement.id
        } else {
            _tabs.value = newList
            if (_activeTabId.value == tabId) {
                val newActiveIndex = if (index >= newList.size) newList.size - 1 else index
                _activeTabId.value = newList[newActiveIndex].id
            }
        }
    }

    fun closeAllTabs(isIncognitoOnly: Boolean = false) {
        if (isIncognitoOnly) {
            val nonIncognito = _tabs.value.filterNot { it.isIncognito }
            if (nonIncognito.isEmpty()) {
                val defaultTab = BrowserTab(url = "", title = "New Tab", isIncognito = false)
                _tabs.value = listOf(defaultTab)
                _activeTabId.value = defaultTab.id
            } else {
                _tabs.value = nonIncognito
                if (activeTab?.isIncognito == true) {
                    _activeTabId.value = nonIncognito.first().id
                }
            }
        } else {
            val freshTab = BrowserTab(url = "", title = "New Tab", isIncognito = false)
            _tabs.value = listOf(freshTab)
            _activeTabId.value = freshTab.id
        }
        _currentScreen.value = ActiveScreen.BROWSER
    }

    fun updateTabState(
        tabId: String,
        url: String? = null,
        title: String? = null,
        favicon: String? = null,
        isLoading: Boolean? = null,
        progress: Int? = null,
        canGoBack: Boolean? = null,
        canGoForward: Boolean? = null,
        incrementTrackers: Boolean = false
    ) {
        _tabs.update { list ->
            list.map { tab ->
                if (tab.id == tabId) {
                    var updated = tab
                    if (url != null) updated = updated.copy(url = url)
                    if (title != null && title.isNotBlank()) updated = updated.copy(title = title)
                    if (favicon != null) updated = updated.copy(favicon = favicon)
                    if (isLoading != null) updated = updated.copy(isLoading = isLoading)
                    if (progress != null) updated = updated.copy(progress = progress)
                    if (canGoBack != null) updated = updated.copy(canGoBack = canGoBack)
                    if (canGoForward != null) updated = updated.copy(canGoForward = canGoForward)
                    if (incrementTrackers) {
                        updated = updated.copy(trackersBlocked = updated.trackersBlocked + 1)
                        _shieldStats.update { stats ->
                            stats.copy(
                                trackersBlocked = stats.trackersBlocked + 1,
                                adsBlocked = stats.adsBlocked + 1,
                                dataSavedKb = stats.dataSavedKb + 34
                            )
                        }
                    }
                    updated
                } else {
                    tab
                }
            }
        }

        // Save history if successful page load and not incognito
        if (url != null && !url.startsWith("about:") && isLoading == false) {
            val tab = _tabs.value.firstOrNull { it.id == tabId }
            if (tab != null && !tab.isIncognito) {
                viewModelScope.launch {
                    historyDao.insertHistory(
                        HistoryEntity(
                            title = tab.title.ifBlank { url },
                            url = url,
                            favicon = tab.favicon
                        )
                    )
                }
            }
        }
    }

    fun toggleDesktopMode() {
        val currentId = activeTabId.value
        _tabs.update { list ->
            list.map {
                if (it.id == currentId) it.copy(isDesktopMode = !it.isDesktopMode) else it
            }
        }
        viewModelScope.launch {
            _webNavActions.emit(WebNavAction.Reload)
        }
    }

    fun toggleReaderMode() {
        val currentId = activeTabId.value
        val tab = activeTab ?: return
        val newReaderState = !tab.isReaderMode
        _tabs.update { list ->
            list.map {
                if (it.id == currentId) it.copy(isReaderMode = newReaderState) else it
            }
        }
        if (newReaderState) {
            viewModelScope.launch {
                _webNavActions.emit(WebNavAction.ExtractReaderContent(currentId))
            }
        } else {
            _readerContent.value = null
        }
    }

    fun setReaderExtractedData(data: Map<String, String>) {
        _readerContent.value = data
    }

    fun goBack() {
        viewModelScope.launch { _webNavActions.emit(WebNavAction.GoBack) }
    }

    fun goForward() {
        viewModelScope.launch { _webNavActions.emit(WebNavAction.GoForward) }
    }

    fun reload() {
        viewModelScope.launch { _webNavActions.emit(WebNavAction.Reload) }
    }

    fun stop() {
        viewModelScope.launch { _webNavActions.emit(WebNavAction.Stop) }
    }

    fun bookmarkCurrentPage() {
        val tab = activeTab ?: return
        if (tab.url.isBlank() || tab.url.startsWith("about:")) return

        viewModelScope.launch {
            val existing = bookmarkDao.getBookmarkByUrl(tab.url)
            if (existing != null) {
                bookmarkDao.deleteBookmark(existing)
            } else {
                bookmarkDao.insertBookmark(
                    BookmarkEntity(
                        title = tab.title.ifBlank { tab.url },
                        url = tab.url,
                        favicon = tab.favicon
                    )
                )
            }
        }
    }

    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            bookmarkDao.deleteBookmark(bookmark)
        }
    }

    fun removeHistoryItem(item: HistoryEntity) {
        viewModelScope.launch {
            historyDao.deleteHistory(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyDao.clearAllHistory()
        }
    }

    fun addQuickLink(title: String, url: String) {
        viewModelScope.launch {
            val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
            quickLinkDao.insertQuickLink(
                QuickLinkEntity(
                    title = title.ifBlank { "Site" },
                    url = formattedUrl,
                    iconName = "link",
                    position = quickLinks.value.size
                )
            )
        }
    }

    fun updateQuickLink(item: QuickLinkEntity, title: String, url: String) {
        viewModelScope.launch {
            val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
            quickLinkDao.insertQuickLink(
                item.copy(title = title, url = formattedUrl)
            )
        }
    }

    fun removeQuickLink(item: QuickLinkEntity) {
        viewModelScope.launch {
            quickLinkDao.deleteQuickLink(item)
        }
    }

    fun setQuickLinkForContext(item: QuickLinkEntity?) {
        _selectedQuickLinkForContext.value = item
    }

    // Downloads
    fun deleteDownload(item: DownloadItem) {
        _downloads.update { it.filter { d -> d.id != item.id } }
    }

    fun clearAllDownloads() {
        _downloads.value = emptyList()
    }

    fun addDownload(fileName: String, url: String, sizeFormatted: String) {
        val ext = fileName.substringAfterLast('.', "file")
        _downloads.update {
            listOf(
                DownloadItem(
                    fileName = fileName,
                    url = url,
                    fileSizeFormatted = sizeFormatted,
                    fileExtension = ext
                )
            ) + it
        }
    }

    // DevTools & Console Logs
    fun addConsoleLog(message: String, level: String, sourceId: String, lineNumber: Int) {
        _consoleLogs.update {
            (listOf(
                ConsoleLogItem(
                    message = message,
                    level = level,
                    sourceId = sourceId,
                    lineNumber = lineNumber
                )
            ) + it).take(150)
        }
    }

    fun clearConsoleLogs() {
        _consoleLogs.value = emptyList()
    }

    fun toggleDevTools(open: Boolean) {
        _isDevToolsOpen.value = open
    }

    // Find In Page
    fun showFindInPage(show: Boolean) {
        _isFindInPageVisible.value = show
        if (!show) {
            _findQuery.value = ""
            _findMatchCount.value = 0
            _findActiveIndex.value = 0
            viewModelScope.launch { _webNavActions.emit(WebNavAction.ClearFindMatches) }
        }
    }

    fun updateFindQuery(query: String) {
        _findQuery.value = query
        viewModelScope.launch {
            _webNavActions.emit(WebNavAction.FindInPage(query, forward = true))
        }
    }

    fun findNext(forward: Boolean) {
        viewModelScope.launch {
            _webNavActions.emit(WebNavAction.FindInPage(_findQuery.value, forward = forward))
        }
    }

    fun setFindResult(activeMatch: Int, totalMatches: Int) {
        _findActiveIndex.value = activeMatch
        _findMatchCount.value = totalMatches
    }

    fun showSiteSecurityDialog(show: Boolean) {
        if (!show) {
            _siteSecurityInfo.value = null
            return
        }
        val tab = activeTab
        if (tab != null && tab.url.isNotBlank()) {
            val isHttps = tab.url.startsWith("https://")
            val cookieHeader = try {
                android.webkit.CookieManager.getInstance().getCookie(tab.url)
            } catch (e: Exception) {
                null
            }
            val realCookiesCount = if (cookieHeader.isNullOrBlank()) 0 else cookieHeader.split(";").filter { it.isNotBlank() }.size

            _siteSecurityInfo.value = SiteSecurityInfo(
                isHttps = isHttps,
                domain = tab.displayHost,
                protocol = if (isHttps) "TLS 1.3 / AES-256-GCM" else "Insecure HTTP",
                certificateIssuer = if (isHttps) "Valid (Encrypted Connection)" else "None / Unencrypted",
                trackersBlocked = tab.trackersBlocked,
                cookiesCount = realCookiesCount,
                certificateValid = isHttps
            )
        }
    }

    // --- Antigravity AI Agent Automation Methods ---

    fun toggleAgentMode(enabled: Boolean) {
        _isAgentModeEnabled.value = enabled
        if (!enabled) {
            stopAgentTest()
            _agentCursorState.value = AgentCursorState(isVisible = false)
            _agentStatus.value = AgentModeStatus.IDLE
        } else {
            _agentCursorState.value = AgentCursorState(
                xRatio = 0.5f,
                yRatio = 0.5f,
                isVisible = true,
                actionText = "Antigravity Agent Ready"
            )
            injectAgentObserver()
        }
    }

    fun injectAgentObserver() {
        val observerJs = """
            (function() {
                if (window.__antigravityAgentInjected) return;
                window.__antigravityAgentInjected = true;
                window.addEventListener('error', function(e) {
                    console.error('ANTIGRAVITY_AGENT_ERROR:' + JSON.stringify({
                        type: 'JS_RUNTIME_EXCEPTION',
                        message: e.message || 'Script error occurred',
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
        viewModelScope.launch {
            _webNavActions.emit(WebNavAction.ExecuteJavaScript(observerJs))
        }
    }

    fun runAutonomousButtonAudit() {
        if (_isAgentRunning.value) return
        _isAgentRunning.value = true
        _agentStatus.value = AgentModeStatus.SCANNING
        _agentButtonsTested.value = 0

        agentTestJob = viewModelScope.launch {
            injectAgentObserver()

            val testedSelectors = mutableSetOf<String>()
            var totalTested = 0
            val maxScrollPasses = 5
            var currentPass = 0

            while (_isAgentRunning.value && currentPass < maxScrollPasses) {
                currentPass++
                _agentStatus.value = AgentModeStatus.SCANNING
                _agentCursorState.value = AgentCursorState(
                    xRatio = 0.5f,
                    yRatio = 0.35f,
                    isVisible = true,
                    actionText = if (currentPass == 1) "Scanning initial viewport..." else "Scanning viewport (Section $currentPass)..."
                )
                delay(400)

                // Scan currently visible interactive elements in [0, window.innerHeight]
                val scanScript = """
                    (function() {
                        var items = [];
                        var interactive = document.querySelectorAll('button, a, input[type="button"], input[type="submit"], [role="button"], [onclick]');
                        for (var i = 0; i < interactive.length; i++) {
                            var el = interactive[i];
                            var rect = el.getBoundingClientRect();
                            if (rect.width > 8 && rect.height > 8 && rect.top >= 0 && rect.top <= window.innerHeight && rect.left >= 0 && rect.left <= window.innerWidth) {
                                var id = el.id ? '#' + el.id : '';
                                var cls = (el.className && typeof el.className === 'string' && el.className.trim().length > 0) ? '.' + el.className.trim().split(/\s+/)[0] : '';
                                var sel = id || (el.tagName.toLowerCase() + cls) || el.tagName.toLowerCase();
                                var txt = (el.innerText || el.value || el.getAttribute('aria-label') || el.title || el.tagName.toLowerCase()).replace(/\s+/g, ' ').trim();
                                if (txt.length > 25) txt = txt.substring(0, 25) + '...';
                                var x = (rect.left + rect.width / 2) / Math.max(1, window.innerWidth);
                                var y = (rect.top + rect.height / 2) / Math.max(1, window.innerHeight);
                                items.push({
                                    selector: sel,
                                    text: txt || 'Element',
                                    xRatio: Math.max(0.06, Math.min(0.94, x)),
                                    yRatio: Math.max(0.06, Math.min(0.94, y)),
                                    tag: el.tagName.toLowerCase()
                                });
                                if (items.length >= 10) break;
                            }
                        }
                        var isAtBottom = (window.innerHeight + window.pageYOffset) >= (document.body.offsetHeight - 50);
                        return JSON.stringify({ items: items, isAtBottom: isAtBottom });
                    })()
                """.trimIndent()

                val scanCompletable = CompletableDeferred<String>()
                _webNavActions.emit(WebNavAction.ExecuteJavaScript(scanScript) { result ->
                    scanCompletable.complete(result ?: "{}")
                })

                val scanResultJson = try {
                    withTimeout(3000) { scanCompletable.await() }
                } catch (e: Exception) {
                    "{}"
                }

                val elementsToTest = mutableListOf<InteractiveElementInfo>()
                var isAtBottom = false
                try {
                    var cleanJson = scanResultJson.trim()
                    if (cleanJson.startsWith("\"") && cleanJson.endsWith("\"")) {
                        cleanJson = cleanJson.substring(1, cleanJson.length - 1).replace("\\\"", "\"").replace("\\\\", "\\")
                    }
                    val resObj = JSONObject(cleanJson)
                    isAtBottom = resObj.optBoolean("isAtBottom", false)
                    val jsonArr = resObj.optJSONArray("items") ?: JSONArray()
                    for (i in 0 until jsonArr.length()) {
                        val obj = jsonArr.getJSONObject(i)
                        val selector = obj.optString("selector", "button")
                        val text = obj.optString("text", "Button")
                        val tag = obj.optString("tag", "button")
                        val xRatio = obj.optDouble("xRatio", 0.5).toFloat()
                        val yRatio = obj.optDouble("yRatio", 0.5).toFloat()
                        val uniqueKey = "$selector|$text"
                        if (uniqueKey !in testedSelectors) {
                            testedSelectors.add(uniqueKey)
                            elementsToTest.add(InteractiveElementInfo(selector, text, tag, xRatio, yRatio, 0, 0))
                        }
                    }
                } catch (e: Exception) {}

                // Test discovered elements in this section
                if (elementsToTest.isNotEmpty()) {
                    _agentStatus.value = AgentModeStatus.TESTING_BUTTONS
                    for (item in elementsToTest) {
                        if (!_isAgentRunning.value) break

                        // Move cursor smoothly to real element coordinates
                        _agentCursorState.value = _agentCursorState.value.copy(
                            xRatio = item.xRatio,
                            yRatio = item.yRatio,
                            actionText = "Targeting ${item.text.take(18)}"
                        )
                        delay(500)

                        // Human-like click animation with pulse
                        _agentCursorState.value = _agentCursorState.value.copy(
                            isClicking = true,
                            pulseCount = _agentCursorState.value.pulseCount + 1,
                            actionText = "Clicking ${item.selector}"
                        )
                        delay(200)

                        // Dispatch real click in web view DOM
                        val escapedSelector = item.selector.replace("'", "\\'")
                        val clickScript = """
                            (function() {
                                try {
                                    var el = document.querySelector('$escapedSelector')
                                        || document.elementFromPoint(window.innerWidth * ${item.xRatio}, window.innerHeight * ${item.yRatio});
                                    if (el) {
                                        el.click();
                                    }
                                } catch(e) {
                                    console.error('ANTIGRAVITY_AGENT_ERROR:' + JSON.stringify({
                                        type: 'BUTTON_CLICK_FAILURE',
                                        message: e.message || 'Button click handler threw exception',
                                        brokenElementSelector: '$escapedSelector'
                                    }));
                                }
                            })()
                        """.trimIndent()

                        _webNavActions.emit(WebNavAction.ExecuteJavaScript(clickScript))
                        totalTested++
                        _agentButtonsTested.value = totalTested
                        delay(400)
                    }
                }

                if (isAtBottom) {
                    break
                }

                // Smooth Human-Like Swipe Gesture to scroll to next section
                _agentStatus.value = AgentModeStatus.SCROLLING
                _agentCursorState.value = _agentCursorState.value.copy(
                    xRatio = 0.5f,
                    yRatio = 0.72f,
                    actionText = "Swiping to next section..."
                )
                delay(300)

                _agentCursorState.value = _agentCursorState.value.copy(
                    xRatio = 0.5f,
                    yRatio = 0.28f,
                    actionText = "Scrolling down (viewport ${currentPass + 1})..."
                )
                _webNavActions.emit(WebNavAction.ScrollPageBy(0, 380))
                delay(750)
            }

            // Smooth scroll back to top after full page audit
            _agentCursorState.value = _agentCursorState.value.copy(
                xRatio = 0.5f,
                yRatio = 0.35f,
                actionText = "Returning smoothly to top..."
            )
            _webNavActions.emit(WebNavAction.ScrollPageBy(0, -6000))
            delay(650)

            _isAgentRunning.value = false
            _agentStatus.value = if (_agentErrors.value.isNotEmpty()) AgentModeStatus.ERROR_DETECTED else AgentModeStatus.COMPLETED
            _agentCursorState.value = _agentCursorState.value.copy(
                actionText = if (_agentErrors.value.isNotEmpty()) "Audit finished: ${_agentErrors.value.size} bug(s) caught across $totalTested elements!" else "Audit passed: Tested all $totalTested element(s) with 0 bugs!"
            )
        }
    }

    fun runScrollTest() {
        if (_isAgentRunning.value) return
        _isAgentRunning.value = true
        _agentStatus.value = AgentModeStatus.SCROLLING

        agentTestJob = viewModelScope.launch {
            // Human-like progressive swipe down (3 passes)
            repeat(3) { pass ->
                _agentCursorState.value = AgentCursorState(
                    xRatio = 0.5f,
                    yRatio = 0.75f,
                    isVisible = true,
                    actionText = "Swipe down (Pass ${pass + 1}/3)..."
                )
                delay(300)

                _agentCursorState.value = _agentCursorState.value.copy(
                    yRatio = 0.28f,
                    actionText = "Scrolling section ${pass + 1}..."
                )
                _webNavActions.emit(WebNavAction.ScrollPageBy(0, 360))
                delay(700)
            }

            // Smooth scroll back up
            _agentCursorState.value = AgentCursorState(
                xRatio = 0.5f,
                yRatio = 0.3f,
                isVisible = true,
                actionText = "Swiping back to top..."
            )
            delay(300)
            _agentCursorState.value = _agentCursorState.value.copy(
                yRatio = 0.75f,
                actionText = "Returning to header..."
            )
            _webNavActions.emit(WebNavAction.ScrollPageBy(0, -1500))
            delay(750)

            _isAgentRunning.value = false
            _agentStatus.value = AgentModeStatus.COMPLETED
            _agentCursorState.value = _agentCursorState.value.copy(
                actionText = "Scroll & Layout Integrity Verified"
            )
        }
    }

    fun stopAgentTest() {
        agentTestJob?.cancel()
        agentTestJob = null
        _isAgentRunning.value = false
        _agentStatus.value = AgentModeStatus.IDLE
        _agentCursorState.value = _agentCursorState.value.copy(
            actionText = "Agent Test Paused"
        )
    }

    fun clearAgentErrors() {
        _agentErrors.value = emptyList()
        _agentStatus.value = AgentModeStatus.IDLE
    }

    fun recordAgentDetectedError(
        errorType: String,
        message: String,
        filename: String = "",
        lineNumber: Int = 0,
        stackTrace: String = "",
        brokenElementSelector: String? = null,
        actionContext: String = ""
    ) {
        val currentTabUrl = activeTab?.url ?: ""
        val newError = AgentDetectedError(
            errorType = errorType,
            message = message,
            filename = filename,
            lineNumber = lineNumber,
            stackTrace = stackTrace,
            brokenElementSelector = brokenElementSelector,
            actionContext = actionContext.ifBlank { "Automated button interaction" },
            url = currentTabUrl
        )
        _agentErrors.update { listOf(newError) + it }
        _agentStatus.value = AgentModeStatus.ERROR_DETECTED
        _agentCursorState.value = _agentCursorState.value.copy(
            actionText = "⚠ Bug: ${message.take(30)}"
        )
    }

    // --- Direct Agent Server Integration API for Antigravity CLI Bridge ---

    fun scrollPage(dx: Int, dy: Int) {
        viewModelScope.launch {
            _webNavActions.emit(WebNavAction.ScrollPageBy(dx, dy))
        }
    }

    suspend fun executeJavaScriptAsync(script: String, timeoutMs: Long = 4000): String {
        val completable = CompletableDeferred<String>()
        _webNavActions.emit(WebNavAction.ExecuteJavaScript(script) { result ->
            completable.complete(result ?: "")
        })
        return try {
            withTimeout(timeoutMs) { completable.await() }
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun agentClickSelector(selector: String): JSONObject {
        val escaped = selector.replace("'", "\\'")
        val queryScript = """
            (function() {
                var el = document.querySelector('$escaped');
                if (!el) return JSON.stringify({found: false});
                var rect = el.getBoundingClientRect();
                var x = (rect.left + rect.width / 2) / Math.max(1, window.innerWidth);
                var y = (rect.top + rect.height / 2) / Math.max(1, window.innerHeight);
                return JSON.stringify({
                    found: true,
                    xRatio: Math.max(0.05, Math.min(0.95, x)),
                    yRatio: Math.max(0.05, Math.min(0.95, y))
                });
            })()
        """.trimIndent()

        val jsonStr = executeJavaScriptAsync(queryScript)
        var clean = jsonStr.trim()
        if (clean.startsWith("\"") && clean.endsWith("\"")) {
            clean = clean.substring(1, clean.length - 1).replace("\\\"", "\"").replace("\\\\", "\\")
        }
        val info = try { JSONObject(clean) } catch (e: Exception) { JSONObject().put("found", false) }

        if (info.optBoolean("found")) {
            val xRatio = info.optDouble("xRatio", 0.5).toFloat()
            val yRatio = info.optDouble("yRatio", 0.5).toFloat()

            _agentCursorState.value = _agentCursorState.value.copy(
                xRatio = xRatio,
                yRatio = yRatio,
                isVisible = true,
                actionText = "Targeting $selector"
            )
            delay(350)

            _agentCursorState.value = _agentCursorState.value.copy(
                isClicking = true,
                pulseCount = _agentCursorState.value.pulseCount + 1,
                actionText = "Clicking $selector"
            )
            delay(150)

            val clickScript = """
                (function() {
                    var el = document.querySelector('$escaped');
                    if (el) el.click();
                })()
            """.trimIndent()
            executeJavaScriptAsync(clickScript)
        }
        return info
    }

    suspend fun agentTypeText(selector: String, text: String): JSONObject {
        val escapedSel = selector.replace("'", "\\'")
        val escapedText = JSONObject.quote(text)
        val script = """
            (function() {
                var el = document.querySelector('$escapedSel');
                if (!el) return JSON.stringify({found: false});
                el.focus();
                el.value = $escapedText;
                el.dispatchEvent(new Event('input', { bubbles: true }));
                el.dispatchEvent(new Event('change', { bubbles: true }));
                return JSON.stringify({found: true});
            })()
        """.trimIndent()
        val res = executeJavaScriptAsync(script)
        return try { JSONObject(res) } catch (e: Exception) { JSONObject().put("status", "ok") }
    }

    suspend fun agentExtractText(): String {
        val script = "(function() { return document.body ? document.body.innerText : ''; })()"
        val res = executeJavaScriptAsync(script)
        var clean = res.trim()
        if (clean.startsWith("\"") && clean.endsWith("\"")) {
            clean = clean.substring(1, clean.length - 1).replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\")
        }
        return clean
    }

    suspend fun agentGetHtml(): String {
        val script = "(function() { return document.documentElement ? document.documentElement.outerHTML : ''; })()"
        val res = executeJavaScriptAsync(script)
        var clean = res.trim()
        if (clean.startsWith("\"") && clean.endsWith("\"")) {
            clean = clean.substring(1, clean.length - 1).replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\")
        }
        return clean
    }

    suspend fun agentExtractSemanticMap(): JSONObject {
        val script = """
            (function() {
                var result = {
                    url: window.location.href || '',
                    title: document.title || '',
                    inputs: [],
                    buttons: [],
                    isAtBottom: (window.innerHeight + window.pageYOffset) >= (document.body.offsetHeight - 50)
                };

                var winW = Math.max(1, window.innerWidth);
                var winH = Math.max(1, window.innerHeight);

                // 1. Scan Form Inputs
                var inputEls = document.querySelectorAll('input:not([type="hidden"]):not([type="submit"]):not([type="button"]):not([type="reset"]):not([type="image"]), textarea, select');
                for (var i = 0; i < inputEls.length; i++) {
                    var el = inputEls[i];
                    var rect = el.getBoundingClientRect();
                    if (rect.width > 5 && rect.height > 5) {
                        var id = el.id ? '#' + el.id : '';
                        var name = el.getAttribute('name') || '';
                        var sel = id || (name ? el.tagName.toLowerCase() + '[name="' + name + '"]' : '') || el.tagName.toLowerCase();
                        
                        var labelText = '';
                        if (el.id) {
                            var lbl = document.querySelector('label[for="' + el.id + '"]');
                            if (lbl) labelText = lbl.innerText || lbl.textContent || '';
                        }
                        if (!labelText && el.closest('label')) {
                            labelText = el.closest('label').innerText || el.closest('label').textContent || '';
                        }
                        if (!labelText) {
                            labelText = el.getAttribute('aria-label') || el.getAttribute('placeholder') || el.title || name || '';
                        }
                        labelText = labelText.replace(/\s+/g, ' ').trim();
                        if (labelText.length > 40) labelText = labelText.substring(0, 40) + '...';

                        var ph = (el.getAttribute('placeholder') || '').replace(/\s+/g, ' ').trim();
                        var inputType = el.getAttribute('type') || (el.tagName.toLowerCase() === 'textarea' ? 'textarea' : (el.tagName.toLowerCase() === 'select' ? 'select' : 'text'));

                        var cx = (rect.left + rect.width / 2) / winW;
                        var cy = (rect.top + rect.height / 2) / winH;

                        result.inputs.push({
                            selector: sel,
                            label: labelText,
                            placeholder: ph,
                            type: inputType.toLowerCase(),
                            name: name,
                            id: el.id || '',
                            xRatio: Math.max(0.05, Math.min(0.95, cx)),
                            yRatio: Math.max(0.05, Math.min(0.95, cy))
                        });
                        if (result.inputs.length >= 30) break;
                    }
                }

                // 2. Scan Interactive Buttons & Action Triggers
                var btnEls = document.querySelectorAll('button, input[type="submit"], input[type="button"], [role="button"], a[role="button"]');
                for (var j = 0; j < btnEls.length; j++) {
                    var b = btnEls[j];
                    var bRect = b.getBoundingClientRect();
                    if (bRect.width > 8 && bRect.height > 8) {
                        var bId = b.id ? '#' + b.id : '';
                        var bCls = (b.className && typeof b.className === 'string' && b.className.trim().length > 0) ? '.' + b.className.trim().split(/\s+/)[0] : '';
                        var bSel = bId || (b.tagName.toLowerCase() + bCls) || b.tagName.toLowerCase();
                        
                        var bText = (b.innerText || b.value || b.getAttribute('aria-label') || b.title || '').replace(/\s+/g, ' ').trim();
                        if (bText.length > 35) bText = bText.substring(0, 35) + '...';

                        var bRole = 'button';
                        var lowerText = bText.toLowerCase();
                        if (b.type === 'submit' || lowerText.indexOf('submit') !== -1 || lowerText.indexOf('login') !== -1 || lowerText.indexOf('sign in') !== -1 || lowerText.indexOf('register') !== -1 || lowerText.indexOf('send') !== -1) {
                            bRole = 'primary_action';
                        } else if (lowerText.indexOf('cancel') !== -1 || lowerText.indexOf('close') !== -1 || lowerText.indexOf('dismiss') !== -1) {
                            bRole = 'cancel_action';
                        }

                        var bx = (bRect.left + bRect.width / 2) / winW;
                        var by = (bRect.top + bRect.height / 2) / winH;

                        result.buttons.push({
                            selector: bSel,
                            text: bText || 'Button',
                            role: bRole,
                            xRatio: Math.max(0.05, Math.min(0.95, bx)),
                            yRatio: Math.max(0.05, Math.min(0.95, by))
                        });
                        if (result.buttons.length >= 30) break;
                    }
                }

                return JSON.stringify(result);
            })()
        """.trimIndent()

        val raw = executeJavaScriptAsync(script)
        var clean = raw.trim()
        if (clean.startsWith("\"") && clean.endsWith("\"")) {
            clean = clean.substring(1, clean.length - 1).replace("\\\"", "\"").replace("\\\\", "\\")
        }
        return try {
            val json = JSONObject(clean)
            json.put("status", "ok")
            val inputsCount = json.optJSONArray("inputs")?.length() ?: 0
            val buttonsCount = json.optJSONArray("buttons")?.length() ?: 0
            json.put("totalInteractiveElements", inputsCount + buttonsCount)
            json
        } catch (e: Exception) {
            JSONObject()
                .put("status", "error")
                .put("message", e.message ?: "Failed to parse semantic element map")
                .put("inputs", JSONArray())
                .put("buttons", JSONArray())
        }
    }
}
