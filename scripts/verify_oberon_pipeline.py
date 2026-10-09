#!/usr/bin/env python3
"""
Master End-to-End Pipeline Verification Suite for Oberon-2 AOA & Preview Pipeline.
Verifies all 6 Tasks:
1. Semantic DOM Extractor contract
2. Dual-Engine Isolation logic
3. Dual Viewport App Frame simulation
4. Antigravity CLI AOA ReAct Operator harness
5. Closed-Loop Developer Watcher Daemon & MCP server tools
6. Android source code integrity and CI readiness
"""

import os
import subprocess
import sys
import unittest

def run_test_module(test_path):
    print(f"▶️ Running: {os.path.basename(test_path)}...")
    res = subprocess.run([sys.executable, test_path], capture_output=True, text=True)
    if res.returncode == 0:
        print(f"  ✅ {os.path.basename(test_path)}: PASSED")
        return True
    else:
        print(f"  ❌ {os.path.basename(test_path)}: FAILED")
        print(res.stderr or res.stdout)
        return False

def verify_code_integrity():
    print("▶️ Checking Android source code integrity...")
    checks = [
        ("app/src/main/java/com/example/agent/AgentModels.kt", ["SemanticElementMap", "AgentEngineType", "OPERATOR_THINKING"]),
        ("app/src/main/java/com/example/viewmodel/BrowserViewModel.kt", ["agentExtractSemanticMap", "runAutonomousOperatorGoal", "setPreviewMode"]),
        ("app/src/main/java/com/example/agent/AgentServer.kt", ["extract_map", "operate", "set_preview_mode"]),
        ("app/src/main/java/com/example/ui/components/AgentHudBar.kt", ["AgentEngineType", "AOA Operator", "Dispatch AOA Goal"]),
        ("app/src/main/java/com/example/ui/screens/WebScreen.kt", ["isPreviewAppFrame", "webContentModifier"]),
        ("app/src/main/java/com/example/MainActivity.kt", ["isPreviewAppFrame", "activeEngine", "activeGoal"]),
        (".github/workflows/build-apks.yml", ["testing"])
    ]

    base_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    all_ok = True
    for rel_path, required_tokens in checks:
        full_path = os.path.join(base_dir, rel_path)
        if not os.path.exists(full_path):
            print(f"  ❌ Missing file: {rel_path}")
            all_ok = False
            continue
        with open(full_path, "r", encoding="utf-8") as f:
            content = f.read()
        for token in required_tokens:
            if token not in content:
                print(f"  ❌ Missing required token '{token}' in {rel_path}")
                all_ok = False
            else:
                print(f"  ✅ Found '{token}' in {rel_path}")
    return all_ok

def main():
    print("=" * 60)
    print("🚀 OBERON-2 AOA & PREVIEW PIPELINE VERIFICATION SUITE")
    print("=" * 60)

    base_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    test_files = [
        os.path.join(base_dir, "tests/test_semantic_map.py"),
        os.path.join(base_dir, "tests/test_dual_engine.py"),
        os.path.join(base_dir, "tests/test_preview_mode.py"),
        os.path.join(base_dir, "tests/test_oberon_operator.py"),
        os.path.join(base_dir, "tests/test_watch_daemon.py"),
    ]

    test_results = [run_test_module(t) for t in test_files]
    integrity_ok = verify_code_integrity()

    print("=" * 60)
    if all(test_results) and integrity_ok:
        print("🎉 ALL CHECKS PASSED: Oberon-2 AOA Pipeline is 100% verified and ready for CI!")
        print("=" * 60)
        sys.exit(0)
    else:
        print("💥 SOME VERIFICATIONS FAILED. Check logs above.")
        print("=" * 60)
        sys.exit(1)

if __name__ == "__main__":
    main()
