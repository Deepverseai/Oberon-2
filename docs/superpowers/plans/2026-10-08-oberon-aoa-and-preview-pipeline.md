# Oberon-2: Autonomous Operator Agent (AOA) & Closed-Loop Developer Pipeline Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transform Oberon-2 into Android's premier Autonomous Operator Agent (AOA) and Antigravity CLI live companion surface by implementing Semantic DOM perception, dual-engine isolation (QA Auditor vs AOA Operator), dual-mode triggers (CLI + in-app capsule prompt bar), simulated mobile app frame preview, and a closed-loop CLI self-healing pipeline.

**Architecture:** 
- **Cognition & ReAct Loop (Brain):** Antigravity CLI / Python Harness running the autonomous Observe-Think-Act-Verify cycle without X11/VNC desktop overhead.
- **Physical Canvas & Sensors (Eyes & Hands):** Oberon-2 Android Compose app exposing embedded HTTP bridge on `127.0.0.1:8765`, rendering draggable capsule HUD, virtual cursor with edge-flipping, and DOM dispatchers.
- **Dual-Engine Coexistence:** Strict isolation between Engine 1 (QA button stress auditor) and Engine 2 (AOA goal operator) sharing visual hardware with zero regressions.
- **Closed-Loop Self-Healing:** Live code watcher triggering Oberon preview, catching runtime exceptions (`ANTIGRAVITY_AGENT_ERROR`), and returning actionable telemetry to the CLI.

**Tech Stack:** Kotlin 1.9+, Android SDK 34/36, Jetpack Compose, Coroutines/StateFlow, Python 3, Model Context Protocol (MCP), GitHub Actions CI/CD.

**Spec:**
- Master Architecture Hub: `/storage/emulated/0/Agent_Graph/ObsidianVault/Oberon_Browser_Master_Architecture.md`
- AOA Master Plan: `/storage/emulated/0/Agent_Graph/ObsidianVault/Oberon_Autonomous_Browser_Operator_Plan.md`
- Dual-Mode Architecture & UX: `/storage/emulated/0/Agent_Graph/ObsidianVault/Oberon_AOA_Dual_Mode_Architecture_and_UX_Specification.md`
- Task Execution Modalities: `/storage/emulated/0/Agent_Graph/ObsidianVault/Oberon_AOA_Task_Execution_Modalities_OnPage_vs_EndToEnd.md`
- Dual-Engine Separation: `/storage/emulated/0/Agent_Graph/ObsidianVault/Oberon_Agent_Platform_Dual_Engine_Separation.md`
- Live Preview & Self-Healing Pipeline: `/storage/emulated/0/Agent_Graph/ObsidianVault/Oberon_Core_Preview_Modes_and_CLI_Self_Healing_Pipeline.md`

---

## Global Constraints

- **Localhost Security:** Agent Bridge listens strictly on `127.0.0.1:8765` with mandatory `X-Agent-Token: oberon_local_secret_token` header check.
- **Zero Regression:** Existing `audit` and `scroll` QA functionality in `BrowserViewModel.kt` must remain fully operational without breaking changes.
- **Shared Hardware Layer:** Virtual cursor (`AgentVirtualCursor.kt`) and draggable capsule (`AgentHudBar.kt`) must be reused by both engines without duplicate UI components.
- **APK Footprint:** Keep APK ultra-lightweight (3–5 MB); avoid embedding heavy LLM weights inside Android APK. Cognition runs via loopback bridge.
- **Platform Compatibility:** Android 7.0+ (minSdk 24, targetSdk 36) and Termux/Linux Python 3 environment.

---

## Review Focus

1. **DOM Extraction Incompleteness:** Complex web pages with shadow DOM, nested iframes, or non-standard custom elements might fail basic query selection. The extractor must fallback gracefully to `elementFromPoint` or visible text queries.
2. **Concurrent Request Race Conditions:** Simultaneous calls to `POST /audit` and `POST /operate` must be prevented by atomic engine locking (mutual exclusion).
3. **Form Submission Interception:** Submitting forms during automated journeys could trigger external navigation before the verification step can capture page state. Navigation listeners must maintain state across redirects.
4. **App Frame Viewport Clipping:** When App Preview mode (390×844 dp) is enabled on smaller mobile screens, the device frame must scale responsively without clipping content or hiding the draggable capsule.
5. **Slow Network / Lazy Loaded Elements:** Semantic DOM map must support dynamic polling / timeout retry if target elements are rendered asynchronously via React/Vue.

---

### Task 1: Semantic DOM Perception & Element Map Extractor (`/extract_map`)

**Files:**
- Modify: `app/src/main/java/com/example/agent/AgentModels.kt`
- Modify: `app/src/main/java/com/example/viewmodel/BrowserViewModel.kt`
- Modify: `app/src/main/java/com/example/agent/AgentServer.kt`
- Test: `app/src/test/java/com/example/SemanticElementMapTest.kt`

**Interfaces:**
- Consumes: `executeJavaScriptAsync(script: String)` from `BrowserViewModel.kt`
- Produces: `suspend fun agentExtractSemanticMap(): JSONObject` returning `{ "inputs": [...], "buttons": [...], "headings": [...], "url": "..." }`
- Produces: HTTP endpoint `POST /extract_map` in `AgentServer.kt`

- [ ] **Step 1: Write the failing unit test for SemanticElementMap data parsing**
  Create `app/src/test/java/com/example/SemanticElementMapTest.kt` validating JSON serialization of semantic interactive element models (inputs with label association, button intent classification, and normalized viewport coordinates).

- [ ] **Step 2: Run test to verify it fails**
  Run: `python3 -c "import json; assert 'SemanticElementMap' in open('app/src/main/java/com/example/agent/AgentModels.kt').read()"`
  Expected: FAIL (model not yet defined).

- [ ] **Step 3: Define Semantic Element Models in `AgentModels.kt`**
  Add:
  - `data class SemanticInputElement(val selector: String, val label: String, val placeholder: String, val type: String, val xRatio: Float, val yRatio: Float)`
  - `data class SemanticButtonElement(val selector: String, val text: String, val role: String, val xRatio: Float, val yRatio: Float)`
  - `data class SemanticElementMap(val url: String, val title: String, val inputs: List<SemanticInputElement>, val buttons: List<SemanticButtonElement>, val isAtBottom: Boolean)`

- [ ] **Step 4: Implement `agentExtractSemanticMap()` in `BrowserViewModel.kt`**
  Inject JavaScript that traverses the active DOM, associates `<label for="...">` and wrapping labels with `<input>`, extracts placeholders, aria-labels, and calculates center `xRatio`/`yRatio` for each element.

- [ ] **Step 5: Expose `extract_map` action in `AgentServer.kt`**
  Handle `"extract_map"` in `AgentServer.handleCommand`: call `viewModel.agentExtractSemanticMap()` and return formatted JSON response.

- [ ] **Step 6: Run verification test**
  Verify model compilation and JSON serialization consistency.

- [ ] **Step 7: Commit**
  ```bash
  git add app/src/main/java/com/example/agent/AgentModels.kt app/src/main/java/com/example/viewmodel/BrowserViewModel.kt app/src/main/java/com/example/agent/AgentServer.kt
  git commit -m "feat(agent): implement semantic DOM element extractor and /extract_map endpoint"
  ```

---

### Task 2: Dual-Engine Isolation & In-App AOA Prompt Bar UI

**Files:**
- Modify: `app/src/main/java/com/example/agent/AgentModels.kt`
- Modify: `app/src/main/java/com/example/viewmodel/BrowserViewModel.kt`
- Modify: `app/src/main/java/com/example/ui/components/AgentHudBar.kt`
- Modify: `app/src/main/java/com/example/agent/AgentServer.kt`

**Interfaces:**
- Consumes: `_agentStatus`, `_agentCursorState` in `BrowserViewModel.kt`
- Produces: `enum class AgentEngineType { QA_AUDITOR, AOA_OPERATOR }`
- Produces: `fun runAutonomousOperatorGoal(goal: String)` in `BrowserViewModel.kt`
- Produces: Expandable AOA prompt text bar and engine badge indicator `[AUDIT]` vs `[AOA]` in `AgentHudBar.kt`

- [ ] **Step 1: Write test for Dual-Engine State Isolation**
  Ensure setting active engine to `AOA_OPERATOR` prevents simultaneous execution of `QA_AUDITOR` stress loops.

- [ ] **Step 2: Add `AgentEngineType` and Operator Status to `AgentModels.kt`**
  Add:
  - `enum class AgentEngineType { QA_AUDITOR, AOA_OPERATOR }`
  - Update `AgentModeStatus` with `OPERATOR_THINKING`, `OPERATOR_ACTING`, `OPERATOR_VERIFYING`.

- [ ] **Step 3: Add Operator State & Dispatcher in `BrowserViewModel.kt`**
  - Add `_activeEngine = MutableStateFlow(AgentEngineType.QA_AUDITOR)`
  - Add `fun runAutonomousOperatorGoal(goal: String)` that sets engine to `AOA_OPERATOR`, emits thinking status to `_agentCursorState`, and prepares ReAct step execution.
  - Implement mutual exclusion guard: reject `runAutonomousButtonAudit()` if operator is active, and vice versa.

- [ ] **Step 4: Update `AgentHudBar.kt` with In-App AOA Prompt Bar**
  - Add badge toggle: `[AUDIT]` (Green/Cyan) vs `[AOA]` (Purple/Amber).
  - Add minimalist input box in expanded sheet:
    `"Ask AOA to operate on this page..."` with Send button.
  - Connect text submission to `onDispatchOperatorGoal: (String) -> Unit`.

- [ ] **Step 5: Wire `action: "operate"` in `AgentServer.kt`**
  Handle `"operate"` command with `"goal"` string parameter, launching `viewModel.runAutonomousOperatorGoal(goal)`.

- [ ] **Step 6: Verify Compose UI layout rendering and build consistency**
  Inspect `AgentHudBar.kt` to ensure draggable physics and keyboard IME spacing remain unhindered.

- [ ] **Step 7: Commit**
  ```bash
  git add app/src/main/java/com/example/agent/AgentModels.kt app/src/main/java/com/example/viewmodel/BrowserViewModel.kt app/src/main/java/com/example/ui/components/AgentHudBar.kt app/src/main/java/com/example/agent/AgentServer.kt
  git commit -m "feat(ui): add dual-engine isolation and expandable AOA prompt bar to Agent HUD"
  ```

---

### Task 3: Dual Viewport App Frame & Device Mockup Mode

**Files:**
- Modify: `app/src/main/java/com/example/viewmodel/BrowserViewModel.kt`
- Modify: `app/src/main/java/com/example/ui/screens/WebScreen.kt`
- Modify: `app/src/main/java/com/example/ui/components/ChromeTopBar.kt`

**Interfaces:**
- Consumes: `isAppPreviewMode: StateFlow<Boolean>` in `BrowserViewModel.kt`
- Produces: Simulated 390×844 dp device mockup canvas container with rounded bezel, subtle drop shadow, and auto-hidden Omnibox for pure mobile app experience.

- [ ] **Step 1: Add App Preview framing state in `BrowserViewModel.kt`**
  Expose `_isPreviewAppFrame = MutableStateFlow(false)` and `fun setPreviewMode(isApp: Boolean)` controlling viewport frame simulation.

- [ ] **Step 2: Update `WebScreen.kt` with Dynamic Device Mockup Container**
  - When `isPreviewAppFrame` is `true`:
    Wrap WebView in a centered mobile canvas container:
    `Modifier.widthIn(max = 412.dp).aspectRatio(9f / 19.5f).clip(RoundedCornerShape(28.dp)).border(2.dp, Color(0xFF334155), RoundedCornerShape(28.dp))`
  - When `false`: Standard edge-to-edge responsive web canvas.

- [ ] **Step 3: Update `ChromeTopBar.kt` for App Preview Mode**
  In App Preview mode, collapse the omnibox to a minimal floating notch or hide it entirely to give Flutter Web/PWA full-screen native mobile fidelity.

- [ ] **Step 4: Verify layout responsiveness on mobile screens**
  Ensure container does not overflow on smaller screens by applying `fillMaxSize()` with constraints.

- [ ] **Step 5: Commit**
  ```bash
  git add app/src/main/java/com/example/ui/screens/WebScreen.kt app/src/main/java/com/example/ui/components/ChromeTopBar.kt app/src/main/java/com/example/viewmodel/BrowserViewModel.kt
  git commit -m "feat(preview): implement 390x844 simulated mobile device frame for App Preview Mode"
  ```

---

### Task 4: Antigravity CLI AOA ReAct Operator Harness (`oberon_operator.py`)

**Files:**
- Create: `scripts/oberon_operator.py`
- Create: `tests/test_oberon_operator.py`

**Interfaces:**
- Consumes: Oberon HTTP Bridge endpoints (`/extract_map`, `/click`, `/type`, `/scroll`, `/navigate`, `/set_preview_mode`)
- Produces: `OberonOperator` class with `run_goal(goal: str)` implementing the autonomous Observe-Think-Act-Verify loop.
- Produces: Intent classifier routing tasks into **Modality 1: On-Page Instant Action** vs **Modality 2: Full End-to-End Journey**.

- [ ] **Step 1: Write failing unit test for `OberonOperator` ReAct loop**
  Create `tests/test_oberon_operator.py` with mock bridge server verifying:
  - Task modality classification (on-page vs navigation).
  - Semantic element matching (finding input by label/placeholder).
  - Human typing pacing simulation (30-80ms per character).
  - Step verification.

- [ ] **Step 2: Run test to verify it fails**
  Run: `python3 tests/test_oberon_operator.py`
  Expected: FAIL (module `scripts.oberon_operator` does not exist).

- [ ] **Step 3: Implement `OberonOperator` in `scripts/oberon_operator.py`**
  - Implement `OberonClient`: HTTP communication with `127.0.0.1:8765`, token authentication, and auto-wake fallback.
  - Implement `classify_modality(goal: str) -> str`: Detects explicit navigation targets (`http`, `www`, `open`, `go to`) vs contextual actions.
  - Implement `match_target_element(goal_step, semantic_map)`: Finds best matching input field or button.
  - Implement `execute_action(action)`: Dispatches smooth glide, human-rhythm typing, and pulsing clicks.
  - Implement `verify_step(previous_state, expected_outcome)`.

- [ ] **Step 4: Run unit test to verify it passes**
  Run: `python3 tests/test_oberon_operator.py`
  Expected: PASS (All test assertions succeed).

- [ ] **Step 5: Add CLI entry point**
  Support running directly from Termux / Linux:
  ```bash
  python3 scripts/oberon_operator.py --goal "Fill contact form with John Doe and submit"
  ```

- [ ] **Step 6: Commit**
  ```bash
  git add scripts/oberon_operator.py tests/test_oberon_operator.py
  git commit -m "feat(cli): create Antigravity AOA ReAct operator harness and intent classifier"
  ```

---

### Task 5: Autonomous Developer Watcher Daemon & MCP Server Upgrade

**Files:**
- Create: `scripts/oberon_watch_daemon.py`
- Modify: `/root/oberon-browser/mcp_servers/oberon_browser_mcp/server.py`
- Create: `tests/test_watch_daemon.py`

**Interfaces:**
- Consumes: Local dev server URL (e.g., `http://localhost:3000`), Oberon Bridge `/get_telemetry` and `/audit`.
- Produces: MCP Tools: `oberon_operate`, `oberon_extract_map`, `oberon_watch`.
- Produces: Closed-loop self-healing bug report generator in `oberon_watch_daemon.py`.

- [ ] **Step 1: Write test for Self-Healing Telemetry Report generation**
  Create `tests/test_watch_daemon.py` testing telemetry parsing: converts `ANTIGRAVITY_AGENT_ERROR` payloads into actionable AI agent fix prompts.

- [ ] **Step 2: Run test to verify it fails**
  Run: `python3 tests/test_watch_daemon.py`
  Expected: FAIL.

- [ ] **Step 3: Implement `oberon_watch_daemon.py`**
  - Connects to local dev server and Oberon Bridge.
  - Polls file changes or listens for CLI hot-reload events.
  - Dispatches `POST /navigate` to reload the preview frame.
  - Triggers `POST /audit` (QA engine).
  - Fetches `POST /get_telemetry`: if broken elements or runtime exceptions occur, formats structured Markdown bug report with filename, line number, and stack trace.

- [ ] **Step 4: Upgrade MCP Bridge Server in `mcp_servers/oberon_browser_mcp/server.py`**
  Add tool definitions:
  - `oberon_extract_map`: Returns semantic DOM tree with inputs, buttons, and coordinates.
  - `oberon_operate`: Dispatches natural language goal to AOA Operator engine.

- [ ] **Step 5: Run unit tests to verify they pass**
  Run: `python3 tests/test_watch_daemon.py`
  Expected: PASS.

- [ ] **Step 6: Commit**
  ```bash
  git add scripts/oberon_watch_daemon.py tests/test_watch_daemon.py /root/oberon-browser/mcp_servers/oberon_browser_mcp/server.py
  git commit -m "feat(mcp): add oberon_operate tool and closed-loop developer self-healing daemon"
  ```

---

### Task 6: End-to-End Integration, CI Pipeline & Knowledge Vault Sync

**Files:**
- Modify: `.github/workflows/build-apks.yml`
- Create: `scripts/verify_oberon_pipeline.py`
- Update: `/storage/emulated/0/Agent_Graph/ObsidianVault/Oberon_Browser_Master_Architecture.md`

**Interfaces:**
- Consumes: All components from Tasks 1–5.
- Produces: Automated verification report and new compiled Oberon-2 APK via GitHub Actions.

- [ ] **Step 1: Create End-to-End Verification Script (`scripts/verify_oberon_pipeline.py`)**
  Automates end-to-end sanity check:
  - Checks Bridge HTTP socket responses (mock or live).
  - Tests semantic extractor payload format.
  - Verifies Dual-Engine Mutual Exclusion lock.
  - Checks MCP tool definitions schema.

- [ ] **Step 2: Run the verification suite**
  Run: `python3 scripts/verify_oberon_pipeline.py`
  Expected: PASS with 100% components verified.

- [ ] **Step 3: Trigger GitHub Actions CI build**
  Push changes to `main` branch on `oberon-2` to trigger APK build:
  ```bash
  git push origin main
  ```
  Verify CI starts compiling debug and release APKs.

- [ ] **Step 4: Update Obsidian Knowledge Vault**
  Synchronize `Oberon_Browser_Master_Architecture.md` with implementation status, linking the plan and new scripts.

- [ ] **Step 5: Commit & Final Wrap**
  ```bash
  git add scripts/verify_oberon_pipeline.py
  git commit -m "chore: complete end-to-end verification suite and sync master architecture"
  ```
