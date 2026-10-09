package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agent.AgentEngineType
import com.example.agent.AgentModeStatus
import com.example.ui.theme.ChromeBlue
import com.example.ui.theme.ChromeGreen
import com.example.ui.theme.ChromeRed
import kotlin.math.roundToInt

private val AOAPurple = Color(0xFF8B5CF6)
private val AOAAmber = Color(0xFFF59E0B)

/**
 * Draggable, collapsible floating capsule & control HUD for Antigravity Agent Platform.
 * Supports Dual-Engine Coexistence:
 * 1. QA Button Auditor: Stress testing, broken element detection, and DOM telemetry.
 * 2. Autonomous Operator Agent (AOA): In-app natural language goal dispatcher and ReAct loop.
 */
@Composable
fun AgentHudBar(
    status: AgentModeStatus,
    isRunning: Boolean,
    errorCount: Int,
    activeEngine: AgentEngineType = AgentEngineType.QA_AUDITOR,
    activeGoal: String = "",
    onSwitchEngine: (AgentEngineType) -> Unit = {},
    onDispatchOperatorGoal: (String) -> Unit = {},
    onRunButtonAudit: () -> Unit,
    onRunScrollTest: () -> Unit,
    onStopTest: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenSandbox: () -> Unit,
    onCloseAgentMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var promptInputText by remember { mutableStateOf("") }

    val activeAccentColor = when {
        errorCount > 0 -> ChromeRed
        activeEngine == AgentEngineType.AOA_OPERATOR -> AOAPurple
        else -> ChromeBlue
    }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .padding(12.dp)
    ) {
        if (!isExpanded) {
            // --- Sleek, Draggable Collapsed Floating Pill with Badge ---
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(
                    1.2.dp,
                    activeAccentColor.copy(alpha = 0.7f)
                ),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { isExpanded = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    // Engine Badge Bubble (AUDIT vs AOA)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = activeAccentColor.copy(alpha = 0.18f),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = activeEngine.badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeAccentColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    // Pulse indicator dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    errorCount > 0 -> ChromeRed
                                    isRunning -> activeAccentColor
                                    else -> ChromeGreen
                                }
                            )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Label / Status Text
                    Text(
                        text = when {
                            errorCount > 0 -> "$errorCount Bugs"
                            isRunning && activeEngine == AgentEngineType.AOA_OPERATOR -> "AOA Running..."
                            isRunning -> "Auditing..."
                            activeEngine == AgentEngineType.AOA_OPERATOR -> "AOA Ready"
                            else -> "Auditor Ready"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (errorCount > 0) ChromeRed else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            // --- Expanded Floating Control Sheet with Dual Engine Tabs ---
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    activeAccentColor.copy(alpha = 0.5f)
                ),
                shadowElevation = 14.dp,
                modifier = Modifier
                    .widthIn(max = 330.dp)
                    .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header with Drag Handle & Collapse/Close Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = activeAccentColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (activeEngine == AgentEngineType.AOA_OPERATOR) Icons.Default.AutoAwesome else Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = activeAccentColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (activeEngine == AgentEngineType.AOA_OPERATOR) "AOA Operator" else "Antigravity Auditor",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = status.displayName,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Collapse Button (Back to small pill)
                            IconButton(
                                onClick = { isExpanded = false },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Collapse HUD",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            // Close Agent Mode entirely
                            IconButton(
                                onClick = onCloseAgentMode,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Agent Mode",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // --- Dual Engine Switcher Tabs ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        EngineTabPill(
                            label = "🧪 QA Auditor",
                            isSelected = activeEngine == AgentEngineType.QA_AUDITOR,
                            selectedColor = ChromeBlue,
                            onClick = { onSwitchEngine(AgentEngineType.QA_AUDITOR) },
                            modifier = Modifier.weight(1f)
                        )
                        EngineTabPill(
                            label = "🤖 AOA Operator",
                            isSelected = activeEngine == AgentEngineType.AOA_OPERATOR,
                            selectedColor = AOAPurple,
                            onClick = { onSwitchEngine(AgentEngineType.AOA_OPERATOR) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // --- Engine Content ---
                    if (activeEngine == AgentEngineType.AOA_OPERATOR) {
                        // In-App AOA Prompt Input Bar
                        Text(
                            text = "Ask AOA to operate on this page:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AOAPurple.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                BasicTextField(
                                    value = promptInputText,
                                    onValueChange = { promptInputText = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    cursorBrush = SolidColor(AOAPurple),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = {
                                        if (promptInputText.isNotBlank()) {
                                            onDispatchOperatorGoal(promptInputText)
                                            promptInputText = ""
                                        }
                                    }),
                                    modifier = Modifier.weight(1f),
                                    decorationBox = { innerTextField ->
                                        if (promptInputText.isEmpty()) {
                                            Text(
                                                text = "e.g. Fill form with John Doe...",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                )

                                IconButton(
                                    onClick = {
                                        if (promptInputText.isNotBlank()) {
                                            onDispatchOperatorGoal(promptInputText)
                                            promptInputText = ""
                                        }
                                    },
                                    enabled = promptInputText.isNotBlank() && !isRunning,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Dispatch AOA Goal",
                                        tint = if (promptInputText.isNotBlank()) AOAPurple else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        if (isRunning) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HudActionChip(
                                icon = Icons.Default.Stop,
                                label = "Stop AOA Operator",
                                color = ChromeRed,
                                onClick = onStopTest,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        // QA Button Auditor Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isRunning) {
                                HudActionChip(
                                    icon = Icons.Default.Stop,
                                    label = "Stop Audit",
                                    color = ChromeRed,
                                    onClick = onStopTest,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                HudActionChip(
                                    icon = Icons.Default.PlayArrow,
                                    label = "Audit All",
                                    color = ChromeBlue,
                                    onClick = onRunButtonAudit,
                                    modifier = Modifier.weight(1f)
                                )
                                HudActionChip(
                                    icon = Icons.Default.SwapVert,
                                    label = "Scroll Test",
                                    color = MaterialTheme.colorScheme.secondary,
                                    onClick = onRunScrollTest,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HudActionChip(
                                icon = Icons.Default.Code,
                                label = "Sandbox App",
                                color = MaterialTheme.colorScheme.primary,
                                onClick = onOpenSandbox,
                                modifier = Modifier.weight(1f)
                            )

                            if (errorCount > 0) {
                                HudActionChip(
                                    icon = Icons.Default.BugReport,
                                    label = "$errorCount Bugs Found",
                                    color = ChromeRed,
                                    onClick = onOpenDiagnostics,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EngineTabPill(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) selectedColor.copy(alpha = 0.2f) else Color.Transparent,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, selectedColor.copy(alpha = 0.6f)) else null,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 5.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HudActionChip(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                maxLines = 1
            )
        }
    }
}
