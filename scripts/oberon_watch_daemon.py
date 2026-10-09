#!/usr/bin/env python3
"""
Oberon-2 Autonomous Developer Watcher Daemon & Self-Healing Pipeline.
Enables Antigravity CLI to maintain a closed-loop hot fix cycle:
1. Observes local dev server changes (e.g., http://localhost:3000).
2. Dispatches live preview reload to Oberon-2 in App or Web frame.
3. Automatically triggers QA Auditor progressive stress test.
4. Traps any ANTIGRAVITY_AGENT_ERROR exceptions from telemetry.
5. Emits structured Markdown fix prompt back to Antigravity CLI for autonomous self-healing.
"""

import argparse
import json
import os
import sys
import time

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from scripts.oberon_operator import OberonClient

class OberonWatchDaemon:
    def __init__(self, bridge=None, target_url="http://localhost:3000", is_app_frame=False):
        self.bridge = bridge or OberonClient()
        self.target_url = target_url
        self.is_app_frame = is_app_frame

    def generate_self_heal_prompt(self, errors: list) -> str | None:
        if not errors:
            return None

        lines = [
            "### [Antigravity Browser Agent Bug Report]",
            f"**Target Preview URL:** {self.target_url}",
            f"**Total Bugs Trapped:** {len(errors)}",
            "",
            "#### Trapped Exceptions & Broken Elements:"
        ]

        for idx, err in enumerate(errors, 1):
            lines.append(f"{idx}. `{err}`")

        lines.extend([
            "",
            "**Task for Antigravity CLI / AI Coding Agent:**",
            "1. Inspect the stack trace and selector above to identify the offending component or handler.",
            "2. Fix the runtime failure or missing variable/function in the local source code.",
            "3. Save the file so hot reload updates Oberon Browser preview.",
            "4. Re-run `python3 scripts/oberon_watch_daemon.py --url <url>` to verify the fix with 0 errors."
        ])
        return "\n".join(lines)

    def run_preview_audit_cycle(self, timeout_sec: int = 10) -> dict:
        print(f"🔄 [Oberon Watcher] Navigating live preview: {self.target_url}")
        nav_res = self.bridge.navigate(self.target_url)
        time.sleep(2.0)

        print("🧪 [Oberon Watcher] Initiating Autonomous QA Button Audit...")
        audit_res = self.bridge._post({"action": "audit"})
        time.sleep(3.0)

        print("📡 [Oberon Watcher] Polling Telemetry for Runtime Bugs...")
        telemetry = self.bridge.get_telemetry()
        errors = telemetry.get("errors", [])

        if errors:
            print(f"❌ [Oberon Watcher] Caught {len(errors)} runtime error(s)!")
            prompt = self.generate_self_heal_prompt(errors)
            return {
                "status": "errors_detected",
                "error_count": len(errors),
                "errors": errors,
                "self_heal_prompt": prompt
            }

        print("✨ [Oberon Watcher] Preview Verified: 0 runtime errors detected across all elements!")
        return {
            "status": "ok",
            "message": "All interactive elements operational.",
            "errors": []
        }

def main():
    parser = argparse.ArgumentParser(description="Oberon-2 Closed-Loop Developer Watcher Daemon")
    parser.add_argument("--url", type=str, default="http://localhost:3000", help="Local dev server URL")
    parser.add_argument("--app-frame", action="store_true", help="Enable 390x844 mobile device frame")
    args = parser.parse_args()

    client = OberonClient()
    daemon = OberonWatchDaemon(bridge=client, target_url=args.url, is_app_frame=args.app_frame)
    result = daemon.run_preview_audit_cycle()
    print(json.dumps(result, indent=2))
    if result.get("self_heal_prompt"):
        print("\n" + result["self_heal_prompt"])

if __name__ == "__main__":
    main()
