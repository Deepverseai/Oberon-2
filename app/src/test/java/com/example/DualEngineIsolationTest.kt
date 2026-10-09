package com.example

import com.example.agent.AgentEngineType
import com.example.agent.AgentModeStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class DualEngineIsolationTest {

    @Test
    fun testEngineTypesDefinition() {
        assertEquals("AUDIT", AgentEngineType.QA_AUDITOR.badge)
        assertEquals("AOA", AgentEngineType.AOA_OPERATOR.badge)
        assertEquals("QA Button Auditor", AgentEngineType.QA_AUDITOR.displayName)
        assertEquals("Autonomous Operator Agent", AgentEngineType.AOA_OPERATOR.displayName)
    }

    @Test
    fun testOperatorStatusLifecycle() {
        assertEquals("AOA Thinking...", AgentModeStatus.OPERATOR_THINKING.displayName)
        assertEquals("AOA Executing Action...", AgentModeStatus.OPERATOR_ACTING.displayName)
        assertEquals("AOA Verifying Step...", AgentModeStatus.OPERATOR_VERIFYING.displayName)
    }
}
