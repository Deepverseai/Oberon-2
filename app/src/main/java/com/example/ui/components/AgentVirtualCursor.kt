package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agent.AgentCursorState
import com.example.ui.theme.ChromeBlue
import kotlin.math.roundToInt

@Composable
fun AgentVirtualCursor(
    cursorState: AgentCursorState,
    modifier: Modifier = Modifier
) {
    if (!cursorState.isVisible) return

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewWidthPx = constraints.maxWidth.toFloat()
        val viewHeightPx = constraints.maxHeight.toFloat()

        // Human-like smooth trajectory animation
        val animatedXPx by animateFloatAsState(
            targetValue = (cursorState.xRatio * viewWidthPx).coerceIn(20f, viewWidthPx - 20f),
            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
            label = "agent_cursor_x"
        )
        val animatedYPx by animateFloatAsState(
            targetValue = (cursorState.yRatio * viewHeightPx).coerceIn(40f, viewHeightPx - 40f),
            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
            label = "agent_cursor_y"
        )

        // Click Ripple pulse animation
        val clickPulseRadius = remember { Animatable(0f) }
        val clickPulseAlpha = remember { Animatable(0f) }

        LaunchedEffect(cursorState.pulseCount) {
            if (cursorState.pulseCount > 0) {
                clickPulseRadius.snapTo(4f)
                clickPulseAlpha.snapTo(0.9f)
                clickPulseRadius.animateTo(
                    targetValue = 38f,
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                )
                clickPulseAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 150)
                )
            }
        }

        Box(
            modifier = Modifier.offset {
                IntOffset(animatedXPx.roundToInt(), animatedYPx.roundToInt())
            }
        ) {
            // Expanding Ripple ring on click
            if (clickPulseAlpha.value > 0.05f) {
                Canvas(modifier = Modifier.size(80.dp).offset((-40).dp, (-40).dp)) {
                    drawCircle(
                        color = ChromeBlue.copy(alpha = clickPulseAlpha.value),
                        radius = clickPulseRadius.value,
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            // Visible Human-like Cursor Pointer
            Canvas(
                modifier = Modifier
                    .size(28.dp)
                    .shadow(4.dp, shape = CircleShape)
            ) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(22f, 15f)
                    lineTo(12f, 16f)
                    lineTo(16f, 25f)
                    lineTo(12f, 27f)
                    lineTo(8f, 18f)
                    lineTo(0f, 22f)
                    close()
                }
                // Outline
                drawPath(path = path, color = Color.White, style = Stroke(width = 2.5.dp.toPx()))
                // Body
                drawPath(path = path, color = ChromeBlue)
            }

            // Floating status chip attached to cursor with smart boundary flipping
            if (cursorState.actionText.isNotBlank()) {
                val isNearRightEdge = cursorState.xRatio > 0.52f
                val isNearBottomEdge = cursorState.yRatio > 0.82f

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ChromeBlue.copy(alpha = 0.55f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .offset(
                            x = if (isNearRightEdge) (-175).dp else 20.dp,
                            y = if (isNearBottomEdge) (-34).dp else 16.dp
                        )
                        .widthIn(max = 175.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ChromeBlue)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cursorState.actionText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
