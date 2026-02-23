# Agent Session Resumption Context (Restart Context)

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `Library/architecture/ASKI_POSITIONING.md`.

**Metadata:**
*   **Target Authority:** Top Admin, Li Goldragon.
*   **Primary Working Bookmark:** `dev`.
*   **Current Programming Version:** `76j6my3v` (Ref: `Components/scripts/program_version/main.clj`).
*   **Latest Release Tag:** `v0.12.5.45.52` (release target for this snapshot).

## 1. Project Overview
Mentci-AI is a Level 5 "Dark Factory" AI daemon (`mentci-aid`) under stabilization. The repository currently operates with a split control-plane state (`dev` active development lineage, `main` release lineage) and an in-progress deterministic cleanup program.

## 2. Mandatory Core Context (The Program)
These files define the agent's operating logic and must be loaded automatically:
- `Core/AGENTS.md`
- `Core/ARCHITECTURAL_GUIDELINES.md`
- `Core/VERSION_CONTROL.md`
- `Core/HIGH_LEVEL_GOALS.md`
- `Library/architecture/ASKI_POSITIONING.md`
- `Core/SEMA_*_GUIDELINES.md`
- `Library/specs/ASKI_FS_SPEC.md`
- `Core/EXTENSION_INDEX_PROTOCOL.md`

## 3. R&D Topology Contract (High Importance)
R&D is the mirrored two-tree model:
1. `Development/<priority>/<Subject>/` contains executable plans and implementation guidance.
2. `Research/<priority>/<Subject>/` contains prompt-traceable findings and answer artifacts.
3. Topic names mirror across both trees by subject; counterpart integrity is enforced with `bb Components/scripts/subject_unifier/main.clj`.

## 4. Component Status Map
- **Engine (`mentci-aid`):** `Components/mentci-aid/src/main.rs`. Status: **Experimental / not in running state**.
- **Rust componentization:** `Components/mentci-aid/` and `Components/chronos/` are standalone crates.
- **Unified Rust script tool:** `execute` lives at `Components/mentci-aid/src/bin/execute.rs`.
- **Script orchestration surface:** `Components/scripts/<name>/main.clj` with per-script `TESTING_CONTEXT.md` (mixed maturity; see Risks).
- **Filesystem ontology authority:** `Library/specs/ASKI_FS_SPEC.md`.
- **Source substrate:** `Sources/` (transitional alias: `Inputs/`) mounted read-only via jail tooling.

## 5. Current Operational Snapshot (♓︎.5.45.52 | 5919 AM)
- `dev` head: `2749f72` (`intent: execute section-6 cleanup and add one-subject rd synthesis`)
- `main` head (pre-release update): `3781ce0` (`intent: rescan and align core library readmes and high-priority docs`)

Recent high-signal commits:
1. `3781ce0` (main): Core/Library/high-priority consistency pass + restart refresh prep.
2. `7e24e16` (dev): script-surface deadweight/hard-wiring research (`Artifact-Sweep`).
3. `2749f72` (dev): section-6 cleanup (`Development/low`) + one-subject R&D synthesis.

Section sweep progress (`Development/high/Repo-Intent-Realignment/SECTION_SWEEP_PROGRAM.md`):
- Completed: sections `1` to `6`.
- Remaining: sections `7` to `10` (`Research/high`, `Research/medium`, `Research/low`, root files).

## 6. Guard Health Snapshot
Latest observed gate outcomes:
1. `validate_scripts`: passing.
2. `reference_guard`: passing.
3. `subject_unifier` (dry-run): passing.
4. `session_guard`: failing due trailing `intent:` chain above latest `session:`.
5. `root_guard`: failing on top-level drift (`outputs` directory, `result` file).

Interpretation:
- Policy/reference hygiene is mostly converging.
- Session finalization discipline and root contract closure remain the blocking control gates.

## 7. High-Importance Active Risks
1. **Control-plane divergence risk:** `main` and `dev` are both advancing; convergence protocol (`dev` -> finalized session -> release) is not yet consistently enforced.
2. **Session closure risk:** trailing intent chains degrade audit clarity and violate completion invariants.
3. **Top-level FS contract drift:** unresolved `outputs` and `result` conflict with root guard contract.
4. **Script-surface complexity risk:** overlapping script capabilities and hard-wired policy/path constants increase drift potential.

## 8. Immediate Priorities (Execution Order)
1. Close control-plane hygiene:
- re-establish single promotion flow (`dev` -> `main`) post-release.
2. Repair session protocol health:
- synthesize/close trailing `intent:` chains into protocol-compliant `session:` commits.
3. Resolve root contract drift:
- decide explicit contract handling for `outputs` and `result`.
4. Complete remaining section sweeps (`7` to `10`) with per-section research checkpoints.

## 9. Script-Surface Realignment Focus
From current `Artifact-Sweep` research:
1. Fix/deprecate stale-wired `admin_shell` bootstrap path.
2. Converge overlapping source-mount paths (`launcher` vs `sources_mounter`/`sources_remount`).
3. Reclassify optional utilities (`tool_discoverer`, `intent`, `session_metadata`) out of critical path.
4. Externalize guard policy allowlists/roots from code to data contracts.

## 10. Operational Requirements
- Any major component/protocol/input mapping changes must update this file and append `Outputs/Logs/RELEASE_MILESTONES.md`.
- Keep R&D mirror integrity (Development/Research counterpart coverage) when adding new subjects.

*The Great Work continues.*
