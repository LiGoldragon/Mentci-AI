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

## 5. Current Operational Snapshot (♓︎.6.4.45 | 5919 AM)
- `dev` head: `0cd7a9f` (`session: harden VCS mandates and finalize migration research`)

Section sweep progress (`Development/high/Repo-Intent-Realignment/SectionSweepProgram.md`):
- Completed: sections `1` to `10` (Full Repository Sweep).
- Current Status: All core and library documentation synchronized with refined authority tiers.

## 6. Guard Health Snapshot
Latest observed gate outcomes:
1. `root-guard`: passing.
2. `link-guard`: passing.
3. `session-guard`: passing.
4. `unify` (dry-run): passing.

Interpretation:
- The transition to native Rust actors for repository integrity has been successful.
- Obsolete Clojure scripts have been removed, simplifying the orchestration surface.

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
- Any major component/protocol/input mapping changes must update this file and append `Outputs/Logs/ReleaseMilestones.md`.
- Keep R&D mirror integrity (Development/Research counterpart coverage) when adding new subjects.

*The Great Work continues.*
