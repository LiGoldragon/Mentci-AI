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
3.  **`docs/specs/WORKFLOW_STANDARD.md`**: The official standard for Agentic Workflows (Attractor-based).
4.  **`docs/specs/ASKI_FLOW_DSL.md`**: The new **Noun-Sequence DSL** for defining workflows in EDN. Use this over DOT for new definitions.
5.  **`schema/mentci.capnp`** & **`schema/atom_filesystem.capnp`**: The semantic truth of the system (Filesystem Atoms, Orchestrator RPCs).
6.  **`docs/reports/BRYNARY_ATTRACTOR_SUMMARY.md`**: High-level summary of the Attractor approach and how it differs from chat loops.

## 3. Current State & Intent
*   **Filesystem:** The entire project is typed as a "Repo Atom". Documentation is organized into `docs/`.
*   **Orchestration:** Rust daemon (`mentci-ai`) implements a basic traversal engine.
    *   **Parsers:** DOT parser (`src/dot_loader.rs`) is being migrated to `dot-parser` 0.6.1 canonical representation.
    *   **Aski-Flow:** EDN loader (`src/edn_loader.rs`) implemented for Noun-Sequence DSL. Supported extension: `.aski-flow`.
    *   **Engine:** Executes workflows, saves checkpoints (`src/main.rs`). Supports switching between DOT and Aski-Flow based on file extension.
*   **Workflow Definition:**
    *   **Visual:** DOT (`workflows/*.dot`) is supported for legacy/visualization.
    *   **Internal:** **Aski-Flow** (`docs/specs/ASKI_FLOW_DSL.md`) is the adopted standard for future definitions (Extension: `.aski-flow`).
*   **Jail:** Managed by `scripts/launcher.clj`. Supports **Materialized (Mutable) Inputs** in Admin mode via `rsync`.

## 4. Immediate Tasks for New Session
1.  **Resolve Compilation Errors**: 
    *   Fix `src/dot_loader.rs` to correctly interface with `dot-parser` 0.6.1 `canonical::Graph` (handle `NodeSet`/`EdgeSet` iteration and private fields).
    *   Fix `src/edn_loader.rs` ownership/private field issues with `edn-rs` (Map/List conversion and iteration).
2.  **Verify Aski-Flow Parsing**: Use `workflows/test.aski-flow` to verify the `EdnLoader` correctly extracts nodes and implicit/explicit edges once compiled.
3.  **Schema Alignment**: Ensure the updated `schema/mentci.capnp` is correctly utilized in `src/main.rs`.
4.  **Dynamic Chronography**: Implement a native solar coordinate calculator in Clojure to replace static placeholders in `scripts/logger.clj`.

## 5. Philosophical Anchors
*   **Ontology Resides in Data:** Type is intrinsic, not procedural.
*   **Sema Object Style:** Everything is an Object. Direction encodes Action.
*   **Level 6 Vision:** Mentci is the bridge to symbolic manipulation (Ref: *The Zero Theorem*, *Cloud Atlas*).

**Good luck. The Great Work continues.**