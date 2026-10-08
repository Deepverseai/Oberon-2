package com.example

import com.example.agent.SemanticButtonElement
import com.example.agent.SemanticElementMap
import com.example.agent.SemanticInputElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanticElementMapTest {

    @Test
    fun testSemanticInputElementSerialization() {
        val input = SemanticInputElement(
            selector = "#email",
            label = "Your Work Email",
            placeholder = "name@company.com",
            type = "email",
            name = "email_field",
            id = "email",
            xRatio = 0.5f,
            yRatio = 0.35f
        )
        val json = input.toJsonObject()
        assertEquals("#email", json.getString("selector"))
        assertEquals("Your Work Email", json.getString("label"))
        assertEquals("name@company.com", json.getString("placeholder"))
        assertEquals("email", json.getString("type"))
        assertEquals(0.5, json.getDouble("xRatio"), 0.001)
        assertEquals(0.35, json.getDouble("yRatio"), 0.001)
    }

    @Test
    fun testSemanticButtonElementSerialization() {
        val button = SemanticButtonElement(
            selector = "#submit-btn",
            text = "Submit Form",
            role = "primary_action",
            xRatio = 0.5f,
            yRatio = 0.8f
        )
        val json = button.toJsonObject()
        assertEquals("#submit-btn", json.getString("selector"))
        assertEquals("Submit Form", json.getString("text"))
        assertEquals("primary_action", json.getString("role"))
    }

    @Test
    fun testSemanticElementMapSerialization() {
        val input = SemanticInputElement(
            selector = "#username",
            label = "Username",
            placeholder = "Enter user",
            type = "text"
        )
        val button = SemanticButtonElement(
            selector = "#login",
            text = "Log In",
            role = "primary_action"
        )
        val map = SemanticElementMap(
            url = "https://example.com/login",
            title = "Login Page",
            inputs = listOf(input),
            buttons = listOf(button),
            isAtBottom = true
        )

        val json = map.toJsonObject()
        assertEquals("ok", json.getString("status"))
        assertEquals("https://example.com/login", json.getString("url"))
        assertEquals("Login Page", json.getString("title"))
        assertEquals(1, json.getJSONArray("inputs").length())
        assertEquals(1, json.getJSONArray("buttons").length())
        assertEquals(2, json.getInt("totalInteractiveElements"))
        assertTrue(json.getBoolean("isAtBottom"))
    }
}
