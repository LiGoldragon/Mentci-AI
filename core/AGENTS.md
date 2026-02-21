# Mentci-AI Agent Instructions

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `docs/architecture/ASKI_POSITIONING.md`.

This document provides non-negotiable instructions for AI agents operating within the Mentci-AI ecosystem. These rules ensure architectural integrity and cryptographic provenance.

## -1. Enforcement Contract (Automatic Loading)

The following files are mandatory authority sources and **must be loaded automatically** by the agent before any analysis or implementation:

1. `core/ASKI_POSITIONING.md`
2. `core/ARCHITECTURAL_GUIDELINES.md`
3. `core/VERSION_CONTROL.md`
4. `core/HIGH_LEVEL_GOALS.md`
5. `core/SEMA_RUST_GUIDELINES.md`, `core/SEMA_CLOJURE_GUIDELINES.md`, `core/SEMA_NIX_GUIDELINES.md` (as relevant to touched files)
6. `core/programs/RESTART_CONTEXT.md` (The overview map)
7. `core/AGENTS.md` (This file)

Enforcement requirements:

*   **Preemptive Context Acquisition:** If these files are not in the agent's active context, it must stop and acquire them immediately.
*   **No-Edit Without Architecture Context:** Any change made without having processed these guidelines is a violation of the Enforcement Contract.
*   **Programming Version Signature:** Every agent response **must** end with its current "Programming Version"—a content-addressed hash of the `core/` directory. 
    *   Format: `programming: <version_hash>` (on its own line at the very end of the response).
    *   Acquire via: `bb scripts/program_version.clj get`.

*   **Architecture Gate:** Any change conflicting with the hierarchy in `core/ARCHITECTURAL_GUIDELINES.md` is forbidden.
*   **Version-Control Gate:** `core/VERSION_CONTROL.md` is mandatory procedure, not guidance.
*   **Restart Context Mandate:** Any modification to major repository components (Engine logic, core protocols, input structures) **must** be reflected in an update to `core/programs/RESTART_CONTEXT.md` and a new entry in `Logs/RELEASE_MILESTONES.md`. Agents must consult the Restart Context at the beginning of every session to understand the project's current spatial map.

## 0. Core Sema Object Principles (Primary)

These are the highest-order rules for all languages and scripts.

*   **Sub-Program Directory:** The `core/programs/` directory contains agent-executable overview modules. These are the primary tools for state resumption.
*   **Strategy System (Pre-Implementation):** All planning, architectural drafts, and feasibility studies must be kept in `strategies/<subject>/` dedicated directories.
    *   **Composition:** Strategies should consist of multiple files (e.g., `MISSION.md`, `ARCHITECTURE.md`, `ROADMAP.md`) and sub-folder source code drafts (`src/`).
    *   **Workflow:** Strategies are "lined up" for implementation-trials. Use cheaper models to explore dead-ends and effective paths (vibe-coding permitted here) before high-authority models formalize the final logic.
    *   **Refinement:** Strategies are iteratively refined. Once a strategy reaches implementation maturity, its finalized components must be migrated to `core/`, `src/`, or `tasks/`.
*   **Per-Subject Indexing:** Subjects should be merged or split as context volume changes. Multi-subject files should be cross-referenced.
*   **mentci-aid Identification:** The core execution engine is **mentci-aid** (Daemon + Aid). Agents should recognize this as the primary pipeline supervisor. **Note: mentci-aid is currently NOT in a running state.**
*   **Assimilation of Inputs:** `attractor` (StrongDM) and `attractor-docs` (Brynary) are critical building blocks located in `inputs/`. They must be **assimilated**—rewritten internally in Sema-standard Aski + Rust + Clojure + Nix—rather than merely consumed as external dependencies.
*   **Language Authority Hierarchy:**
    1.  **Aski:** Evolved multi-domain Clojure. Takes precedence for specs and LLM-friendly logic.
    2.  **Rust:** Core implementation and heavy lifting.
    3.  **Clojure:** Fast prototyping for small tools and orchestration glue.
    4.  **Nix:** Low-level utility only. Should be phased out or hidden behind Aski (see Lojix).
*   **Single Object In/Out:** All boundary-crossing values are Sema objects. Every function accepts exactly one explicit object argument and returns exactly one object. When multiple inputs/outputs are required, define an input/output object.
*   **Everything Is an Object:** Reusable behavior belongs to named objects or traits. Free functions exist only as orchestration shells.
*   **Naming Is a Semantic Layer:** Meaning appears once at the highest valid layer. Repetition across layers is forbidden.
*   **Capitalization Is Ontology:** `PascalCase` denotes durable objects. `lowercase` denotes flow and transient logic.
*   **Direction Encodes Action:** Prefer `from_*`, `to_*`, `into_*`. Avoid verbs like `read`, `write`, `load`, `save` when direction already conveys meaning.
*   **Schema Is Sema:** Schemas define truth. Encodings are incidental. Do not name domain logic after wire formats.
*   **Filesystem Is Semantic:** Repo/dir/module boundaries carry meaning. Inner layers assume outer context.
*   **Documentation Protocol:** Impersonal, timeless, precise. Document only non-boilerplate behavior.

## 1. Environment & Isolation

Agents execute within a **Nix Jail**. All operations must be performed using the provided tools. Direct network access from the sandbox is forbidden.

### 1.1 Pre-Fetch Orchestrator
To acquire external inputs (tarballs, git repos), you must use the **CriomOS Pre-Fetch Orchestrator** MCP server located at `scripts/prefetch_orchestrator.py`.

*   **Transport:** Communication is via `stdio` using JSON-RPC.
*   **Command:** `python3 scripts/prefetch_orchestrator.py --mcp`
*   **Methods:**
    *   `prefetch_url(url, unpack=False)` -> Returns SRI hash.
    *   `prefetch_git(url, rev=None, submodules=False)` -> Returns SRI hash, rev, and store path.

## 2. Audit Trail

Use `jj log` as the authoritative audit trail for work performed in the repository.

## 3. Structural Rules

*   **Clojure (Babashka) Mandate:** All glue code and scripts must be written in Clojure (Babashka). No Bash logic beyond the one-line bb shim.
*   **Script Typing:** All Clojure scripts must define Malli schemas for inputs/config and validate them.
*   **Script Guard:** Run `bb scripts/validate_scripts.clj` when adding or editing scripts. Python is forbidden under `scripts/` except `scripts/prefetch_orchestrator.py`.
*   **Per-Language Sema Guidelines:** Follow the dedicated language rules in `core/SEMA_CLOJURE_GUIDELINES.md`, `core/SEMA_RUST_GUIDELINES.md`, and `core/SEMA_NIX_GUIDELINES.md`.
*   **Attractor Code Reference:** Implementation lives in `inputs/brynary-attractor/attractor`. The `inputs/attractor` folder is specs only.
*   **Attractor Backend Behavior:** `CliAgentBackend` spawns a subprocess with env merged from `process.env` and backend config. `SessionBackend` uses `unified-llm` `Client.fromEnv` (API keys via standard env vars).
*   **Inputs Directory Rule:** Do not edit anything under `inputs/`. Treat it as read-only reference material.
*   **EDN Authority:** Favor EDN for all data storage and state persistence. Use `jet` for transformations.
*   **Sema Object Style:** Strictly follow the ontology defined in `schema/*.capnp`.
*   **Context-Local Naming Rule:** Avoid repeating enclosing context in identifiers (example: in `nix/` code, use `namespace`, not `nixns`).
*   **Source Control:** Atomic, concise commits to the `dev` bookmark using `jj`. Follow the per-prompt dirty-tree auto-commit rule in `core/VERSION_CONTROL.md`.
*   **Tagging:** When creating git tags, always use the `-m` flag to provide a message directly (e.g., `git tag -a vX.Y.Z -m "release: vX.Y.Z"`) to avoid interactive editor prompts.

## 4. Admin Developer Mode

High-authority agents (like Mentci) operate in Admin Developer Mode. You are responsible for the system's evolution toward Level 6 instinctive symbolic interaction.

