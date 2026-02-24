# Agent Session Resumption Context (Restart Context)

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `Library/architecture/AskiPositioning.md`.

**Metadata:**
*   **Target Authority:** Top Admin, Li Goldragon.
*   **Primary Working Bookmark:** `dev`.
*   **Current Programming Version:** `kp0tz2tm` (Ref: `execute version`).
*   **Latest Release Tag:** `v0.12.7.3.31` (current milestone).

## 1. Project Overview
Mentci-AI is a Level 5 "Dark Factory" AI daemon (`mentci-aid`) under stabilization. The repository now operates with a native Rust actor-based orchestration layer (`execute`) and strict Logic-Data separation.

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
- **Rust componentization:** `Components/mentci-aid/`, `Components/chronos/`, `Components/mentci-box/`, `Components/mentci-fs/`, and `Components/mentci-launch/` are standalone crates.
- **Symbolic Orchestrator:** `execute` lives at `Components/mentci-aid/src/bin/execute.rs` and uses `ractor` actors.
- **Filesystem ontology authority:** `Library/specs/AskiFsSpec.md`.
- **Source substrate:** `Sources/` mounted read-only via jail tooling.
- **High-Weight Specs:**
  - `Library/specs/SemaBinarySpec.md`: SEMA binary format.
  - `Library/specs/LojixSyntaxSpec.md`: `lojix` syntax rules (extended EDN).
  - `Library/architecture/MentciBoxIsolation.md`: Mentci Box isolation protocol.
  - `Library/specs/MentciLaunchSpec.md`: systemd + terminal launch contract for Mentci-Box.
- **Primary Operator Interface:** TypeScript `pi` (`pi`) is default; Rust `pi` remains non-default validation lane.

## 5. Current Operational Snapshot (♓︎.7.1.7 | 5919 AM)
- `dev` lineage now includes control-plane convergence hardening plus initial `mentci-launch` component implementation and test coverage.

Section sweep progress:
- **Status:** **COMPLETED** (Full Repository Sweep 1-10).
- **Outcome:** All core and library documentation synchronized with refined authority tiers.
- **Convergence Slice:** `nix run .#execute` command path restored, execute test/check surfaces realigned to actor subcommands, and `mentci_box.capnp` authority centralized in `Components/schema/`.
- **Launch Slice:** `mentci-launch` crate added with Cap'n Proto request decoding, systemd launch-plan synthesis (terminal/service), and dedicated tests.

## 6. Guard Health Snapshot
Latest observed gate outcomes:
1. `root-guard`: passing.
2. `link-guard`: passing.
3. `session-guard`: passing.
4. `unify`: passing.

Interpretation:
- The transition to native Rust actors for repository integrity has been successful.
- **Logic-Data Separation** is enforced via EDN sidecar configurations.
- Obsolete Clojure scripts have been removed.

## 7. High-Importance Active Risks
1. **Control-plane divergence risk:** `main` and `dev` are both advancing; convergence protocol (`dev` -> finalized session -> release) needs consistent enforcement.
2. **Component Maturity:** `mentci-fs` and `aski-lib` are in early prototype stages.
3. **Launch Integration Gap:** `mentci-launch` is implemented as a standalone crate but not yet wired into `execute` command routing and policy guards.

## 8. Immediate Priorities (Execution Order)
1. Integrate `mentci-launch` into `execute` command routing and finalize service/terminal policy checks.
2. Stabilize `mentci-box` V1 for robust bootstrap.
3. Mature `mentci-fs` for concise filesystem comprehension.
4. Implement `actor-first` logic for remaining orchestration tasks.

## 9. Script-Surface Realignment Focus
1. **REPLACED:** legacy Clojure scripts entirely removed in favor of `execute` subcommands (Rust/Actor).
2. **EXTERNALIZED:** Guard policy allowlists and constants reside in `.edn` sidecars.
3. **CONVERGED:** Environment initialization now handled by `execute launcher`.

## 10. Operational Requirements
- Any major component/protocol/input mapping changes must update this file and append `Library/reports/ReleaseMilestones.md`.
- Keep R&D mirror integrity (Development/Research counterpart coverage) when adding new subjects.

*The Great Work continues.*
