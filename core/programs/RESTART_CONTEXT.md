# Agent Session Resumption Context (Restart Context)

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `core/ASKI_POSITIONING.md`.

**Metadata:**
*   **Target Authority:** Top Admin, Li Goldragon.
*   **Active Bookmark:** `dev`.
*   **Current Programming Version:** `045flr83` (Ref: `scripts/program_version.clj`).
*   **Latest Release Tag:** `v0.12.3.45.53` (Ref: `Logs/RELEASE_MILESTONES.md`).

## 1. Project Overview
Mentci-AI is a Level 5 "Dark Factory" AI daemon (**mentci-aid**) designed for autonomous symbolic manipulation. It operates within a Pure Nix Jail environment and uses Jujutsu (`jj`) for version control.

## 2. Mandatory Core Context (The Program)
These files define the agent's operating logic and must be loaded automatically:
- `core/AGENTS.md`: Instruction set and response signatures.
- `core/ARCHITECTURAL_GUIDELINES.md`: The laws of naming, casing, and authority.
- `core/VERSION_CONTROL.md`: JJ protocols and "Commit Every Intent" mandate.
- `core/HIGH_LEVEL_GOALS.md`: Durable roadmap (Current: Goal 0 - Stabilization).
- `core/ASKI_POSITIONING.md`: Teleological framing of the symbolic interface.
- `core/SEMA_*_GUIDELINES.md`: Language-specific structural rules (Rust, Clojure, Nix).

## 3. Component Status Map
- **Engine (`mentci-aid`):** `src/main.rs`. Status: **Experimental / Not in a running state.**
- **Orchestration:** `scripts/launcher.clj` (Jail), `scripts/commit.clj` (Shipping).
- **Truth Layer:** `schema/*.capnp` (Semantic types), `core/` (Architectural mandates).
- **Drafting System:** `drafts/<subject>/`. Status: **Operational.** Active subjects: `attractor`, `mentci-rfs`.
- **Obsolescence Pipeline:** `core/programs/OBSOLESCENCE_PROTOCOL.md`. Status: **Active.** Tracking in `Logs/OBSOLESCENCE_STRIKES.edn`.
- **Input Substrate:** `Inputs/` (Read-only store paths), managed via `flake.nix` and `jail.nix`.
- **Audit Trail:** `jj log` (VCS), `Logs/RELEASE_MILESTONES.md` (Human-readable history).

## 4. Operational Requirements
- **Change Mandate:** Any modification to major components (Engine, Core Protocols, Input Mapping) **must** be reflected in an update to this file and a new entry in `Logs/RELEASE_MILESTONES.md`.
- **Purity:** All work occurs in the Nix Jail. Editor synchronization is managed via `tools/emacs/mentci.el`.

*The Great Work continues.*
