# Mentci-AI: Agent Session Resumption Context

**Metadata:**
*   **Target Authority:** Top Admin, Li Goldragon.
*   **Current Stack:** OpenCode (Agentic Interface) / DeepSeek-V4 (Reasoning Engine).
*   **Active Bookmark:** `dev`.
*   **Current Date:** ♓︎ 1° 28' 44" | 5919 AM (v0.12.1.28.44)

## 1. Mission Statement
Mentci-AI is a Nix-and-Rust based AI daemon designed to implement **Level 5 "Dark Factory"** programming, evolving toward **Level 6 Instinctive Symbolic Interaction**. The goal is the **Liberation of the Human Mind** through the automation of implementation details, returning to the technological levels of the Golden Age.

## 2. Technical Infrastructure (Read These First)
1.  **`ARCHITECTURAL_GUIDELINES.md`**: Section 0 contains the Critical Operational Rules (Jail Mandate, Clojure Mandate, Logging Protocol, Admin Mode). Section 8 defines the Ecliptic Chronographic Versioning.
2.  **`AGENTS.md`**: Non-negotiable instructions for your operation.
3.  **`TOOLS.md`**: Documentation of the tool stack (Clojure, EDN, Nix, Jet).
4.  **`schema/mentci.capnp`** & **`schema/atom_filesystem.capnp`**: The semantic truth of the system (Filesystem Atoms, Orchestrator RPCs).
5.  **`reports/attractor_gap_analysis.md`**: Comprehensive analysis of what Mentci-AI is currently lacking compared to the Attractor specs.

## 3. Current State & Intent
*   **Filesystem:** The entire project is typed as a "Repo Atom".
*   **Jail:** A pure Nix Jail is active with native `__structuredAttrs`. Control is handed to `scripts/launcher.clj`.
*   **Logging:** Handshake Logging Protocol is active using EDN format (`Logs/*.edn`).
*   **Engine:** Initial Rust daemon implemented in `src/main.rs`. It features an `ExecutionEnvironment` abstraction and a `PipelineEngine` based on the Attractor specification.
*   **Versioning:** Ecliptic Chronographic Versioning (Section 8) is documented and enforced. Current version: 0.12.1.13.

## 4. Immediate Tasks for New Session
1.  **Engine Expansion:** Implement the `Codergen` and `Wait.human` handlers in `src/main.rs`.
2.  **Schema Completion:** Fully map the Attractor DAG structures to Cap'n Proto RPCs in `schema/mentci.capnp`.
3.  **Validation:** Implement the Attractor diagnostic/linting matrix within the Rust daemon.
4.  **Major Release:** Finalize all Level 5 components in preparation for the transition to ♈︎ 1.1.1 (5920 Anno Mundi).

## 5. Philosophical Anchors
*   **Ontology Resides in Data:** Type is intrinsic, not procedural.
*   **Sema Object Style:** Everything is an Object. Direction encodes Action.
*   **Level 6 Vision:** Mentci is the bridge to symbolic manipulation (Ref: *The Zero Theorem*, *Cloud Atlas*).

**Good luck. The Great Work continues.**
