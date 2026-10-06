package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrowserTab
import org.json.JSONObject

@Composable
fun ReaderModeView(
    tab: BrowserTab,
    extractedData: Map<String, String>?,
    onCloseReader: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fontSizeSp by remember { mutableFloatStateOf(17f) }
    var themeIndex by remember { mutableIntStateOf(0) } // 0 = Standard, 1 = Sepia, 2 = Pitch Black

    val bgColor = when (themeIndex) {
        1 -> Color(0xFFFBF0D9)
        2 -> Color(0xFF000000)
        else -> MaterialTheme.colorScheme.background
    }

    val textColor = when (themeIndex) {
        1 -> Color(0xFF382D1D)
        2 -> Color(0xFFE5E7EB)
        else -> MaterialTheme.colorScheme.onBackground
    }

    var parsedTitle = tab.title
    var parsedContent = "Extracting article contents... Please wait a moment."

    if (extractedData != null && extractedData.containsKey("extracted")) {
        try {
            val json = JSONObject(extractedData["extracted"] ?: "{}")
            val t = json.optString("title")
            val c = json.optString("content")
            if (t.isNotBlank()) parsedTitle = t
            if (c.isNotBlank()) parsedContent = c
        } catch (e: Exception) {
            // fallback
            parsedContent = extractedData["extracted"] ?: ""
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Reader toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reader View",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Font size controls
                IconButton(
                    onClick = { if (fontSizeSp > 13f) fontSizeSp -= 2f },
                    modifier = Modifier.size(36.dp)
                ) {
                    Text("A-", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                }

                IconButton(
                    onClick = { if (fontSizeSp < 25f) fontSizeSp += 2f },
                    modifier = Modifier.size(36.dp)
                ) {
                    Text("A+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Theme Switcher dots
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1E293B),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { themeIndex = 0 }
                ) {}
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFBF0D9),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { themeIndex = 1 }
                ) {}
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF000000),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { themeIndex = 2 }
                ) {}

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = onCloseReader,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Reader",
                        tint = textColor
                    )
                }
            }
        }

        HorizontalDivider(color = textColor.copy(alpha = 0.1f))

        // Reader Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            Text(
                text = parsedTitle,
                fontSize = (fontSizeSp + 7).sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                lineHeight = (fontSizeSp + 11).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tab.displayHost,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = parsedContent,
                fontSize = fontSizeSp.sp,
                color = textColor,
                lineHeight = (fontSizeSp * 1.55f).sp,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
