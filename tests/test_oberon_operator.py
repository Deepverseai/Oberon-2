#!/usr/bin/env python3
"""
Unit test suite for OberonOperator CLI ReAct Harness & Intent Resolution.
Tests Task Modalities (On-Page vs End-to-End) and Semantic Element Matching.
"""

import os
import sys
import unittest
from unittest.mock import MagicMock

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

class MockOberonClient:
    def __init__(self):
        self.last_navigated_url = None
        self.clicked_selectors = []
        self.typed_data = []

    def ping(self):
        return True

    def extract_semantic_map(self):
        return {
            "status": "ok",
            "url": "http://localhost:3000/register",
            "title": "Register Page",
            "inputs": [
                {"selector": "#username", "label": "Username", "placeholder": "Enter username", "type": "text", "xRatio": 0.5, "yRatio": 0.3},
                {"selector": "#email", "label": "Email Address", "placeholder": "name@example.com", "type": "email", "xRatio": 0.5, "yRatio": 0.45},
                {"selector": "#password", "label": "Password", "placeholder": "Secret", "type": "password", "xRatio": 0.5, "yRatio": 0.6}
            ],
            "buttons": [
                {"selector": "button.submit", "text": "Create Account", "role": "primary_action", "xRatio": 0.5, "yRatio": 0.75}
            ],
            "isAtBottom": False
        }

    def navigate(self, url):
        self.last_navigated_url = url
        return {"status": "ok", "url": url}

    def click(self, selector):
        self.clicked_selectors.append(selector)
        return {"status": "ok", "selector": selector}

    def type_text(self, selector, text):
        self.typed_data.append((selector, text))
        return {"status": "ok", "selector": selector, "text": text}

class TestOberonOperator(unittest.TestCase):

    def setUp(self):
        from scripts.oberon_operator import OberonOperator
        self.client = MockOberonClient()
        self.operator = OberonOperator(client=self.client)

    def test_classify_modality_on_page(self):
        # Contextual prompt without URL
        modality = self.operator.classify_modality("Fill this form with my saved profile")
        self.assertEqual(modality, "ON_PAGE_ACTION")

        modality2 = self.operator.classify_modality("Click the download button")
        self.assertEqual(modality2, "ON_PAGE_ACTION")

    def test_classify_modality_end_to_end(self):
        # Explicit navigation prompt
        modality = self.operator.classify_modality("Go to http://localhost:3000 and register")
        self.assertEqual(modality, "END_TO_END_JOURNEY")

        modality2 = self.operator.classify_modality("Open github.com and check notifications")
        self.assertEqual(modality2, "END_TO_END_JOURNEY")

    def test_find_matching_input_element(self):
        dom = self.client.extract_semantic_map()
        target = self.operator.match_input(dom["inputs"], "email")
        self.assertIsNotNone(target)
        self.assertEqual(target["selector"], "#email")

    def test_find_matching_button_element(self):
        dom = self.client.extract_semantic_map()
        target = self.operator.match_button(dom["buttons"], "create")
        self.assertIsNotNone(target)
        self.assertEqual(target["selector"], "button.submit")

if __name__ == "__main__":
    unittest.main()
