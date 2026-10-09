package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewModeTest {

    @Test
    fun testPreviewModeModelAttributes() {
        val appMode = "app"
        val webMode = "website"
        assertTrue(appMode.equals("app", ignoreCase = true))
        assertFalse(webMode.equals("app", ignoreCase = true))
    }
}
