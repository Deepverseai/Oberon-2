package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrowserTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OberonMenuSheet(
    sheetState: SheetState,
    tab: BrowserTab?,
    isBookmarked: Boolean,
    onDismiss: () -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit,
    onFindInPage: () -> Unit,
    onToggleDesktop: () -> Unit,
    onToggleReader: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenPrivacyShields: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenDevTools: () -> Unit,
    onOpenSettings: () -> Unit,
    onClearData: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // Quick Action Row at the top of the menu (New Tab, Incognito, Bookmark, Share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                QuickMenuPill(
                    icon = Icons.Default.Add,
                    label = "New Tab",
                    onClick = {
                        onDismiss()
                        onNewTab()
                    },
                    modifier = Modifier.testTag("menu_new_tab")
                )
                QuickMenuPill(
                    icon = Icons.Default.VisibilityOff,
                    label = "Incognito",
                    onClick = {
                        onDismiss()
                        onNewIncognitoTab()
                    },
                    modifier = Modifier.testTag("menu_new_incognito")
                )
                QuickMenuPill(
                    icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    label = if (isBookmarked) "Saved" else "Bookmark",
                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    onClick = {
                        onToggleBookmark()
                    },
                    modifier = Modifier.testTag("menu_bookmark")
                )
                QuickMenuPill(
                    icon = Icons.Default.Share,
                    label = "Share",
                    onClick = {
                        onDismiss()
                        onShare()
                    },
                    modifier = Modifier.testTag("menu_share")
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // Primary List Actions
            MenuItemRow(
                icon = Icons.Default.FindInPage,
                title = "Find in page",
                onClick = {
                    onDismiss()
                    onFindInPage()
                }
            )

            MenuItemRow(
                icon = Icons.Default.MenuBook,
                title = "Reader mode",
                subtitle = if (tab?.isReaderMode == true) "Active (Distraction-free)" else "Standard view",
                onClick = {
                    onDismiss()
                    onToggleReader()
                }
            )

            // Desktop Site switch row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleDesktop() }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Desktop site",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (tab?.isDesktopMode == true) "Desktop user-agent active" else "Mobile view",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = tab?.isDesktopMode == true,
                    onCheckedChange = { onToggleDesktop() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // Navigation destinations
            MenuItemRow(
                icon = Icons.Default.Bookmark,
                title = "Bookmarks",
                onClick = {
                    onDismiss()
                    onOpenBookmarks()
                }
            )

            MenuItemRow(
                icon = Icons.Default.History,
                title = "Browsing History",
                onClick = {
                    onDismiss()
                    onOpenHistory()
                }
            )

            MenuItemRow(
                icon = Icons.Default.Download,
                title = "Downloads",
                onClick = {
                    onDismiss()
                    onOpenDownloads()
                }
            )

            MenuItemRow(
                icon = Icons.Default.Shield,
                title = "Privacy Shields & Stats",
                onClick = {
                    onDismiss()
                    onOpenPrivacyShields()
                }
            )

            MenuItemRow(
                icon = Icons.Default.Palette,
                title = "Appearance & Customization",
                onClick = {
                    onDismiss()
                    onOpenAppearance()
                }
            )

            MenuItemRow(
                icon = Icons.Default.Terminal,
                title = "Developer Tools & Console",
                onClick = {
                    onDismiss()
                    onOpenDevTools()
                }
            )

            MenuItemRow(
                icon = Icons.Default.Delete,
                title = "Clear browsing data",
                onClick = {
                    onDismiss()
                    onClearData()
                }
            )

            MenuItemRow(
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
private fun QuickMenuPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
