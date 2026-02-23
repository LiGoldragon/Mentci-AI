# Agent Session Resumption Context (Restart Context)

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `Library/architecture/AskiPositioning.md`.

**Metadata:**
*   **Target Authority:** Top Admin, Li Goldragon.
*   **Primary Working Bookmark:** `dev`.
*   **Current Programming Version:** `1v4rpwsy` (Ref: `execute version`).
*   **Latest Release Tag:** `v0.12.5.45.52` (release target for this snapshot).

## 1. Project Overview
Mentci-AI is a Level 5 "Dark Factory" AI daemon (`mentci-aid`) under stabilization. The repository currently operates with a native Rust actor-based orchestration layer (`execute`) following the transition from legacy Clojure scripts.

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

## 4. Component Status Map
- **Engine (`mentci-aid`):** `Components/mentci-aid/src/main.rs`. Status: **Experimental / not in running state**.
- **Rust componentization:** `Components/mentci-aid/` and `Components/chronos/` are standalone crates.
- **Symbolic Orchestrator:** `execute` lives at `Components/mentci-aid/src/bin/execute.rs` and uses `ractor` actors.
- **Filesystem ontology authority:** `Library/specs/AskiFsSpec.md`.
- **Source substrate:** `Sources/` mounted read-only via jail tooling.
- **High-Weight Specs:**
  - `Library/specs/SemaBinarySpec.md`: SEMA binary format.
  - `Library/specs/LojixSyntaxSpec.md`: `lojix` syntax rules (extended EDN).
  - `Library/architecture/MentciBoxIsolation.md`: Mentci Box isolation protocol.

## 5. Current Operational Snapshot (♓︎.6.6.15 | 5919 AM)
- `dev` head: `968e572` (`session: fix chronos warnings and report on Rust data ergonomics`)

Section sweep progress:
- **Status:** **COMPLETED** (Full Repository Sweep 1-10).
- **Outcome:** All core and library documentation synchronized with refined authority tiers.

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

## 8. Immediate Priorities (Execution Order)
1. Stabilize `mentci-box` V1 for robust bootstrap.
2. Mature `mentci-fs` for concise filesystem comprehension.
3. Implement `actor-first` logic for remaining orchestration tasks.

## 9. Script-Surface Realignment Focus
1. **DEPRECATED:** legacy Clojure scripts (`root_guard`, `reference_guard`, `session_guard`, `program_version`, `subject_unifier`, `intent`, `session_finalize`, `logger`, `session_metadata`, `solar_prefix`, `answer_report`).
2. **PORTED:** These functions now reside in `execute` subcommands (Rust/Actor).
3. **EXTERNALIZED:** Guard policy allowlists and constants reside in `.edn` sidecars.
4. **REMAINING:** Converge overlapping source-mount paths (`launcher` vs `sources_mounter`/`sources_remount`).

## 10. Operational Requirements
- Any major component/protocol/input mapping changes must update this file and append `Outputs/Logs/ReleaseMilestones.md`.
- Keep R&D mirror integrity (Development/Research counterpart coverage) when adding new subjects.

*The Great Work continues.*
