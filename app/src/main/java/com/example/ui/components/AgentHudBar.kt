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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agent.AgentModeStatus
import com.example.ui.theme.ChromeBlue
import com.example.ui.theme.ChromeGreen
import com.example.ui.theme.ChromeRed
import kotlin.math.roundToInt

/**
 * Draggable, collapsible floating capsule & control HUD for Antigravity Agent Mode.
 * Solves viewport occlusion: collapses into a tiny, non-intrusive floating pill
 * that can be dragged anywhere on screen (left, right, up, down).
 */
@Composable
fun AgentHudBar(
    status: AgentModeStatus,
    isRunning: Boolean,
    errorCount: Int,
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
            // --- Sleek, Draggable Collapsed Floating Pill ---
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(
                    1.2.dp,
                    if (errorCount > 0) ChromeRed.copy(alpha = 0.8f) else ChromeBlue.copy(alpha = 0.6f)
                ),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { isExpanded = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    // Robot Icon in glowing bubble
                    Surface(
                        shape = CircleShape,
                        color = if (errorCount > 0) ChromeRed.copy(alpha = 0.15f) else ChromeBlue.copy(alpha = 0.15f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "Expand Agent HUD",
                                tint = if (errorCount > 0) ChromeRed else ChromeBlue,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(7.dp))

                    // Pulse indicator dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    errorCount > 0 -> ChromeRed
                                    isRunning -> ChromeBlue
                                    else -> ChromeGreen
                                }
                            )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Label / Bug Counter
                    Text(
                        text = when {
                            errorCount > 0 -> "$errorCount Bugs"
                            isRunning -> "Running..."
                            else -> "Agent"
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
            // --- Expanded Floating Control Sheet (Compact, Doesn't Cover Full Screen) ---
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (errorCount > 0) ChromeRed.copy(alpha = 0.6f) else ChromeBlue.copy(alpha = 0.5f)
                ),
                shadowElevation = 14.dp,
                modifier = Modifier
                    .widthIn(max = 320.dp)
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
                                color = ChromeBlue.copy(alpha = 0.15f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = ChromeBlue,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Antigravity Agent",
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

                    // Action Grid / Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isRunning) {
                            HudActionChip(
                                icon = Icons.Default.Stop,
                                label = "Stop Test",
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
