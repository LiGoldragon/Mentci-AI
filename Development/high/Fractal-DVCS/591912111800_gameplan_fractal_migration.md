# Gameplan: Fractal DVCS Migration & Orchestration

## Phase 1: Saṃskāra Implementation (Bridge & Registry)
Goal: Build the Rust logic to ingest `jj` state and maintain the `FlowRegistry`.

- [ ] **1.1. Ingestion Engine:** Create a Rust utility in `Components/samskara` that:
    - Executes `jj log --template @Components/samskara/templates/samskarize_jj.tmpl`.
    - Parses the JSON output.
- [ ] **1.2. SQLite Integration:** Implement the database adapter for `.mentci/flow_registry.db`.
    - Table synchronization (Logic to match `FlowRegistry` schema).
    - Atomic updates for flow status (`in-progress` -> `success`).
- [ ] **1.3. Verification Hook:** Create a tool `samskara_verify` that runs `cargo check` or tests in a specific component repo and updates the registry.

## Phase 2: Systematic Component Splitting
Goal: Convert `Components/*` into independent `jj` repositories.

- [ ] **2.1. Migration Script:** Author a Rust actor to:
    - Iterate through `Components/`.
    - `jj git init` each directory.
    - Register the initial state in `FlowRegistry`.
- [ ] **2.2. Root Repo Realignment:** 
    - Convert `Components/` in the main repo into a directory of "pointers" or managed subtrees.
    - Establish the `.jjconfig` for colocated or external repo management.

## Phase 3: Flow Automation (`mentci-flow`)
Goal: Simplify the `UidHash__TypeIntent__Subflow` protocol for agents.

- [ ] **3.1. CLI Tool:** Create `mentci-flow` (Rust) to handle:
    - `mentci-flow start <intent>`: Generates UID and creates the bookmark.
    - `mentci-flow list`: Shows active flows from SQLite with status.
    - `mentci-flow merge <uid>`: Triggers the Auto-Merge protocol.
- [ ] **3.2. Skill Integration:** Update `.pi/skills/independent-developer/SKILL.md` to prioritize `mentci-flow` over manual bookmarking.

## Phase 4: Auto-Merge Protocol Implementation
Goal: Realize the cross-repo rebase/merge logic.

- [ ] **4.1. Orchestrator Logic:** Implement the "Auto-Merge" state machine in Saṃskāra.
    - Parent flow discovery.
    - Sequential rebase logic.
    - Conflict projection (Output conflict state as an actionable ticket).
- [ ] **4.2. Testing:** 
    - Mock a multi-component conflict.
    - Verify Saṃskāra correctly identifies and reports the conflict plane.

## Phase 5: Refinement & Flight Recorder
- [ ] **5.1. Operation History:** Standardize `.mentci/op_history.txt` as a native Saṃskāra output.
- [ ] **5.2. UI Mirror Integration:** Ensure `FlowRegistry` status is visible in the Mirror Hook.
