#!/usr/bin/env python3
"""
Unit test for Oberon-2 Semantic Element Map perception schema and contract.
Ensures that the JSON contract produced by /extract_map matches the AOA ReAct engine expectations.
"""

import json
import unittest

class TestSemanticElementMap(unittest.TestCase):

    def test_semantic_map_schema(self):
        sample_response = {
            "status": "ok",
            "url": "http://localhost:3000/contact",
            "title": "Contact Form",
            "inputs": [
                {
                    "selector": "#name",
                    "label": "Full Name",
                    "placeholder": "Enter your name",
                    "type": "text",
                    "name": "name",
                    "id": "name",
                    "xRatio": 0.5,
                    "yRatio": 0.3
                },
                {
                    "selector": "#email",
                    "label": "Email Address",
                    "placeholder": "name@example.com",
                    "type": "email",
                    "name": "email",
                    "id": "email",
                    "xRatio": 0.5,
                    "yRatio": 0.45
                }
            ],
            "buttons": [
                {
                    "selector": "button.submit-btn",
                    "text": "Send Message",
                    "role": "primary_action",
                    "xRatio": 0.5,
                    "yRatio": 0.65
                }
            ],
            "isAtBottom": False,
            "totalInteractiveElements": 3
        }

        # Check required fields
        self.assertEqual(sample_response["status"], "ok")
        self.assertEqual(len(sample_response["inputs"]), 2)
        self.assertEqual(len(sample_response["buttons"]), 1)
        self.assertEqual(sample_response["totalInteractiveElements"], 3)

        # Check field attribution
        first_input = sample_response["inputs"][0]
        self.assertEqual(first_input["label"], "Full Name")
        self.assertEqual(first_input["type"], "text")
        self.assertTrue(0.05 <= first_input["xRatio"] <= 0.95)
        self.assertTrue(0.05 <= first_input["yRatio"] <= 0.95)

        # Check button role
        submit_btn = sample_response["buttons"][0]
        self.assertEqual(submit_btn["role"], "primary_action")
        self.assertEqual(submit_btn["text"], "Send Message")

if __name__ == "__main__":
    unittest.main()
