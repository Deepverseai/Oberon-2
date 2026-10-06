package com.example.agent

import java.util.UUID

enum class AgentModeStatus(val displayName: String) {
    IDLE("Agent Idle"),
    SCANNING("Scanning DOM & Elements"),
    TESTING_BUTTONS("Testing Interactive Elements"),
    SCROLLING("Testing Viewport & Scroll"),
    ERROR_DETECTED("Bug Detected"),
    COMPLETED("Audit Completed")
}

data class AgentCursorState(
    val xRatio: Float = 0.5f, // 0.0 to 1.0 (relative to viewport width)
    val yRatio: Float = 0.5f, // 0.0 to 1.0 (relative to viewport height)
    val isVisible: Boolean = false,
    val isClicking: Boolean = false,
    val actionText: String = "",
    val pulseCount: Int = 0
)

data class InteractiveElementInfo(
    val selector: String,
    val text: String,
    val tag: String,
    val xRatio: Float,
    val yRatio: Float,
    val rawX: Int,
    val rawY: Int
)

data class AgentDetectedError(
    val id: String = UUID.randomUUID().toString(),
    val errorType: String,
    val message: String,
    val filename: String = "",
    val lineNumber: Int = 0,
    val stackTrace: String = "",
    val brokenElementSelector: String? = null,
    val actionContext: String = "",
    val url: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun generateAgentFixPrompt(): String {
        return buildString {
            appendLine("### [Antigravity Browser Agent Bug Report]")
            appendLine("**Page URL:** $url")
            if (!brokenElementSelector.isNullOrBlank()) {
                appendLine("**Broken Element:** `$brokenElementSelector`")
            }
            if (actionContext.isNotBlank()) {
                appendLine("**Action Taken:** $actionContext")
            }
            appendLine("**Error Type:** $errorType")
            appendLine("**Error Message:** $message")
            if (filename.isNotBlank()) {
                appendLine("**Source File:** $filename:${lineNumber}")
            }
            if (stackTrace.isNotBlank()) {
                appendLine("**Stack Trace:**")
                appendLine("```")
                appendLine(stackTrace.take(800))
                appendLine("```")
            }
            appendLine()
            appendLine("**Task for Antigravity CLI / AI Coding Agent:**")
            appendLine("1. Locate the source code causing this issue ($filename:$lineNumber).")
            appendLine("2. Fix the runtime failure / unhandled exception in the click handler or component.")
            appendLine("3. Save the file and reload the page in Oberon Browser to verify the fix with the agent test suite.")
        }
    }
}

data class AgentAuditReport(
    val totalElementsScanned: Int = 0,
    val buttonsTested: Int = 0,
    val brokenElementsCount: Int = 0,
    val errors: List<AgentDetectedError> = emptyList(),
    val durationSeconds: Float = 0f,
    val passedSuccessfully: Boolean = true
)
