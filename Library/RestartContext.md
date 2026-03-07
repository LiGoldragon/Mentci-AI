# Agent Session Resumption Context (Restart Context)

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `Library/architecture/AskiPositioning.md`.

**Metadata:**
*   **Target Authority:** Top Admin, Li Goldragon.
*   **Primary Working Bookmark:** `dev`.
*   **Current Programming Version:** `f681220` (unifying Level 6 Saṃskāra Baseline & Debugging protocols).
*   **Latest Release Tag:** `v0.12.16.17.42` (current milestone).

## 1. Project Overview
Mentci-AI is a Level 5 "Dark Factory" AI daemon (`mentci-aid`) under stabilization, now entering **Level 6 Instinctive Symbolic Interaction**. The repository operates with a native Rust actor-based orchestration layer and distributed **Fractal DVCS** architecture.

## 1.1 Transition Summary Since Last Tagged Release (`v0.12.10.52.12`)
- **Unified Parallel History:** Successfully recovered and merged high-value parallel intents (OAuth, Fractal Core, Saṃskāra Bridge, Malli Infra) from detached heads into the primary `dev` trunk.
- **Fractal DVCS Baseline Established:** Transitioned the monorepo to a network of independent Jujutsu (`jj`) repositories managed as hermetic Nix Flake inputs. `mentci-user` is the first fully fractalized component.
- **Saṃskāra Bridge Realized:** Implemented the structured state projection layer with an MVP **Editor Bridge** for symbolic VCS refinement.
- **Stable Interaction Mandate:** Codified the requirement to use durable **Change IDs** instead of volatile Commit IDs for agentic operations.
- **Data Weighting Mandate:** Enforced the "Inquiry over Action" principle, requiring dense evidence gathering (Linkup/Logical queries) before code mutations.
- **Terminal Research:** Audited modern terminal emulators (WezTerm, Ghostty, Kitty) for hot-reloading capability and agentic integration.
- **Deep Rebase Disaster Recovery:** Formally outlawed using `jj rebase` for historical intent recovery, establishing "Content Extraction over Rebasing" to prevent `.gitignore` time-machine pollution.
- **Independence Refinement:** Updated the `independent-developer` skill with Lineage Integrity and Crypto-Content-Addressed Rebasing protocols.

## 2. Mandatory Core Context (The Program)
These files define the agent's operating logic and must be loaded automatically:
- `Core/AGENTS.md`
- `Core/ARCHITECTURAL_GUIDELINES.md`
- `Core/VersionControlProtocol.md`
- `Core/HIGH_LEVEL_GOALS.md`
- `Library/architecture/AskiPositioning.md`
- `Core/SEMA_*_GUIDELINES.md`
- `Library/specs/AskiFsSpec.md`
- `Core/ExtensionIndexProtocol.md`

## 3. R&D Topology Contract (High Importance)
R&D is the mirrored two-tree model:
1. `Development/<priority>/<Subject>/` contains executable plans and implementation guidance.
2. `Research/<priority>/<Subject>/` contains prompt-traceable findings and answer artifacts.
3. Topic names mirror across both trees by subject; counterpart integrity is enforced with `execute unify`.
4. **Localized Indexing:** All `index.edn` files use relative paths for efficiency and portability.

## 4. Component Status Map
- **Engine (`mentci-aid`):** `Components/mentci-aid/src/main.rs`. Status: **Experimental / not in running state**.
- **Saṃskāra Bridge:** `Components/samskara/`. Status: **MVP functional**. Manages VCS state projection and editor interception.
- **Fractal Core:** `Components/schema/mentci_vcs.capnp`. Status: **Schema established**.
- **OAuth Framework:** Designed for VTCode. Status: **Design-draft/Design-approved**.
- **Rust componentization:** `Components/chronos/`, `Components/mentci-box/`, `Components/mentci-fs/`, and `Components/mentci-launch/` are standalone crates.
- **Symbolic Orchestrator:** `execute` lives at `Components/mentci-aid/src/bin/execute.rs`.
- **Filesystem ontology authority:** `Library/specs/AskiFsSpec.md`.
- **Primary Operator Interface:** TypeScript `pi` (`pi`) is default; Rust `pi` remains non-default validation lane.
- **Symbolic Discovery Surface:** `logical-edit` (AST queries) and `linkup` (web validation) are the canonical discovery tools.

## 5. Current Operational Snapshot (♓︎.12.9.60 | 5919 AM)
- **Fractal Protocol:** Treating every component as a strict boundary; inter-component interaction must occur only through schema-validated channels.
- **Inquiry over Action:** LLM logic is weighted toward architectural fit through 2-3 additional discovery calls.
- **Change ID Primacy:** Agents target logical units of work using durable identifiers that persist through rebases.
- **Pure Nix devShell:** `mentci-user` bootstraps environment variables purely within the Nix shell logic.
- **Programmatic Refactoring:** moving to AST/CST lossless transformations via the `mentci-mcp` Rust server and `logical-edit`.
- **Extraction over Rebasing:** Due to the "Deep Rebase Disaster," history recovery relies strictly on `jj restore` or manual content extraction, never `jj rebase`, to prevent resurrecting stale un-ignored directory states (like `node_modules`).
- **Op-Log Recovery:** Mistakes during VCS operations are rectified surgically using `jj op restore <operation_id>` rather than destructive `jj undo` chains.

## 6. Guard Health Snapshot
Latest observed gate outcomes:
1. `root-guard`: passing.
2. `link-guard`: passing.
3. `session-guard`: passing.
4. `unify`: passing.

Interpretation:
- The tree is clean, linear, and unified.
- **Lineage Integrity** is restored after large-scale historical recovery.

## 7. High-Importance Active Risks
1. **Fractal Boundary Drift:** ensuring new cross-component interactions are strictly schema-first as the fractal migration progresses to remaining 22 components.
2. **Component Maturity:** `mentci-fs` and `aski-lib` remain in prototype stages.

## 8. Immediate Priorities (Execution Order)
1. **Automate Extraction:** Author `samskara-extract` tool to perform the fractal migration sequence for remaining components.
2. **Implement mentci-search:** Port Linkup API logic to a Rust actor to resolve extension registration race conditions.
3. **Refine Flow Registry:** Add hierarchical rebase discovery logic to `mentci-flow`.
4. **Flight Recorder Extension:** Standardize `jj` Op ID logging into `.mentci/op_history.txt`.

## 9. Session Continuity Addendum (2026-03-07)
- `main` currently points to `zvzuoyum / 980edb11` with:
  - Pi update to `0.57.0` (`Components/nix/pi.nix`, `flake.lock` updated to pi-mono `b279d03d...`).
  - `.pi/extensions.edn` removed (was disabling extension discovery with `"!**"`).
- Prior commit on `main`: `wzupruqn / 1cf87dae`
  - Removed shellHook behavior that created `.pi/last_pi_version` in repo working tree.
- Prior commit on `main`: `wzwusktn / 453b17aa`
  - Fixed `mentci-user export-env` initialization in flake-based `nix develop` by sourcing setup from `${mentci_user_src}/data/setup.bin` rather than `${repo_root}/Components/mentci-user/data/setup.bin`.
- Important operational note:
  - `nix develop github:LiGoldragon/Mentci-AI` may resolve default branch ref, while explicit `github:LiGoldragon/Mentci-AI/main` resolves `main` tip deterministically.
- Verified extension/runtime context:
  - Linkup extension discovered from path: `~/.pi/pi-source/node_modules/@aliou/pi-linkup/src/index.ts` after reload.
  - `settings.json` remains the active settings authority for extension loading in this repo context.

## 10. Next-Goal Handoff State
- Repository status intended for next task handoff:
  - `main` has been advanced with pi + shell behavior fixes.
  - Working copy should be empty commit on top of `main` before starting the next objective.
  - Continue `main`-only push protocol unless explicitly overridden.

*The Great Work continues.*
