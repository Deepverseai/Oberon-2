package com.example.agent

import java.util.UUID

enum class AgentEngineType(val displayName: String, val badge: String) {
    QA_AUDITOR("QA Button Auditor", "AUDIT"),
    AOA_OPERATOR("Autonomous Operator Agent", "AOA")
}

enum class AgentModeStatus(val displayName: String) {
    IDLE("Agent Idle"),
    SCANNING("Scanning DOM & Elements"),
    TESTING_BUTTONS("Testing Interactive Elements"),
    SCROLLING("Testing Viewport & Scroll"),
    ERROR_DETECTED("Bug Detected"),
    COMPLETED("Audit Completed"),
    OPERATOR_THINKING("AOA Thinking..."),
    OPERATOR_ACTING("AOA Executing Action..."),
    OPERATOR_VERIFYING("AOA Verifying Step...")
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

data class SemanticInputElement(
    val selector: String,
    val label: String,
    val placeholder: String,
    val type: String,
    val name: String = "",
    val id: String = "",
    val xRatio: Float = 0.5f,
    val yRatio: Float = 0.5f
) {
    fun toJsonObject(): org.json.JSONObject = org.json.JSONObject().apply {
        put("selector", selector)
        put("label", label)
        put("placeholder", placeholder)
        put("type", type)
        put("name", name)
        put("id", id)
        put("xRatio", xRatio.toDouble())
        put("yRatio", yRatio.toDouble())
    }
}

data class SemanticButtonElement(
    val selector: String,
    val text: String,
    val role: String = "button",
    val xRatio: Float = 0.5f,
    val yRatio: Float = 0.5f
) {
    fun toJsonObject(): org.json.JSONObject = org.json.JSONObject().apply {
        put("selector", selector)
        put("text", text)
        put("role", role)
        put("xRatio", xRatio.toDouble())
        put("yRatio", yRatio.toDouble())
    }
}

data class SemanticElementMap(
    val url: String = "",
    val title: String = "",
    val inputs: List<SemanticInputElement> = emptyList(),
    val buttons: List<SemanticButtonElement> = emptyList(),
    val isAtBottom: Boolean = false
) {
    fun toJsonObject(): org.json.JSONObject = org.json.JSONObject().apply {
        put("status", "ok")
        put("url", url)
        put("title", title)
        val inputsArr = org.json.JSONArray()
        inputs.forEach { inputsArr.put(it.toJsonObject()) }
        put("inputs", inputsArr)
        val buttonsArr = org.json.JSONArray()
        buttons.forEach { buttonsArr.put(it.toJsonObject()) }
        put("buttons", buttonsArr)
        put("isAtBottom", isAtBottom)
        put("totalInteractiveElements", inputs.size + buttons.size)
    }
}
