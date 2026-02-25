# Agent Report

- Chronography: `12.7.42.00 | 5919 AM`
- Kind: `answer`
- Change Scope: `no-files`
- Subject: `Mentci-Project-Survey`
- Title: `project-survey-and-author-suggestions`

## Prompt

survey the project and create a report with suggestions and questions for the author

## Agent Answer

### 1. Project Survey & Current State Analysis
I have surveyed the Mentci-AI repository architecture, High-Level Goals, Restart Context, and recent architectural shifts. The project is undergoing a massive stabilization phase moving from legacy scripting to a hardened, Rust-native, mathematically pure "Dark Factory" (Level 5 autonomous pipeline).

**Key Achievements (Stable Foundations):**
*   **The Orchestration Layer:** The `execute` utility (`Components/mentci-aid/src/bin/execute.rs`) has successfully transitioned from Babashka/Clojure to native Rust using the `ractor` actor framework. It currently handles critical guard protocols (`root-guard`, `session-guard`, etc.) and session finalization (`.mentci/session.json`).
*   **Philosophical Mandate:** The repository now explicitly rejects manipulative AI governance and embraces the "Local Machine of Fit," establishing the architectural justification for the Russian Doll isolation model and Jujutsu state separation.
*   **Lojix/Aski/EDN Ascension:** Natural language checklists have been deprecated. The `pi` agent subagent flow now demands structured `lojix` (EDN Next) DAGs for agent-to-agent communication, maximizing context token density.
*   **Component Modularity:** Core logic is properly isolated in crates (`mentci-aid`, `mentci-box`, `mentci-launch`, `mentci-fs`, `chronos`).

**Current Frictions & Gaps:**
*   **Goal 1 Obsolescence:** `Goal 1` in `HIGH_LEVEL_GOALS.md` still demands an "Attractor DOT Job Handoff." However, recent research (`Sema-Subagent-Flow`) concluded that Node.js/TypeScript Attractor is incompatible with the Russian Doll model and should be deprecated in favor of native EDN/Rust actor orchestration. 
*   **Mentci-Launch Integration:** While `Components/mentci-launch/` exists as a crate and builds systemd/foot launch plans, it is not yet fully wired into the `execute` command routing as dictated by the active `Mentci-Launch` strategy.
*   **Mentci-Aid is Offline:** The master daemon `mentci-aid` is currently marked as "Experimental / not in running state" in the Restart Context. The orchestration is currently manual/semi-autonomous via `pi` rather than supervised by the daemon.

### 2. Suggestions for the Author (Li Goldragon)

Based on the survey and the philosophical alignment of the project, I propose the following strategic implementations:

**Suggestion 1: Formalize Goal 1 Deprecation & Replace with Lojix Handoff**
Since we established that `Attractor` (in its current Node.js state) is incompatible with the nested Nix jail subagent spawning required for single-ownership, we should modify `Core/HIGH_LEVEL_GOALS.md`. We should replace the "Attractor DOT Job Handoff" with a **"Lojix Sub-Flow Handoff"**. The goal should be for `mentci-aid` to successfully parse a `lojix` DAG and spawn a `mentci-box` for a subagent.

**Suggestion 2: Implement `mentci-jj subagent-spawn` Wrapper**
Currently, the `@task` subagent is instructed to simulate a sub mentci-box by running `jj new -m "intent: [Task]"`. To truly enforce the "Russian Doll" architecture, we should create a dedicated command (e.g., `execute subagent-spawn <task-id>`) that automatically:
1. Creates the isolated Jujutsu bookmark.
2. Generates the `lojix` payload sidecar for that specific task.
3. Spawns the nested `mentci-box` Nix shell bound specifically to that bookmark.

**Suggestion 3: `mentci-schema-read` Context Tool**
To help the `@explore` subagent respect Cap'n Proto boundaries without blasting its context window with massive `.capnp` binary protocol files, we should build a small Rust tool that reads `.capnp` files and outputs highly condensed `lojix` signatures (essentially just the struct names and field types).

### 3. Questions for the Author

1.  **The Fate of Attractor:** Now that we are orchestrating via Lojix DAGs and `ractor`, should we completely delete the `Sources/attractor` and `Sources/attractor-docs` references, or do you still foresee porting its visual/UI components to Rust in the future?
2.  **Mentci-Aid Daemon Lifecycle:** What is the specific trigger you envision for `mentci-aid` to wake up from its "not in running state"? Should the daemon be running continuously in the background using `mentci-launch` (service mode), waiting for a `.mentci/intent.edn` file to drop, or will it remain a CLI-triggered tool for now?
3.  **Lojix Syntax Parser in Rust:** The current `execute` binary relies on `edn-rs`. Does `edn-rs` fully satisfy your vision for `Lojix` (EDN Next), or do we need to implement a custom parser (`Components/lojix-parse/`) that handles the specific Askii/Shen-inspired symbol density you envision?

## Reporting Protocol Notes

- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.