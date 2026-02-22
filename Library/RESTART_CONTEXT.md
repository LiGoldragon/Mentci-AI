# Agent Session Resumption Context (Restart Context)

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `Core/ASKI_POSITIONING.md`.

**Metadata:**
*   **Target Authority:** Top Admin, Li Goldragon.
*   **Active Bookmark:** `dev`.
*   **Current Programming Version:** `mdg07qlf` (Ref: `Components/scripts/program_version/main.clj`).
*   **Latest Release Tag:** `v0.12.3.58.4` (Ref: Git tag state).

## 1. Project Overview
Mentci-AI is a Level 5 "Dark Factory" AI daemon (**mentci-aid**) designed for autonomous symbolic manipulation. It operates within a Pure Nix Jail environment and uses Jujutsu (`jj`) for version control.

## 2. Mandatory Core Context (The Program)
These files define the agent's operating logic and must be loaded automatically:
- `Core/AGENTS.md`: Instruction set and response signatures.
- `Core/ARCHITECTURAL_GUIDELINES.md`: The laws of naming, casing, and authority.
- `Core/VERSION_CONTROL.md`: JJ protocols and "Commit Every Intent" mandate.
- `Core/HIGH_LEVEL_GOALS.md`: Durable roadmap (Current: Goal 0 - Stabilization).
- `Core/ASKI_POSITIONING.md`: Teleological framing of the symbolic interface.
- `Core/SEMA_*_GUIDELINES.md`: Language-specific structural rules (Rust, Clojure, Nix).
- `Core/ASKI_FS_SPEC.md`: Filesystem Ontology.
- `Core/EXTENSION_INDEX_PROTOCOL.md`: Optional extension-index contract for keeping Core stable while loading extensions from standard locations.

## 3. Component Status Map
- **Engine (`mentci-aid`):** `Components/mentci-aid/src/main.rs`. Status: **Experimental / Not in a running state.**
- **Orchestration:** `Components/scripts/launcher/` (Jail), `Components/scripts/commit/` (Shipping).
- **Truth Layer:** `Components/schema/*.capnp` (Semantic types), `Core/` (Architectural mandates).
- **FS Ontology:** `Core/ASKI_FS_SPEC.md`. Status: **Operational / Canonical.**
- **Strategy System:** `Strategies/<Subject>/`. Status: **Operational.** Active: `Agent-Authority-Alignment`, `Artifact-Sweep`, `Aski-Conversion`, `Aski-Refinement`, `Attractor`, `Debugging`, `Mentci-RFS`, `Project-Hardening`, `Strategy-Development`, `Universal-Program-Pack`.
- **Strategy Queue:** `Library/STRATEGY_QUEUE.md`. Status: **Operational.** Prioritizing resiliency and efficiency.
- **Development Loop:** `Library/STRATEGY_DEVELOPMENT.md`. Status: **Active.**
- **Obsolescence Pipeline:** `Library/OBSOLESCENCE_PROTOCOL.md`. Status: **Active.** Tracking 4 files at Strike-2 (Restored). Using **Three-Strike Rule**.
- **Orchestration Scripts:** `Components/scripts/<name>/`. Reorganized into autonomous directories with `TESTING_CONTEXT.md`. Nix-wrapped and reachable.
- **Source Substrate:** `Sources/` (transitional alias: `Inputs/`) read-only store paths, managed via `flake.nix` and `Components/nix/jail.nix`.
- **VCS Guardrail:** `Sources/` (and transitional `Inputs/`) is gitignored as mounted runtime substrate; source updates are managed through flake/input refresh workflows, not direct Git tracking.
- **Audit Trail:** `jj log` (VCS), `Outputs/Logs/RELEASE_MILESTONES.md` (Human-readable history), `Outputs/Logs/ARTIFACT_SWEEP_REPORT.md` (Instruction-artifact tracking), `Strategies/Artifact-Sweep/ARTIFACT_ANALYSIS.md` (Obsolete file analysis).

## 3.1 Current Operational Snapshot (♓︎.5.9.42 | 5919 AM)
- `dev` head: `727426e3` (`session: backfill missing bug sweep exchange with report and strategy`)
- `main` head: `0faae18a` (`session: release main from dev and record release action`)
- Recent major session commits:
  - `0828f303`: core self-contained extension-index strategy implementation.
  - `040e5e75`: high-priority remediation batch (protocol/path/authority sync).
- New sandbox execution path in `mentci-aid`:
  - `mentci-ai sandbox -- <cmd ...>`
  - `mentci-ai execute sandbox -- <cmd ...>` (alias)
  - implementation location: `Components/mentci-aid/src/sandbox.rs`
- Session protocol health:
  - `session_guard`: passing.
  - `validate_scripts`: passing.
  - `root_guard`: failing due lowercase top-level `outputs` directory.
- New core extension mechanism:
  - `Core/EXTENSION_INDEX_PROTOCOL.md`
  - `Core/EXTENSION_INDEX_LOCATIONS.edn`
  - `Components/scripts/extension_index/main.clj`

## 3.2 Open Risks / Next Checks
1. Top-level FS drift:
- Resolve `outputs` (lowercase) vs canonical `Outputs/`.
2. Source naming migration window:
- Continue `Inputs/` -> `Sources/` textual/operational convergence while compatibility alias is active.
3. Strategy queue hygiene:
- Keep queue entries synchronized with active `Strategies/<Subject>/` set.

## 4. Operational Requirements
- **Change Mandate:** Any modification to major components (Engine, Core Protocols, Input Mapping) **must** be reflected in an update to this file and a new entry in `Outputs/Logs/RELEASE_MILESTONES.md`.
- **Purity:** All work occurs in the Nix Jail. Editor synchronization is managed via `Components/tools/emacs/mentci.el`.

*The Great Work continues.*
