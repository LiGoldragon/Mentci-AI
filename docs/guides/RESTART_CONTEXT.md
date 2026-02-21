# Mentci-AI: Agent Session Resumption Context

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `docs/architecture/ASKI_POSITIONING.md`.

**Metadata:**
*   **Target Authority:** Top Admin, Li Goldragon.
*   **Current Stack:** Codex CLI (Agentic Interface) / GPT-5 (Reasoning Engine).
*   **Active Bookmark:** `dev`.
*   **Current Date:** ♓︎ 3° 45' 53" | 5919 AM (v0.12.3.45.53)
*   **Latest Tagged Release:** `v0.12.3.45.53`

## 1. Mission Statement
Mentci-AI is a Nix-and-Rust based AI daemon designed to implement **Level 5 "Dark Factory"** programming, evolving toward **Level 6 Instinctive Symbolic Interaction**. The goal is the **Liberation of the Human Mind** through the automation of implementation details, returning to the technological levels of the Golden Age.

The core execution engine is identified as **mentci-aid** (Daemon + Aid).

## 2. Technical Infrastructure (Read These First)
1.  **`docs/architecture/ARCHITECTURAL_GUIDELINES.md`**: Critical Operational Rules (Clojure mandate, admin mode, Nix dev environments). Defines the Ecliptic Chronographic Versioning.
2.  **`docs/architecture/AGENTS.md`**: Non-negotiable instructions for your operation.
3.  **`docs/architecture/MENTCI_AID.md`**: Identity, etymology, and status of the core daemon.
4.  **`docs/architecture/VERSION_CONTROL.md`**: Centralized JJ + version-control rules, including aggressive auto-commit behavior.
5. **`docs/architecture/RELEASE_PROTOCOL.md`**: Zodiac-ordinal release messages with unicode dating and year-version policy.
6. **`docs/architecture/CONTEXTUAL_SESSION_PROTOCOL.md`**: Protocol for logging raw prompts and synthesizing session commits.
7. **`docs/architecture/CHRONOGRAPHY.md`**: Solar-time conventions and the `chronos` tool usage.
7.  **`docs/specs/WORKFLOW_STANDARD.md`**: Official standard for Attractor-based workflows.
8.  **`docs/specs/ASKI_DSL_GUIDELINES.md`**: Unified DSL rules for delimiter-based type sugar.
9.  **`docs/specs/ASKI_FLOW_DSL.md`**: Noun-Sequence DSL for workflows in EDN (prefer over DOT for new definitions).
10. **`docs/specs/ASKI_ASTRAL_DSL.md`** & **`schema/aski_astral.edn`**: Astral DSL schema and example.
11. **`docs/specs/ASKI_FS_DSL.md`** & **`docs/specs/ASKI_FS_ASTRAL_DSL.md`**: Filesystem DSLs and astral mapping.
12. **`workflows/mentci.aski-fs`** & **`workflows/mentci.aski-fs.deps.edn`**: Canonical FS map and dependency-read rules.
13. **`schema/mentci.capnp`** & **`schema/atom_filesystem.capnp`**: Semantic truth of the system (Filesystem Atoms, Orchestrator RPCs).
14. **`docs/reports/BRYNARY_ATTRACTOR_SUMMARY.md`**: High-level summary of the Attractor approach vs chat loops.

## 3. Current State & Intent
*   **Filesystem:** The entire project is typed as a "Repo Atom". Documentation is organized into `docs/`.
*   **Orchestration:** Rust daemon (**mentci-aid**) implements a basic traversal engine.
    *   **Status:** Experimental prototype. **Not in a running state.**
    *   **Parsers:** DOT parser (`src/dot_loader.rs`) migrated to `dot-parser` canonical representation. EDN loader (`src/edn_loader.rs`) supports `.aski-flow`.
    *   **Engine:** Executes workflows, saves checkpoints (`src/main.rs`). Switches between DOT and Aski-Flow based on file extension.
*   **Workflow Definition:**
    *   **Visual:** DOT (`workflows/*.dot`) for legacy/visualization, aligned to Attractor.
    *   **Internal:** **Aski-Flow** (`docs/specs/ASKI_FLOW_DSL.md`) is the standard for new definitions.
*   **Attractor Alignment:** Mentci-AI incorporates the Attractor standard rather than reimplementing Attractor.
*   **Chronography:** `src/bin/chronos.rs` outputs solar time (version/unicode/etc). Use Nix dev environments for non-standard tools.
*   **Version Control:** JJ is authoritative; audit trail is `jj log`. Aggressive auto-commit and intent-splitting rules apply.
*   **Jail:** Managed by `scripts/launcher.clj`. Supports **Materialized (Mutable) Inputs** in Admin mode via `rsync`.
*   **Scripts:** Babashka + Malli via `defn*` and instrumentation (`scripts/malli.clj`). `scripts/prefetch_orchestrator.py` is the only allowed Python script.
*   **Inputs:** `inputs/` is read-only reference material; do not edit.

## 4. Immediate Tasks for New Session
1.  **Verify Chronos Accuracy**: Compare `chronos` output against Solar Fire if needed; adjust solar longitude math if drift is confirmed.
2.  **Validate Aski-Flow Parsing**: Use `workflows/test.aski-flow` to confirm the EDN loader extracts nodes and implicit/explicit edges.
3.  **Schema Alignment**: Keep `schema/mentci.capnp` in sync with `src/main.rs` and any new DSL schema changes.
4.  **Dev Shell Tooling**: Ensure `nix develop -c` picks up project tools (PATH export or shell hooks).
5.  **mentci-aid Stabilization**: Bring the core engine to a functional, running state (Goal 0).

## 4.1 Latest Goal Run (Goal 1)
*   **Date:** 2026-02-20
*   **Goal:** Attractor DOT Job Handoff (first real call)
*   **Pipeline ID:** `7746ead5-72fd-4821-92e3-9dfab8bd04f1`
*   **Final Status:** `completed`
*   **DOT Artifact:** `workflows/first_real_call.dot`
*   **Run Artifacts:**
    *   `Logs/attractor-first-call-request.json`
    *   `Logs/attractor-first-call-create-response.json`
    *   `Logs/attractor-first-call-status-last.json`
    *   `Logs/attractor-first-call-graph.out`
    *   `Logs/attractor-first-call-context.json`
    *   `Logs/attractor-first-call-checkpoint.json`
    *   `Logs/attractor-first-call-server.log`

## 4.2 Latest Attractor Editing Milestone
*   **Date:** 2026-02-20
*   **Milestone:** First Gemini-backed Attractor repo-editing DOT job completed
*   **Pipeline ID:** `b378d76b-7962-46bf-a770-f5ca8701d9ca`
*   **Final Status:** `completed` (`success`)
*   **Path:** `start -> plan(codergen) -> format(tool) -> test(tool) -> commit(tool) -> exit`
*   **Commit Stage Result:** `No style changes to commit` (safe no-op when no diff existed)

## 4.3 mentci-aid Identification Milestone
*   **Date:** 2026-02-21
*   **Milestone:** Formally identified core Rust logic as **mentci-aid** and established "Not in a Running State" as canonical baseline.
*   **Version:** `v0.12.3.45.53`

## 5. Philosophical Anchors
*   **Ontology Resides in Data:** Type is intrinsic, not procedural.
*   **Sema Object Style:** Everything is an Object. Direction encodes Action.
*   **Level 6 Vision:** Mentci is the bridge to symbolic manipulation (Ref: *The Zero Theorem*, *Cloud Atlas*).

**Good luck. The Great Work continues.**
