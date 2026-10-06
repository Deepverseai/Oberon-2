package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChromeBlue
import java.net.URLEncoder

private val SAMPLE_DEMO_CODE = """
<!DOCTYPE html>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
    body { font-family: -apple-system, system-ui, sans-serif; padding: 24px; background: #f8fafc; color: #0f172a; margin: 0; }
    .card { background: white; border-radius: 16px; padding: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); margin-bottom: 16px; }
    h2 { margin-top: 0; font-size: 20px; color: #1e293b; }
    p { color: #64748b; font-size: 14px; line-height: 1.5; }
    .btn { display: inline-block; padding: 10px 18px; border-radius: 10px; font-weight: 600; font-size: 14px; border: none; cursor: pointer; margin-right: 8px; margin-bottom: 8px; transition: 0.2s; }
    .btn-primary { background: #2563eb; color: white; }
    .btn-danger { background: #ef4444; color: white; }
    .btn-secondary { background: #e2e8f0; color: #334155; }
    #status { padding: 12px; background: #ecfdf5; border-radius: 8px; color: #065f46; font-size: 13px; font-weight: 500; display: none; margin-top: 10px; }
  </style>
</head>
<body>
  <div class="card">
    <h2>Antigravity Live App Sandbox</h2>
    <p>This web app preview is connected to Oberon Browser's AI Agentic Testing workflow.</p>
    <button class="btn btn-primary" id="btn-counter" onclick="increment()">Click Counter (<span id="count">0</span>)</button>
    <button class="btn btn-secondary" id="btn-toggle" onclick="toggleMsg()">Toggle Status</button>
    <button class="btn btn-danger" id="btn-bug" onclick="triggerBug()">Intentionally Broken Button</button>
    <div id="status">Status: All components operational!</div>
  </div>

  <script>
    let count = 0;
    function increment() {
      count++;
      document.getElementById('count').innerText = count;
    }
    function toggleMsg() {
      const s = document.getElementById('status');
      s.style.display = s.style.display === 'none' ? 'block' : 'none';
    }
    function triggerBug() {
      // Simulates broken logic for agent to detect
      nonExistentComponent.processPayment();
    }
  </script>
</body>
</html>
""".trimIndent()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalPreviewSandboxScreen(
    onLaunchUrl: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var targetUrl by remember { mutableStateOf("http://localhost:3000") }
    var htmlCode by remember { mutableStateOf(SAMPLE_DEMO_CODE) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Dev Server URLs, 1 = Code Sandbox

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = ChromeBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agent App & Web Preview", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // Segmented Toggle: Local Server vs Code Sandbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selectedTab == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { selectedTab = 0 }
            ) {
                Text(
                    text = "Dev Servers (Localhost)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selectedTab == 1) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { selectedTab = 1 }
            ) {
                Text(
                    text = "Live Code Sandbox",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedTab == 0) {
                // Dev Server URL Connect
                item {
                    Text(
                        text = "Connect to Antigravity / Local Dev Server",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Run web or app servers from your CLI agent (React, Vite, Next.js, Node) and test them directly in Oberon Browser.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    OutlinedTextField(
                        value = targetUrl,
                        onValueChange = { targetUrl = it },
                        label = { Text("Server URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                }

                item {
                    Button(
                        onClick = {
                            if (targetUrl.isNotBlank()) {
                                onLaunchUrl(targetUrl)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ChromeBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Preview & Enable Agent Mode", fontWeight = FontWeight.SemiBold)
                    }
                }

                item {
                    Text(
                        text = "Quick Presets",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val presets = listOf(
                    "http://localhost:3000" to "Next.js / Create React App",
                    "http://localhost:5173" to "Vite / Vue / Svelte Dev Server",
                    "http://localhost:8080" to "Express / Spring / Local Web",
                    "http://10.0.2.2:3000" to "Android Emulator Localhost Alias"
                )

                items(presets.size) { idx ->
                    val (url, label) = presets[idx]
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                targetUrl = url
                                onLaunchUrl(url)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Public, contentDescription = null, tint = ChromeBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = url, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else {
                // Live Code Sandbox
                item {
                    Text(
                        text = "Agent Code Preview Sandbox",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Paste or edit HTML/JS/CSS code here. The browser will render the live preview and run human-like cursor testing over all buttons to catch broken features.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    OutlinedTextField(
                        value = htmlCode,
                        onValueChange = { htmlCode = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                item {
                    Button(
                        onClick = {
                            val encoded = "data:text/html;charset=utf-8," + URLEncoder.encode(htmlCode, "UTF-8")
                            onLaunchUrl(encoded)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ChromeBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Launch Sandbox Preview & Run Agent", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
