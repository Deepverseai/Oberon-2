#!/usr/bin/env python3
"""
Unit test for Oberon Watcher Daemon and Self-Healing Bug Report generation.
"""

import os
import sys
import unittest

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

class MockWatcherBridge:
    def __init__(self, errors=None):
        self.errors = errors or []

    def get_telemetry(self):
        return {
            "status": "ok",
            "logs": ["[INFO] Page loaded", "[ERROR] Script failed"],
            "errors": self.errors
        }

class TestWatcherDaemon(unittest.TestCase):

    def test_parse_telemetry_errors(self):
        from scripts.oberon_watch_daemon import OberonWatchDaemon

        errors = [
            "BUTTON_CLICK_FAILURE: applyDiscount is not defined (selector: #btn-discount)"
        ]
        bridge = MockWatcherBridge(errors=errors)
        daemon = OberonWatchDaemon(bridge=bridge, target_url="http://localhost:3000")

        report = daemon.generate_self_heal_prompt(errors)
        self.assertIn("Antigravity Browser Agent Bug Report", report)
        self.assertIn("#btn-discount", report)
        self.assertIn("applyDiscount is not defined", report)
        self.assertIn("Task for Antigravity CLI", report)

    def test_clean_telemetry_no_errors(self):
        from scripts.oberon_watch_daemon import OberonWatchDaemon

        bridge = MockWatcherBridge(errors=[])
        daemon = OberonWatchDaemon(bridge=bridge, target_url="http://localhost:3000")

        report = daemon.generate_self_heal_prompt([])
        self.assertIsNone(report)

if __name__ == "__main__":
    unittest.main()
