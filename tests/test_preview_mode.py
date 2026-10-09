#!/usr/bin/env python3
"""
Unit test for Dual Viewport App Preview vs Web Preview mode contract.
"""

import unittest

class PreviewModeSimulator:
    def __init__(self):
        self.is_app_preview = False
        self.simulated_viewport_width = 1080
        self.simulated_viewport_height = 2400

    def set_mode(self, mode: str):
        if mode.lower() == "app":
            self.is_app_preview = True
            # Simulated 390x844 dp mobile frame canvas
            self.simulated_viewport_width = 390
            self.simulated_viewport_height = 844
        else:
            self.is_app_preview = False
            self.simulated_viewport_width = 1080
            self.simulated_viewport_height = 2400
        return {
            "status": "ok",
            "mode": mode,
            "isAppPreview": self.is_app_preview,
            "viewport": {
                "width": self.simulated_viewport_width,
                "height": self.simulated_viewport_height
            }
        }

class TestPreviewMode(unittest.TestCase):

    def test_default_mode_is_website(self):
        sim = PreviewModeSimulator()
        self.assertFalse(sim.is_app_preview)

    def test_toggle_to_app_mode(self):
        sim = PreviewModeSimulator()
        res = sim.set_mode("app")
        self.assertEqual(res["status"], "ok")
        self.assertTrue(res["isAppPreview"])
        self.assertEqual(res["viewport"]["width"], 390)
        self.assertEqual(res["viewport"]["height"], 844)

    def test_toggle_back_to_website_mode(self):
        sim = PreviewModeSimulator()
        sim.set_mode("app")
        res = sim.set_mode("website")
        self.assertEqual(res["status"], "ok")
        self.assertFalse(res["isAppPreview"])

if __name__ == "__main__":
    unittest.main()
