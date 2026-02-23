# Mentci-AI Agent Instructions

This document provides non-negotiable instructions for AI agents operating within the Mentci-AI ecosystem. These rules ensure architectural integrity and cryptographic provenance.

## -1. Enforcement Contract (Automatic Loading)

The following files are mandatory authority sources and **must be loaded automatically** by the agent before any analysis or implementation:

1. `Library/architecture/AskiPositioning.md`
2. `Core/ARCHITECTURAL_GUIDELINES.md`
3. `Library/specs/AskiFsSpec.md` (Filesystem Ontology)
4. `Core/VersionControlProtocol.md`
5. `Core/HIGH_LEVEL_GOALS.md`
5. `Core/SEMA_RUST_GUIDELINES.md`, `Core/SEMA_CLOJURE_GUIDELINES.md`, `Core/SEMA_NIX_GUIDELINES.md` (as relevant to touched files)
6. `Library/RestartContext.md` (The overview map)
7. `Core/AGENTS.md` (This file)
8. `Core/index.edn` (Authority Registry)

Enforcement requirements:

*   **Preemptive Context Acquisition:** If these files are not in the agent's active context, it must stop and acquire them immediately.
*   **No-Edit Without Architecture Context:** Any change made without having processed these guidelines is a violation of the Enforcement Contract.
*   **Programming Version Signature:** Every agent response **must** end with its current "Programming Version"—a content-addressed hash of the `Core/` directory. 
    *   Format: `programming: <version_hash>` (on its own line at the very end of the response).
    *   Acquire via: `bb Components/scripts/program_version/main.clj get`.
*   **Solar Baseline Prefix:** Every prompt-handling response (including intermediary updates and final response) must begin with the current ordinal solar baseline line.
    *   Format: `solar: <AnnoMundi>.<zodiac>.<degree>.<minute>.<second>`
    *   Canonical example: `solar: 5919.12.05.04.04`
    *   Canonical acquisition:
      1. `chronos --format am --precision second`
      2. fallback `cargo run --quiet --manifest-path Components/chronos/Cargo.toml --bin chronos -- --format am --precision second`
    *   Purpose: establish a true-solar reference timestamp for comparison with other time systems.

*   **Architecture Gate:** Any change conflicting with the hierarchy in `Core/ARCHITECTURAL_GUIDELINES.md` is forbidden.
*   **Version-Control Gate:** `Core/VersionControlProtocol.md` is mandatory procedure, not guidance.
*   **Restart Context Mandate:** Any modification to major repository components (Engine logic, core protocols, input structures) **must** be reflected in an update to `Library/RestartContext.md` and a new entry in `Outputs/Logs/ReleaseMilestones.md`. Agents must consult the Restart Context at the beginning of every session to understand the project's current spatial map.

## 0. Core Sema Object Principles (Primary)

These are the highest-order rules for all languages and scripts.

*   **Sub-Program Directory:** The `Library/` directory contains agent-executable overview modules. These are the primary tools for state resumption.
*   **Strategy System (Pre-Implementation):** All planning, architectural drafts, and feasibility studies must be kept in `Development/<priority>/<Subject>/` dedicated directories (`<priority>` in `{high,medium,low}`).
    *   **Prioritization:** Strategies must be prioritized according to the **Strategy Queue** (Ref: `Library/StrategyQueue.md`). Resiliency and efficiency are ranked highest.
    *   **Composition:** Strategies should consist of multiple files (e.g., `Mission.md`, `ArchitectureMap.md`, `Roadmap.md`) and sub-folder source code drafts (`src/`).
    *   **Workflow:** Strategies are "lined up" for implementation-trials. Use cheaper models to explore dead-ends and effective paths (vibe-coding permitted here) before high-authority models formalize the final logic.
    *   **Development Loop:** Every strategy must undergo the **Strategy-Development Program** (Ref: `Library/StrategyDevelopment.md`) to discover, package, and test the necessary tools and libraries.
    *   **Refinement:** Strategies are iteratively refined. Once a strategy reaches implementation maturity, its finalized components must be migrated to `Core/`, a component directory under `Components/` (for example `Components/mentci-aid/`), or `Components/tasks/`.
*   **Per-Subject Indexing:** Subjects should be merged or split as context volume changes. Multi-subject files should be cross-referenced.
*   **Subject Context Discovery (Mandatory):** Before planning or implementation, agents must search `Development/` and `Research/` for matching subject(s) from the prompt domain and ingest the most relevant entries.
    *   **Search First:** Use subject-name and keyword search to find existing context before creating new artifacts.
    *   **Dual-Tree Read:** Read both sides (`Development/<priority>/<Subject>/` and `Research/<priority>/<Subject>/`) when either side exists.
    *   **Context Reuse Rule:** If matching subject context exists, continue from it instead of starting a disconnected track.
*   **Development/Research Subject Unification:** Every subject in `Research/` must have a corresponding `Development/<priority>/<Subject>/` directory, and every development subject must have a corresponding topic directory `Research/<priority>/<Subject>/` with a topic index file `index.edn`.
    *   **Counterpart Discovery First:** Before creating new subject artifacts, look for an existing counterpart subject in the opposite tree.
    *   **Auto-Create Missing Counterparts:** If no counterpart exists, create and populate it (development scaffold or research topic).
    *   **Canonical Tool:** Use `bb Components/scripts/subject_unifier/main.clj --write` to enforce and repair bidirectional subject coverage.
*   **R&D Mirror Contract:** Repository R&D consists of the paired trees `Development/` and `Research/`. Topic names mirror across both trees: `Development/` carries executable guidance, `Research/` carries prompt-traceable findings.
*   **mentci-aid Identification:** The core execution engine is **mentci-aid** (Daemon + Aid). Agents should recognize this as the primary pipeline supervisor. **Note: mentci-aid is currently NOT in a running state.**
*   **Assimilation of Sources:** `attractor` (StrongDM) and `attractor-docs` (Brynary) are critical building blocks located in `Sources/`. They must be **assimilated**—rewritten internally in Sema-standard Aski + Rust + Clojure + Nix—rather than merely consumed as external dependencies.
*   **Language Authority Hierarchy:**
    1.  **Aski:** Evolved multi-domain Clojure. Takes precedence for specs and LLM-friendly logic.
    2.  **Rust:** Core implementation and heavy lifting.
    3.  **Clojure:** Fast prototyping for small tools and orchestration glue.
    4.  **Nix:** Low-level utility only. Should be phased out or hidden behind Aski (see Lojix).
*   **Single Object In/Out:** All boundary-crossing values are Sema objects. Every function accepts exactly one explicit object argument and returns exactly one object. When multiple Sources/outputs are required, define an input/output object.
*   **Everything Is an Object:** Reusable behavior belongs to named objects or traits. Free functions exist only as orchestration shells.
*   **Naming Is a Semantic Layer:** Meaning appears once at the highest valid layer. Repetition across layers is forbidden.
*   **Capitalization Is Ontology:** `PascalCase` denotes durable objects. `lowercase` denotes flow and transient logic.
*   **Direction Encodes Action:** Prefer `from_*`, `to_*`, `into_*`. Avoid verbs like `read`, `write`, `load`, `save` when direction already conveys meaning.
*   **Schema Is Sema:** Schemas define truth. Encodings are incidental. Do not name domain logic after wire formats.
*   **Filesystem Is Semantic:** Repo/dir/module boundaries carry meaning. Inner layers assume outer context.
*   **Documentation Protocol:** Impersonal, timeless, precise. Document only non-boilerplate behavior.

## 1. Environment & Isolation

Agents execute within a **Nix Jail**. All operations must be performed using the provided tools. Direct network access from the sandbox is forbidden.

### 1.1 Pre-Fetch
To acquire external Sources (tarballs, git repos), use Nix-native prefetch tooling from the jail shell (for example `nix-prefetch-git` and flake input updates). Do not introduce Python fetch helpers.

## 2. Audit Trail (MANDATORY AUTO-COMMIT)

**EVERY PROMPT SESSION MUST END WITH A PUSH TO THE `dev` BOOKMARK.** 

*   **Atomic Intent:** Every single modification MUST result in an `intent:` commit. Do not bundle independent changes.
*   **Dirty Tree Rule:** Never finish a response with a dirty working copy. Use `jj commit` before finalizing.
*   **Session Synthesis:** Use `bb Components/scripts/session_finalize/main.clj` at the end of every prompt to aggregate intents and **PUSH TO ORIGIN/DEV**.
*   **Auditability:** Use `jj log` as the authoritative audit trail for work performed in the repository.

## 3. Structural Rules

*   **Clojure (Babashka) Mandate:** All glue code and scripts must be written in Clojure (Babashka). No Bash logic beyond the one-line bb shim.
*   **Script Typing:** All Clojure scripts must define Malli schemas for Sources/config and validate them.
*   **Script Guard:** Run `bb Components/scripts/validate_scripts/main.clj` when adding or editing scripts. Python is forbidden under `Components/scripts/`.
*   **Per-Language Sema Guidelines:** Follow the dedicated language rules in `Core/SEMA_CLOJURE_GUIDELINES.md`, `Core/SEMA_RUST_GUIDELINES.md`, and `Core/SEMA_NIX_GUIDELINES.md`.
*   **Attractor Code Reference:** Implementation lives in `Sources/brynary-attractor/attractor`. The `Sources/attractor` folder is specs only.
*   **Attractor Backend Behavior:** `CliAgentBackend` spawns a subprocess with env merged from `process.env` and backend config. `SessionBackend` uses `unified-llm` `Client.fromEnv` (API keys via standard env vars).
*   **Sources Directory Rule:** Do not edit anything under `Sources/`. Treat it as read-only reference material.
*   **EDN Authority:** Favor EDN for all data storage and state persistence. Use `jet` for transformations.
*   **Sema Object Style:** Strictly follow the ontology defined in `Components/schema/*.capnp`.
*   **Context-Local Naming Rule:** Avoid repeating enclosing context in identifiers (example: in `nix/` code, use `namespace`, not `nixns`).
*   **Source Control:** Atomic, concise commits to the `dev` bookmark using `jj`. Follow the per-prompt dirty-tree auto-commit rule in `Core/VersionControlProtocol.md`.
*   **Tagging:** When creating git tags, always use the `-m` flag to provide a message directly (e.g., `git tag -a vX.Y.Z -m "release: vX.Y.Z"`) to avoid interactive editor prompts.

## 4. Admin Developer Mode

High-authority agents (like Mentci) operate in Admin Developer Mode. You are responsible for the system's evolution toward Level 6 instinctive symbolic interaction.
