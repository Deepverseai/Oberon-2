#!/usr/bin/env python3
"""
Unit test for Dual Engine separation and mutual exclusion logic.
"""

import unittest

class EngineStateSimulator:
    def __init__(self):
        self.active_engine = "QA_AUDITOR"
        self.is_running = False
        self.current_goal = None

    def start_audit(self):
        if self.is_running:
            return {"status": "error", "message": "Engine is busy"}
        self.active_engine = "QA_AUDITOR"
        self.is_running = True
        return {"status": "ok", "engine": "QA_AUDITOR"}

    def start_operator(self, goal: str):
        if self.is_running:
            return {"status": "error", "message": "Engine is busy"}
        self.active_engine = "AOA_OPERATOR"
        self.current_goal = goal
        self.is_running = True
        return {"status": "ok", "engine": "AOA_OPERATOR", "goal": goal}

    def stop(self):
        self.is_running = False
        self.current_goal = None
        return {"status": "ok"}

class TestDualEngineSeparation(unittest.TestCase):

    def test_mutual_exclusion(self):
        sim = EngineStateSimulator()
        res1 = sim.start_operator("Fill registration form")
        self.assertEqual(res1["status"], "ok")
        self.assertEqual(sim.active_engine, "AOA_OPERATOR")

        # Attempt to run audit while operator is active -> must be blocked
        res2 = sim.start_audit()
        self.assertEqual(res2["status"], "error")
        self.assertEqual(res2["message"], "Engine is busy")

        sim.stop()
        self.assertFalse(sim.is_running)

        # Now audit can run
        res3 = sim.start_audit()
        self.assertEqual(res3["status"], "ok")
        self.assertEqual(sim.active_engine, "QA_AUDITOR")

        # Operator blocked while audit is running
        res4 = sim.start_operator("Submit form")
        self.assertEqual(res4["status"], "error")

if __name__ == "__main__":
    unittest.main()
