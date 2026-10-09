#!/usr/bin/env python3
"""
Oberon-2 Autonomous Operator Agent (AOA) CLI ReAct Controller.
Enables Antigravity CLI and autonomous AI agents to drive Oberon-2 as a physical visual canvas
without X11/VNC desktop overhead. Implements Observe -> Think -> Act -> Verify cognition loop.
"""

import argparse
import json
import os
import re
import subprocess
import sys
import time
import urllib.request
import urllib.error

DEFAULT_PORT = 8765
DEFAULT_HOST = "127.0.0.1"
DEFAULT_TOKEN = "oberon_local_secret_token"

class OberonClient:
    def __init__(self, host=DEFAULT_HOST, port=DEFAULT_PORT, token=DEFAULT_TOKEN, auto_wake=True):
        self.host = host
        self.port = port
        self.token = token
        self.base_url = f"http://{host}:{port}"
        self.auto_wake = auto_wake

    def _post(self, payload: dict) -> dict:
        data = json.dumps(payload).encode("utf-8")
        req = urllib.request.Request(
            self.base_url,
            data=data,
            headers={
                "Content-Type": "application/json",
                "X-Agent-Token": self.token
            },
            method="POST"
        )
        try:
            with urllib.request.urlopen(req, timeout=8) as response:
                return json.loads(response.read().decode("utf-8"))
        except (urllib.error.URLError, ConnectionRefusedError, TimeoutError) as e:
            if self.auto_wake:
                self.wake_app()
                time.sleep(1.2)
                try:
                    with urllib.request.urlopen(req, timeout=8) as retry_res:
                        return json.loads(retry_res.read().decode("utf-8"))
                except Exception as retry_err:
                    return {"status": "error", "message": f"Bridge error after auto-wake: {retry_err}"}
            return {"status": "error", "message": str(e)}

    def ping(self) -> bool:
        req = urllib.request.Request(
            self.base_url,
            headers={"X-Agent-Token": self.token},
            method="GET"
        )
        try:
            with urllib.request.urlopen(req, timeout=2) as response:
                return response.status == 200
        except Exception:
            return False

    def wake_app(self, initial_url: str = None) -> bool:
        am_bin = "/data/data/com.termux/files/usr/bin/am" if os.path.exists("/data/data/com.termux/files/usr/bin/am") else "am"
        cmd = [am_bin, "start", "--user", "0", "-n", "com.aistudio.oberonbrowser.qkrvwx/com.example.MainActivity"]
        if initial_url:
            cmd.extend(["-d", initial_url])
        try:
            res = subprocess.run(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=4)
            return res.returncode == 0
        except Exception:
            return False

    def navigate(self, url: str) -> dict:
        return self._post({"action": "navigate", "url": url})

    def extract_semantic_map(self) -> dict:
        return self._post({"action": "extract_map"})

    def click(self, selector: str) -> dict:
        return self._post({"action": "click", "selector": selector})

    def type_text(self, selector: str, text: str) -> dict:
        return self._post({"action": "type", "selector": selector, "text": text})

    def scroll(self, dy: int = 400) -> dict:
        return self._post({"action": "scroll", "dy": dy})

    def operate(self, goal: str) -> dict:
        return self._post({"action": "operate", "goal": goal})

    def get_telemetry(self) -> dict:
        return self._post({"action": "get_telemetry"})


class OberonOperator:
    def __init__(self, client: OberonClient = None):
        self.client = client or OberonClient()

    def classify_modality(self, goal: str) -> str:
        """
        Autonomously resolves intent into:
        - Modality 1: ON_PAGE_ACTION (In-situ, no explicit navigation required)
        - Modality 2: END_TO_END_JOURNEY (Explicit navigation, multi-hop workflow)
        """
        nav_pattern = r"(https?://|localhost(:\d+)?|www\.|go to\s+|open\s+|browse\s+|navigate to\s+|search on\s+)"
        if re.search(nav_pattern, goal, re.IGNORECASE):
            return "END_TO_END_JOURNEY"
        return "ON_PAGE_ACTION"

    def match_input(self, inputs: list, query: str) -> dict:
        q = query.lower().strip()
        for item in inputs:
            lbl = item.get("label", "").lower()
            name = item.get("name", "").lower()
            elem_id = item.get("id", "").lower()
            ph = item.get("placeholder", "").lower()
            typ = item.get("type", "").lower()
            if q in lbl or q in name or q in elem_id or q in ph or q == typ:
                return item
        return inputs[0] if inputs else None

    def match_button(self, buttons: list, query: str) -> dict:
        q = query.lower().strip()
        for b in buttons:
            txt = b.get("text", "").lower()
            role = b.get("role", "").lower()
            sel = b.get("selector", "").lower()
            if q in txt or q in role or q in sel:
                return b
        for b in buttons:
            if b.get("role") == "primary_action":
                return b
        return buttons[0] if buttons else None

    def run_goal(self, goal: str, simulate_human: bool = True) -> dict:
        """
        Full ReAct loop: Observe -> Think -> Act -> Verify
        """
        print(f"🤖 [Oberon AOA] Initializing Goal: '{goal}'")
        modality = self.classify_modality(goal)
        print(f"🎯 [Oberon AOA] Modality Resolved: {modality}")

        # Notify Oberon APK about incoming goal to activate HUD
        self.client.operate(goal)
        time.sleep(0.5)

        # Modality 2: Navigate to target origin if explicit
        if modality == "END_TO_END_JOURNEY":
            url_match = re.search(r"(https?://[^\s]+|localhost(:\d+)?[^\s]*)", goal)
            if url_match:
                target_url = url_match.group(0)
                if not target_url.startswith("http"):
                    target_url = "http://" + target_url
                print(f"🌐 [Oberon AOA] Navigating origin: {target_url}")
                self.client.navigate(target_url)
                time.sleep(2.0)

        # 1. OBSERVE: Extract active semantic map
        print("👀 [Oberon AOA] Observing Active DOM Semantic Map...")
        dom_map = self.client.extract_semantic_map()
        if dom_map.get("status") != "ok":
            return {"status": "error", "message": f"Failed to observe DOM: {dom_map.get('message')}"}

        inputs = dom_map.get("inputs", [])
        buttons = dom_map.get("buttons", [])
        print(f"📄 [Oberon AOA] Observed: {len(inputs)} Inputs, {len(buttons)} Action Buttons on '{dom_map.get('title')}'")

        # 2. THINK & ACT: Form filling heuristic sequence
        actions_taken = []
        if "form" in goal.lower() or "fill" in goal.lower() or "register" in goal.lower() or "login" in goal.lower():
            for inp in inputs:
                sel = inp["selector"]
                lbl = inp.get("label") or inp.get("placeholder") or inp.get("name") or "field"
                typ = inp.get("type", "text")

                fill_val = "Test User"
                if "email" in typ or "email" in lbl.lower():
                    fill_val = "test@example.com"
                elif "pass" in typ or "pass" in lbl.lower():
                    fill_val = "Secret123!"
                elif "user" in lbl.lower() or "name" in lbl.lower():
                    fill_val = "AntigravityUser"
                elif "phone" in typ or "phone" in lbl.lower() or "tel" in typ:
                    fill_val = "+1234567890"

                print(f"✍️ [Oberon AOA] Typing into '{lbl}' ({sel}) -> '{fill_val}'")
                self.client.type_text(sel, fill_val)
                actions_taken.append({"action": "type", "selector": sel, "value": fill_val})
                if simulate_human:
                    time.sleep(0.3 + (len(fill_val) * 0.04))

        if "submit" in goal.lower() or "send" in goal.lower() or "click" in goal.lower() or "register" in goal.lower():
            target_btn = self.match_button(buttons, "submit") or self.match_button(buttons, "primary")
            if target_btn:
                btn_sel = target_btn["selector"]
                btn_txt = target_btn.get("text", "Button")
                print(f"🎯 [Oberon AOA] Targeting & Clicking Action Button: '{btn_txt}' ({btn_sel})")
                self.client.click(btn_sel)
                actions_taken.append({"action": "click", "selector": btn_sel, "text": btn_txt})
                time.sleep(1.0)

        # 3. VERIFY: Post-action telemetry & state check
        print("🔍 [Oberon AOA] Verifying Post-Action Execution...")
        telemetry = self.client.get_telemetry()
        errors = telemetry.get("errors", [])
        if errors:
            print(f"⚠️ [Oberon AOA] Runtime Errors Detected: {errors}")
            return {
                "status": "partial_success_with_errors",
                "goal": goal,
                "modality": modality,
                "actions_taken": actions_taken,
                "errors": errors
            }

        print("✨ [Oberon AOA] Goal Completed Successfully with Zero Errors!")
        return {
            "status": "ok",
            "goal": goal,
            "modality": modality,
            "actions_taken": actions_taken,
            "total_actions": len(actions_taken)
        }


def main():
    parser = argparse.ArgumentParser(description="Oberon-2 Autonomous Operator Agent CLI Controller")
    parser.add_argument("--goal", type=str, required=True, help="Natural language objective for the operator")
    parser.add_argument("--port", type=int, default=DEFAULT_PORT, help="Oberon HTTP bridge port")
    parser.add_argument("--token", type=str, default=DEFAULT_TOKEN, help="Local bridge secret token")
    args = parser.parse_args()

    client = OberonClient(port=args.port, token=args.token)
    operator = OberonOperator(client=client)
    result = operator.run_goal(args.goal)
    print(json.dumps(result, indent=2))

if __name__ == "__main__":
    main()
