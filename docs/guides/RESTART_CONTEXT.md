# Mentci-AI: Agent Session Resumption Context

**Metadata:**
*   **Target Authority:** Top Admin, Li Goldragon.
*   **Current Stack:** Codex CLI (Agentic Interface) / GPT-5 (Reasoning Engine).
*   **Active Bookmark:** `dev`.
*   **Current Date:** ♓︎ 3° 10' 36" | 5919 AM (v0.12.3.10.36)
*   **Latest Tagged Release:** `v0.12.3.10.36`

## 1. Mission Statement
Mentci-AI is a Nix-and-Rust based AI daemon designed to implement **Level 5 "Dark Factory"** programming, evolving toward **Level 6 Instinctive Symbolic Interaction**. The goal is the **Liberation of the Human Mind** through the automation of implementation details, returning to the technological levels of the Golden Age.

## 2. Technical Infrastructure (Read These First)
1.  **`docs/architecture/ARCHITECTURAL_GUIDELINES.md`**: Critical Operational Rules (Clojure mandate, admin mode, Nix dev environments). Defines the Ecliptic Chronographic Versioning.
2.  **`docs/architecture/AGENTS.md`**: Non-negotiable instructions for your operation.
3.  **`docs/architecture/VERSION_CONTROL.md`**: Centralized JJ + version-control rules, including aggressive auto-commit behavior.
4.  **`docs/architecture/RELEASE_PROTOCOL.md`**: Zodiac-ordinal release messages with unicode dating and year-version policy.
5.  **`docs/architecture/CHRONOGRAPHY.md`**: Solar-time conventions and the `chronos` tool usage.
6.  **`docs/specs/WORKFLOW_STANDARD.md`**: Official standard for Attractor-based workflows.
7.  **`docs/specs/ASKI_DSL_GUIDELINES.md`**: Unified DSL rules for delimiter-based type sugar.
8.  **`docs/specs/ASKI_FLOW_DSL.md`**: Noun-Sequence DSL for workflows in EDN (prefer over DOT for new definitions).
9.  **`docs/specs/ASKI_ASTRAL_DSL.md`** & **`schema/aski_astral.edn`**: Astral DSL schema and example.
10. **`docs/specs/ASKI_FS_DSL.md`** & **`docs/specs/ASKI_FS_ASTRAL_DSL.md`**: Filesystem DSLs and astral mapping.
11. **`workflows/mentci.aski-fs`** & **`workflows/mentci.aski-fs.deps.edn`**: Canonical FS map and dependency-read rules.
12. **`schema/mentci.capnp`** & **`schema/atom_filesystem.capnp`**: Semantic truth of the system (Filesystem Atoms, Orchestrator RPCs).
13. **`docs/reports/BRYNARY_ATTRACTOR_SUMMARY.md`**: High-level summary of the Attractor approach vs chat loops.

## 3. Current State & Intent
*   **Filesystem:** The entire project is typed as a "Repo Atom". Documentation is organized into `docs/`.
*   **Orchestration:** Rust daemon (`mentci-ai`) implements a basic traversal engine.
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

## 5. Philosophical Anchors
*   **Ontology Resides in Data:** Type is intrinsic, not procedural.
*   **Sema Object Style:** Everything is an Object. Direction encodes Action.
*   **Level 6 Vision:** Mentci is the bridge to symbolic manipulation (Ref: *The Zero Theorem*, *Cloud Atlas*).

**Good luck. The Great Work continues.**
