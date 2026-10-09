package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import com.example.agent.AgentServer
import com.example.data.local.QuickLinkEntity
import com.example.model.ActiveScreen
import com.example.model.ThemePreference
import com.example.ui.components.AgentDiagnosticsSheet
import com.example.ui.components.ChromeMenuSheet
import com.example.ui.components.ChromeTopBar
import com.example.ui.components.FindInPageBar
import com.example.ui.components.LiveDevToolsDrawer
import com.example.ui.components.OmniboxSearchOverlay
import com.example.ui.components.SiteSecurityDialog
import com.example.ui.components.SpeedDialTileContextSheet
import com.example.ui.components.TabSwitcherSheet
import com.example.ui.screens.AppearanceTitaniumScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.DeveloperFlagsScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LocalPreviewSandboxScreen
import com.example.ui.screens.PrivacyShieldsScreen
import com.example.ui.screens.ReaderModeView
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WebScreen
import com.example.ui.theme.OberonBrowserTheme
import com.example.viewmodel.BrowserViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: BrowserViewModel
    private var agentServer: AgentServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = androidx.lifecycle.ViewModelProvider(this)[BrowserViewModel::class.java]

        // Handle initial incoming URL from Android intent
        intent?.dataString?.let { incomingUrl ->
            if (incomingUrl.isNotBlank()) {
                viewModel.openUrl(incomingUrl)
            }
        }

        // Start embedded AgentServer for Antigravity CLI automation on 127.0.0.1:8765
        try {
            agentServer = AgentServer(viewModel, lifecycleScope)
            agentServer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            val viewModel = this.viewModel
            val tabs by viewModel.tabs.collectAsStateWithLifecycle()
            val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
            val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
            val themePreference by viewModel.themePreference.collectAsStateWithLifecycle()
            val activeTab = viewModel.activeTab

            val isIncognito = activeTab?.isIncognito == true

            OberonBrowserTheme(isIncognito = isIncognito, themePreference = themePreference) {
                when (currentScreen) {
                    ActiveScreen.TAB_SWITCHER -> {
                        TabSwitcherSheet(
                            tabs = tabs,
                            activeTabId = activeTabId,
                            onSelectTab = { viewModel.switchTab(it) },
                            onCloseTab = { viewModel.closeTab(it) },
                            onNewTab = { isIncog -> viewModel.createNewTab(isIncognito = isIncog) },
                            onCloseAll = { isIncogOnly -> viewModel.closeAllTabs(isIncogOnly) },
                            onDismiss = { viewModel.navigateToScreen(ActiveScreen.BROWSER) }
                        )
                    }
                    ActiveScreen.BOOKMARKS -> {
                        val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
                        BookmarksScreen(
                            bookmarks = bookmarks,
                            onSelectBookmark = { url -> viewModel.openUrl(url) },
                            onDeleteBookmark = { item -> viewModel.removeBookmark(item) },
                            onBack = { viewModel.navigateToScreen(ActiveScreen.BROWSER) }
                        )
                    }
                    ActiveScreen.HISTORY -> {
                        val history by viewModel.history.collectAsStateWithLifecycle()
                        HistoryScreen(
                            history = history,
                            onSelectHistory = { url -> viewModel.openUrl(url) },
                            onDeleteHistoryItem = { item -> viewModel.removeHistoryItem(item) },
                            onClearAllHistory = { viewModel.clearAllHistory() },
                            onBack = { viewModel.navigateToScreen(ActiveScreen.BROWSER) }
                        )
                    }
                    ActiveScreen.DOWNLOADS -> {
                        val downloads by viewModel.downloads.collectAsStateWithLifecycle()
                        DownloadsScreen(
                            downloads = downloads,
                            onDeleteDownload = { item -> viewModel.deleteDownload(item) },
                            onClearAllDownloads = { viewModel.clearAllDownloads() },
                            onOpenFile = { item -> viewModel.openUrl(item.url) },
                            onBack = { viewModel.navigateToScreen(ActiveScreen.BROWSER) }
                        )
                    }
                    ActiveScreen.PRIVACY_SHIELDS -> {
                        val shieldStats by viewModel.shieldStats.collectAsStateWithLifecycle()
                        val adBlockEnabled by viewModel.adBlockEnabled.collectAsStateWithLifecycle()
                        val javascriptEnabled by viewModel.javascriptEnabled.collectAsStateWithLifecycle()

                        PrivacyShieldsScreen(
                            shieldStats = shieldStats,
                            adBlockEnabled = adBlockEnabled,
                            javascriptEnabled = javascriptEnabled,
                            onToggleMasterShield = { viewModel.setAdBlockEnabled(it) },
                            onToggleBlockThirdPartyCookies = { viewModel.toggleBlockThirdPartyCookies(it) },
                            onToggleFingerprinting = { viewModel.toggleFingerprintingProtection(it) },
                            onToggleHttpsOnly = { viewModel.toggleHttpsOnlyMode(it) },
                            onToggleJavascript = { viewModel.setJavascriptEnabled(it) },
                            onResetStats = { viewModel.resetShieldStats() },
                            onBack = { viewModel.navigateToScreen(ActiveScreen.BROWSER) }
                        )
                    }
                    ActiveScreen.APPEARANCE_SETTINGS -> {
                        val isBottomToolbar by viewModel.isBottomToolbar.collectAsStateWithLifecycle()
                        val userAgentPref by viewModel.userAgentPreference.collectAsStateWithLifecycle()
                        val showFavorites by viewModel.showFavoritesOnHome.collectAsStateWithLifecycle()
                        val showRecent by viewModel.showRecentOnHome.collectAsStateWithLifecycle()
                        val showShield by viewModel.showShieldOnHome.collectAsStateWithLifecycle()

                        AppearanceTitaniumScreen(
                            currentTheme = themePreference,
                            currentUserAgent = userAgentPref,
                            isBottomToolbar = isBottomToolbar,
                            showFavorites = showFavorites,
                            showRecent = showRecent,
                            showShield = showShield,
                            onSelectTheme = { viewModel.setThemePreference(it) },
                            onSelectUserAgent = { viewModel.setUserAgentPreference(it) },
                            onToggleToolbar = { viewModel.setToolbarPosition(it) },
                            onToggleShowFavorites = { /* toggled in VM */ },
                            onToggleShowRecent = { /* toggled in VM */ },
                            onToggleShowShield = { /* toggled in VM */ },
                            onBack = { viewModel.navigateToScreen(ActiveScreen.BROWSER) }
                        )
                    }
                    ActiveScreen.DEVELOPER_FLAGS -> {
                        val selectedEngine by viewModel.selectedSearchEngine.collectAsStateWithLifecycle()
                        DeveloperFlagsScreen(
                            currentEngine = selectedEngine,
                            onSelectEngine = { viewModel.setSearchEngine(it) },
                            onOpenDevConsole = {
                                viewModel.navigateToScreen(ActiveScreen.BROWSER)
                                viewModel.toggleDevTools(true)
                            },
                            onClearAllData = { viewModel.clearAllHistory() },
                            onBack = { viewModel.navigateToScreen(ActiveScreen.BROWSER) }
                        )
                    }
                    ActiveScreen.SETTINGS -> {
                        val selectedEngine by viewModel.selectedSearchEngine.collectAsStateWithLifecycle()
                        val isBottomToolbar by viewModel.isBottomToolbar.collectAsStateWithLifecycle()
                        val adBlockEnabled by viewModel.adBlockEnabled.collectAsStateWithLifecycle()
                        val javascriptEnabled by viewModel.javascriptEnabled.collectAsStateWithLifecycle()
                        val shieldStats by viewModel.shieldStats.collectAsStateWithLifecycle()

                        SettingsScreen(
                            currentEngine = selectedEngine,
                            isBottomToolbar = isBottomToolbar,
                            adBlockEnabled = adBlockEnabled,
                            javascriptEnabled = javascriptEnabled,
                            shieldStats = shieldStats,
                            onSelectEngine = { viewModel.setSearchEngine(it) },
                            onToggleToolbarPosition = { viewModel.setToolbarPosition(it) },
                            onToggleAdBlock = { viewModel.setAdBlockEnabled(it) },
                            onToggleJavascript = { viewModel.setJavascriptEnabled(it) },
                            onOpenPrivacyShields = { viewModel.navigateToScreen(ActiveScreen.PRIVACY_SHIELDS) },
                            onOpenAppearance = { viewModel.navigateToScreen(ActiveScreen.APPEARANCE_SETTINGS) },
                            onOpenDeveloperFlags = { viewModel.navigateToScreen(ActiveScreen.DEVELOPER_FLAGS) },
                            onClearAllData = { viewModel.clearAllHistory() },
                            onBack = { viewModel.navigateToScreen(ActiveScreen.BROWSER) }
                        )
                    }
                    ActiveScreen.APP_PREVIEW_SANDBOX -> {
                        LocalPreviewSandboxScreen(
                            onLaunchUrl = { url ->
                                viewModel.openUrl(url)
                                viewModel.toggleAgentMode(true)
                                viewModel.navigateToScreen(ActiveScreen.BROWSER)
                            },
                            onBack = { viewModel.navigateToScreen(ActiveScreen.BROWSER) }
                        )
                    }
                    ActiveScreen.BROWSER -> {
                        BrowserMainScreen(
                            viewModel = viewModel,
                            onShareUrl = { url ->
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, url)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Web Page")
                                startActivity(shareIntent)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.dataString?.let { incomingUrl ->
            if (incomingUrl.isNotBlank()) {
                viewModel.openUrl(incomingUrl)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            agentServer?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserMainScreen(
    viewModel: BrowserViewModel,
    onShareUrl: (String) -> Unit
) {
    val activeTab = viewModel.activeTab
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val isBottomToolbar by viewModel.isBottomToolbar.collectAsStateWithLifecycle()
    val isOmniboxFocused by viewModel.isOmniboxFocused.collectAsStateWithLifecycle()
    val omniboxQuery by viewModel.omniboxQuery.collectAsStateWithLifecycle()
    val selectedEngine by viewModel.selectedSearchEngine.collectAsStateWithLifecycle()
    val userAgentPreference by viewModel.userAgentPreference.collectAsStateWithLifecycle()
    val shieldStats by viewModel.shieldStats.collectAsStateWithLifecycle()
    val quickLinks by viewModel.quickLinks.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val securityInfo by viewModel.siteSecurityInfo.collectAsStateWithLifecycle()
    val isFindInPageVisible by viewModel.isFindInPageVisible.collectAsStateWithLifecycle()
    val findQuery by viewModel.findQuery.collectAsStateWithLifecycle()
    val findMatchCount by viewModel.findMatchCount.collectAsStateWithLifecycle()
    val findActiveIndex by viewModel.findActiveIndex.collectAsStateWithLifecycle()
    val readerExtractedContent by viewModel.readerContent.collectAsStateWithLifecycle()
    val adBlockEnabled by viewModel.adBlockEnabled.collectAsStateWithLifecycle()
    val javascriptEnabled by viewModel.javascriptEnabled.collectAsStateWithLifecycle()
    val isDevToolsOpen by viewModel.isDevToolsOpen.collectAsStateWithLifecycle()
    val consoleLogs by viewModel.consoleLogs.collectAsStateWithLifecycle()
    val selectedQuickLinkForContext by viewModel.selectedQuickLinkForContext.collectAsStateWithLifecycle()
    val showFavorites by viewModel.showFavoritesOnHome.collectAsStateWithLifecycle()
    val showRecent by viewModel.showRecentOnHome.collectAsStateWithLifecycle()
    val showShield by viewModel.showShieldOnHome.collectAsStateWithLifecycle()

    val isAgentModeEnabled by viewModel.isAgentModeEnabled.collectAsStateWithLifecycle()
    val agentStatus by viewModel.agentStatus.collectAsStateWithLifecycle()
    val agentCursorState by viewModel.agentCursorState.collectAsStateWithLifecycle()
    val agentErrors by viewModel.agentErrors.collectAsStateWithLifecycle()
    val isAgentRunning by viewModel.isAgentRunning.collectAsStateWithLifecycle()
    val agentButtonsTested by viewModel.agentButtonsTested.collectAsStateWithLifecycle()
    val activeEngine by viewModel.activeEngine.collectAsStateWithLifecycle()
    val activeGoal by viewModel.activeGoal.collectAsStateWithLifecycle()
    val isPreviewAppFrame by viewModel.isPreviewAppFrame.collectAsStateWithLifecycle()

    val isBookmarked = remember(activeTab?.url, bookmarks) {
        bookmarks.any { it.url == activeTab?.url }
    }

    var showMenuSheet by remember { mutableStateOf(false) }
    var showDiagnosticsSheet by remember { mutableStateOf(false) }
    var editingTile by remember { mutableStateOf<QuickLinkEntity?>(null) }
    var editTitleText by remember { mutableStateOf("") }
    var editUrlText by remember { mutableStateOf("") }
    val menuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val devToolsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val contextSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val diagnosticsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Handle system back button
    BackHandler {
        if (isFindInPageVisible) {
            viewModel.showFindInPage(false)
        } else if (isOmniboxFocused) {
            viewModel.setOmniboxFocused(false)
        } else if (activeTab?.isReaderMode == true) {
            viewModel.toggleReaderMode()
        } else if (activeTab != null && activeTab.canGoBack) {
            viewModel.goBack()
        } else if (activeTab != null && !activeTab.isBlank) {
            viewModel.openUrl("")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!isBottomToolbar && activeTab?.isReaderMode != true && !isOmniboxFocused && !isPreviewAppFrame) {
                ChromeTopBar(
                    tab = activeTab,
                    tabCount = tabs.size,
                    isIncognito = activeTab?.isIncognito == true,
                    onFocusOmnibox = { viewModel.setOmniboxFocused(true) },
                    onHome = { viewModel.openUrl("") },
                    onOpenTabs = { viewModel.navigateToScreen(ActiveScreen.TAB_SWITCHER) },
                    onOpenMenu = { showMenuSheet = true },
                    onOpenSecurityInfo = { viewModel.showSiteSecurityDialog(true) },
                    onToggleReaderMode = { viewModel.toggleReaderMode() },
                    modifier = Modifier.statusBarsPadding()
                )
            }
        },
        bottomBar = {
            if (isBottomToolbar && activeTab?.isReaderMode != true && !isOmniboxFocused) {
                ChromeTopBar(
                    tab = activeTab,
                    tabCount = tabs.size,
                    isIncognito = activeTab?.isIncognito == true,
                    onFocusOmnibox = { viewModel.setOmniboxFocused(true) },
                    onHome = { viewModel.openUrl("") },
                    onOpenTabs = { viewModel.navigateToScreen(ActiveScreen.TAB_SWITCHER) },
                    onOpenMenu = { showMenuSheet = true },
                    onOpenSecurityInfo = { viewModel.showSiteSecurityDialog(true) },
                    onToggleReaderMode = { viewModel.toggleReaderMode() },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main content: Reader Mode, Home Start Page, or WebScreen
            if (activeTab?.isReaderMode == true && activeTab != null) {
                ReaderModeView(
                    tab = activeTab,
                    extractedData = readerExtractedContent,
                    onCloseReader = { viewModel.toggleReaderMode() }
                )
            } else if (activeTab == null || activeTab.isBlank) {
                HomeScreen(
                    isIncognito = activeTab?.isIncognito == true,
                    shieldStats = shieldStats,
                    quickLinks = quickLinks,
                    recentHistory = history,
                    showFavorites = showFavorites,
                    showRecent = showRecent,
                    showShield = showShield,
                    onOpenUrl = { viewModel.openUrl(it) },
                    onFocusSearch = { viewModel.setOmniboxFocused(true) },
                    onAddQuickLink = { title, url -> viewModel.addQuickLink(title, url) },
                    onTileLongClick = { link -> viewModel.setQuickLinkForContext(link) },
                    onOpenPrivacyShields = { viewModel.navigateToScreen(ActiveScreen.PRIVACY_SHIELDS) },
                    onOpenDevTools = { viewModel.toggleDevTools(true) },
                    onOpenSettings = { viewModel.navigateToScreen(ActiveScreen.SETTINGS) },
                    onOpenSandbox = { viewModel.navigateToScreen(ActiveScreen.APP_PREVIEW_SANDBOX) }
                )
            } else {
                WebScreen(
                    tab = activeTab,
                    navActions = viewModel.webNavActions,
                    adBlockEnabled = adBlockEnabled,
                    javascriptEnabled = javascriptEnabled,
                    userAgentPreference = userAgentPreference,
                    isAgentModeEnabled = isAgentModeEnabled,
                    agentStatus = agentStatus,
                    agentCursorState = agentCursorState,
                    agentErrors = agentErrors,
                    isAgentRunning = isAgentRunning,
                    agentButtonsTested = agentButtonsTested,
                    activeEngine = activeEngine,
                    activeGoal = activeGoal,
                    isPreviewAppFrame = isPreviewAppFrame,
                    onSwitchEngine = { viewModel.setAgentEngine(it) },
                    onDispatchOperatorGoal = { viewModel.runAutonomousOperatorGoal(it) },
                    onTogglePreviewMode = { viewModel.setPreviewMode(it) },
                    onTabStateChange = { url, title, favicon, isLoading, progress, canGoBack, canGoForward, incrementTrackers ->
                        viewModel.updateTabState(
                            tabId = activeTab.id,
                            url = url,
                            title = title,
                            favicon = favicon,
                            isLoading = isLoading,
                            progress = progress,
                            canGoBack = canGoBack,
                            canGoForward = canGoForward,
                            incrementTrackers = incrementTrackers
                        )
                    },
                    onFindResult = { activeMatch, totalMatches ->
                        viewModel.setFindResult(activeMatch, totalMatches)
                    },
                    onReaderExtracted = { data ->
                        viewModel.setReaderExtractedData(data)
                    },
                    onConsoleLogged = { message, level, sourceId, lineNumber ->
                        viewModel.addConsoleLog(message, level, sourceId, lineNumber)
                    },
                    onAgentConsoleError = { jsonStr ->
                        try {
                            val json = JSONObject(jsonStr)
                            val type = json.optString("type", "RUNTIME_ERROR")
                            val message = json.optString("message", "Error caught")
                            val filename = json.optString("filename", "")
                            val lineno = json.optInt("lineno", 0)
                            val stack = json.optString("stack", "")
                            val selector = json.optString("brokenElementSelector", null)
                            viewModel.recordAgentDetectedError(
                                errorType = type,
                                message = message,
                                filename = filename,
                                lineNumber = lineno,
                                stackTrace = stack,
                                brokenElementSelector = selector
                            )
                        } catch (e: Exception) {
                            viewModel.recordAgentDetectedError(
                                errorType = "RUNTIME_EXCEPTION",
                                message = jsonStr
                            )
                        }
                    },
                    onRunButtonAudit = { viewModel.runAutonomousButtonAudit() },
                    onRunScrollTest = { viewModel.runScrollTest() },
                    onStopAgentTest = { viewModel.stopAgentTest() },
                    onOpenDiagnostics = { showDiagnosticsSheet = true },
                    onOpenSandbox = { viewModel.navigateToScreen(ActiveScreen.APP_PREVIEW_SANDBOX) },
                    onCloseAgentMode = { viewModel.toggleAgentMode(false) },
                    onDownloadRequested = { fileName, url, size -> viewModel.addDownload(fileName, url, size) }
                )
            }

            // In-page search bar overlay
            AnimatedVisibility(
                visible = isFindInPageVisible,
                enter = slideInVertically(),
                exit = slideOutVertically(),
                modifier = Modifier.fillMaxWidth()
            ) {
                FindInPageBar(
                    query = findQuery,
                    matchCount = findMatchCount,
                    activeMatchIndex = findActiveIndex,
                    onQueryChange = { viewModel.updateFindQuery(it) },
                    onNext = { viewModel.findNext(true) },
                    onPrevious = { viewModel.findNext(false) },
                    onClose = { viewModel.showFindInPage(false) }
                )
            }

            // Full-screen Chrome Omnibox Search Overlay
            if (isOmniboxFocused) {
                OmniboxSearchOverlay(
                    query = omniboxQuery,
                    selectedEngine = selectedEngine,
                    historyList = history,
                    bookmarksList = bookmarks,
                    onQueryChange = { viewModel.setOmniboxQuery(it) },
                    onSelectEngine = { viewModel.setSearchEngine(it) },
                    onSubmit = {
                        viewModel.openUrl(it)
                        viewModel.setOmniboxFocused(false)
                    },
                    onDismiss = {
                        viewModel.setOmniboxFocused(false)
                    }
                )
            }
        }
    }

    // Chrome-style 3-dot Dropdown / Modal Bottom Menu
    if (showMenuSheet) {
        ChromeMenuSheet(
            sheetState = menuSheetState,
            tab = activeTab,
            isBookmarked = isBookmarked,
            onDismiss = {
                scope.launch { menuSheetState.hide() }.invokeOnCompletion {
                    showMenuSheet = false
                }
            },
            onForward = { viewModel.goForward() },
            onToggleBookmark = { viewModel.bookmarkCurrentPage() },
            onOpenDownloads = { viewModel.navigateToScreen(ActiveScreen.DOWNLOADS) },
            onOpenPageInfo = { viewModel.showSiteSecurityDialog(true) },
            onReload = { viewModel.reload() },
            onNewTab = { viewModel.createNewTab(isIncognito = false) },
            onNewIncognitoTab = { viewModel.createNewTab(isIncognito = true) },
            onOpenHistory = { viewModel.navigateToScreen(ActiveScreen.HISTORY) },
            onOpenBookmarks = { viewModel.navigateToScreen(ActiveScreen.BOOKMARKS) },
            onOpenTabs = { viewModel.navigateToScreen(ActiveScreen.TAB_SWITCHER) },
            onShare = {
                activeTab?.let { if (it.url.isNotBlank()) onShareUrl(it.url) }
            },
            onFindInPage = { viewModel.showFindInPage(true) },
            onToggleDesktop = { viewModel.toggleDesktopMode() },
            onToggleReader = { viewModel.toggleReaderMode() },
            onOpenPrivacyShields = { viewModel.navigateToScreen(ActiveScreen.PRIVACY_SHIELDS) },
            onOpenAppearance = { viewModel.navigateToScreen(ActiveScreen.APPEARANCE_SETTINGS) },
            onOpenDevTools = { viewModel.toggleDevTools(true) },
            onOpenSettings = { viewModel.navigateToScreen(ActiveScreen.SETTINGS) },
            onClearData = { viewModel.clearAllHistory() },
            isAgentModeEnabled = isAgentModeEnabled,
            onToggleAgentMode = { viewModel.toggleAgentMode(!isAgentModeEnabled) },
            onOpenSandbox = { viewModel.navigateToScreen(ActiveScreen.APP_PREVIEW_SANDBOX) }
        )
    }

    // Antigravity Agent Diagnostics & Error Report Sheet
    if (showDiagnosticsSheet) {
        AgentDiagnosticsSheet(
            sheetState = diagnosticsSheetState,
            errors = agentErrors,
            buttonsTestedCount = agentButtonsTested,
            onDismiss = {
                scope.launch { diagnosticsSheetState.hide() }.invokeOnCompletion {
                    showDiagnosticsSheet = false
                }
            },
            onRerunTest = {
                scope.launch { diagnosticsSheetState.hide() }.invokeOnCompletion {
                    showDiagnosticsSheet = false
                    viewModel.clearAgentErrors()
                    viewModel.runAutonomousButtonAudit()
                }
            }
        )
    }

    // Live DevTools Console Drawer
    if (isDevToolsOpen) {
        LiveDevToolsDrawer(
            logs = consoleLogs,
            sheetState = devToolsSheetState,
            onClearLogs = { viewModel.clearConsoleLogs() },
            onDismiss = {
                scope.launch { devToolsSheetState.hide() }.invokeOnCompletion {
                    viewModel.toggleDevTools(false)
                }
            }
        )
    }

    // Speed Dial Tile Context Sheet
    selectedQuickLinkForContext?.let { tile ->
        SpeedDialTileContextSheet(
            item = tile,
            sheetState = contextSheetState,
            onDismiss = { viewModel.setQuickLinkForContext(null) },
            onOpenInNewTab = { url -> viewModel.createNewTab(isIncognito = false, initialUrl = url) },
            onOpenInPrivateTab = { url -> viewModel.createNewTab(isIncognito = true, initialUrl = url) },
            onEdit = { item ->
                editingTile = item
                editTitleText = item.title
                editUrlText = item.url
                viewModel.setQuickLinkForContext(null)
            },
            onRemove = { item -> viewModel.removeQuickLink(item) }
        )
    }

    // Edit Shortcut Dialog
    editingTile?.let { tile ->
        AlertDialog(
            onDismissRequest = { editingTile = null },
            title = { Text("Edit shortcut", fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTitleText,
                        onValueChange = { editTitleText = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editUrlText,
                        onValueChange = { editUrlText = it },
                        label = { Text("URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editUrlText.isNotBlank()) {
                            viewModel.updateQuickLink(tile, editTitleText.ifBlank { "Site" }, editUrlText)
                            editingTile = null
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTile = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Site Security Dialog
    securityInfo?.let { info ->
        SiteSecurityDialog(
            info = info,
            onDismiss = { viewModel.showSiteSecurityDialog(false) }
        )
    }
}
