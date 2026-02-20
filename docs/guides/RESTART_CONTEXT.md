# Mentci-AI: Agent Session Resumption Context

**Metadata:**
*   **Target Authority:** Top Admin, Li Goldragon.
*   **Current Stack:** OpenCode (Agentic Interface) / DeepSeek-V4 (Reasoning Engine).
*   **Active Bookmark:** `dev`.
*   **Current Date:** ♓︎ 1° 28' 44" | 5919 AM (v0.12.1.28.44)

## 1. Mission Statement
Mentci-AI is a Nix-and-Rust based AI daemon designed to implement **Level 5 "Dark Factory"** programming, evolving toward **Level 6 Instinctive Symbolic Interaction**. The goal is the **Liberation of the Human Mind** through the automation of implementation details, returning to the technological levels of the Golden Age.

## 2. Technical Infrastructure (Read These First)
1.  **`docs/architecture/ARCHITECTURAL_GUIDELINES.md`**: Section 0 contains the Critical Operational Rules (Jail Mandate, Clojure Mandate, Logging Protocol, Admin Mode). Section 8 defines the Ecliptic Chronographic Versioning.
2.  **`docs/architecture/AGENTS.md`**: Non-negotiable instructions for your operation.
3.  **`docs/architecture/TOOLS.md`**: Documentation of the tool stack (Clojure, EDN, Nix, Jet).
4.  **`schema/mentci.capnp`** & **`schema/atom_filesystem.capnp`**: The semantic truth of the system (Filesystem Atoms, Orchestrator RPCs).
5.  **`docs/reports/attractor_gap_analysis.md`**: Progress tracking against the Attractor specifications.
6.  **`docs/specs/ATTRACTOR_DOT_REFERENCE.md`**: Schema reference for agent execution graphs.

## 3. Current State & Intent
*   **Filesystem:** The entire project is typed as a "Repo Atom". Documentation is organized into `docs/`.
*   **Orchestration:** Rust daemon (`mentci-ai`) now supports **DOT-based workflow loading** and **Checkpoint/Resume** state persistence.
*   **Engine Handlers:** `Codergen` and `Wait.human` handlers are implemented and integrated into the runtime.
*   **Jail:** Managed by `scripts/launcher.clj`. Supports **Materialized (Mutable) Inputs** in Admin mode via `rsync`.
*   **Input Whitelist:** `agent-inputs.edn` controls which flake inputs are mounted for agent use. Current whitelist: `attractor`, `brynary-attractor`, `criomos`, `webpublish`.
*   **Reference Implementation:** `brynary-attractor` (TypeScript) is mounted in `inputs/untyped/` for architectural study.
*   **Version Control:** **Jujutsu (jj)** is the primary VCS. The **"Commit-Every-Intent"** mandate is in effect. 

## 4. Immediate Tasks for New Session
1.  **Study Attractor Reference**: Thoroughly analyze `inputs/untyped/brynary-attractor` implementation to refine the Rust engine's routing and parallelism logic.
2.  **Advanced Edge Selection**: Upgrade `src/dot_loader.rs` and the engine to support edge weights, full conditions (`outcome=success && context.key=val`), and preferred labels.
3.  **Dynamic Chronography**: Implement a native solar coordinate calculator in Clojure to replace static placeholders in `scripts/logger.clj`.
4.  **ASKI Integration**: Utilize the `aski` DSL within the Mentci manifestation loop for structural generation.

## 5. Philosophical Anchors
*   **Ontology Resides in Data:** Type is intrinsic, not procedural.
*   **Sema Object Style:** Everything is an Object. Direction encodes Action.
*   **Level 6 Vision:** Mentci is the bridge to symbolic manipulation (Ref: *The Zero Theorem*, *Cloud Atlas*).

**Good luck. The Great Work continues.**
