package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrowserTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChromeMenuSheet(
    sheetState: SheetState,
    tab: BrowserTab?,
    isBookmarked: Boolean,
    onDismiss: () -> Unit,
    onForward: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenPageInfo: () -> Unit,
    onReload: () -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenTabs: () -> Unit,
    onShare: () -> Unit,
    onFindInPage: () -> Unit,
    onToggleDesktop: () -> Unit,
    onToggleReader: () -> Unit,
    onOpenPrivacyShields: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenDevTools: () -> Unit,
    onOpenSettings: () -> Unit,
    onClearData: () -> Unit,
    isAgentModeEnabled: Boolean = false,
    onToggleAgentMode: () -> Unit = {},
    onOpenSandbox: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        ) {
            // Chrome-style Top Icon Bar: [ -> ] [ ★ ] [ ↓ ] [ ⓘ ] [ ↻ ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        onDismiss()
                        onForward()
                    },
                    enabled = tab?.canGoForward == true
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (tab?.canGoForward == true) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onToggleBookmark
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = {
                        onDismiss()
                        onOpenDownloads()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Downloads",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = {
                        onDismiss()
                        onOpenPageInfo()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Page info",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = {
                        onDismiss()
                        onReload()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            )

            // Primary Navigation Items
            ChromeMenuItem(
                icon = Icons.Default.Add,
                title = "New tab",
                onClick = {
                    onDismiss()
                    onNewTab()
                }
            )

            ChromeMenuItem(
                icon = Icons.Default.VisibilityOff,
                title = "New incognito tab",
                onClick = {
                    onDismiss()
                    onNewIncognitoTab()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            )

            ChromeMenuItem(
                icon = Icons.Default.History,
                title = "History",
                onClick = {
                    onDismiss()
                    onOpenHistory()
                }
            )

            ChromeMenuItem(
                icon = Icons.Default.Download,
                title = "Downloads",
                onClick = {
                    onDismiss()
                    onOpenDownloads()
                }
            )

            ChromeMenuItem(
                icon = Icons.Default.Bookmark,
                title = "Bookmarks",
                onClick = {
                    onDismiss()
                    onOpenBookmarks()
                }
            )

            ChromeMenuItem(
                icon = Icons.Default.Tab,
                title = "Recent tabs",
                onClick = {
                    onDismiss()
                    onOpenTabs()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            )

            ChromeMenuItem(
                icon = Icons.Default.Share,
                title = "Share...",
                onClick = {
                    onDismiss()
                    onShare()
                }
            )

            ChromeMenuItem(
                icon = Icons.Default.FindInPage,
                title = "Find in page",
                onClick = {
                    onDismiss()
                    onFindInPage()
                }
            )

            // Desktop site with checkbox (Chrome standard)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleDesktop() }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Desktop site",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Checkbox(
                    checked = tab?.isDesktopMode == true,
                    onCheckedChange = { onToggleDesktop() }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            )

            ChromeMenuItem(
                icon = Icons.Default.Shield,
                title = "Privacy Shields",
                onClick = {
                    onDismiss()
                    onOpenPrivacyShields()
                }
            )

            ChromeMenuItem(
                icon = Icons.Default.Palette,
                title = "Appearance & Theme",
                onClick = {
                    onDismiss()
                    onOpenAppearance()
                }
            )

            ChromeMenuItem(
                icon = Icons.Default.SmartToy,
                title = if (isAgentModeEnabled) "Disable Antigravity Agent" else "Antigravity Agent Mode (AI Testing)",
                onClick = {
                    onDismiss()
                    onToggleAgentMode()
                }
            )

            ChromeMenuItem(
                icon = Icons.Default.Code,
                title = "App & Web Sandbox Preview",
                onClick = {
                    onDismiss()
                    onOpenSandbox()
                }
            )

            ChromeMenuItem(
                icon = Icons.Default.Terminal,
                title = "Developer Tools",
                onClick = {
                    onDismiss()
                    onOpenDevTools()
                }
            )

            ChromeMenuItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                onClick = {
                    onDismiss()
                    onOpenSettings()
                }
            )
        }
    }
}

@Composable
private fun ChromeMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
