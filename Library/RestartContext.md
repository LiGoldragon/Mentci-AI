# Agent Session Resumption Context (Restart Context)

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `Library/architecture/AskiPositioning.md`.

**Metadata:**
*   **Target Authority:** Top Admin, Li Goldragon.
*   **Primary Working Bookmark:** `dev`.
*   **Current Programming Version:** `(will be updated by commit hook or manually)`
*   **Latest Release Tag:** `v0.12.9.59.28` (current milestone).

## 1. Project Overview
Mentci-AI is a Level 5 "Dark Factory" AI daemon (`mentci-aid`) under stabilization. The repository now operates with a native Rust actor-based orchestration layer (`execute`) and strict Logic-Data separation.

## 1.1 Transition Summary Since Last Tagged Release (`v0.12.8.5.55`)
- Introduced Cap'n Proto component-local schema enforcement and hash-synced binary message protocol as normal operating contract.
- Integrated VTCode into Nix tooling lane with Mentci-specific stability and smoke-test checks.
- Implemented Aski Projector workflow (EDN -> Cap'n Proto text -> packed binary) in Rust via `aski-lib` + `mentci-mcp`.
- Migrated key component data authorities (`mentci-stt`, `mentci-user`, `mentci-intel`) to EDN sidecar sources.
- Strengthened release protocol toward signed tagging and dual chronographic formatting.
- Established **Logical Editing** (AST-aware) and **Search Intelligence** (Linkup) as primary symbolic tools.
- Implemented **Mirror Hook** protocol to eliminate agent-UI synchronization hallucinations.

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
- **Symbolic Discovery Surface:** `logical-edit` (AST queries) and `linkup` (web validation) are the canonical discovery tools. Agents must prefer `logical_run_query` over `grep` for code analysis.

## 5. Current Operational Snapshot (♓︎.9.59.28 | 5919 AM)
- **Terminology Alignment:** Officially marked "sajban" as deprecated for "sema" (the machine-code symbolic language). **Sajban** is now the name for the natural-language **Aski** (`aski-sajban`), which is self-loading from SEMA binary.
- **Component Locality:** Cap'n Proto schemas now live within component directories. Architectural guidelines enforce SHA-256 hash synchronization between binary and text message versions.
- **Aski Projector + Native Sync:** `aski-lib` now projects EDN into Cap'n Proto text and `mentci-mcp` exposes `capnp_sync_protocol` for hash-synced `.bin` generation without ad-hoc shell scripts.
- **Spec-Source Migration:** `mentci-stt`, `mentci-user`, and `mentci-intel` now use EDN text source artifacts (`*.edn`) as the editable authority feeding prebuilt Cap'n Proto binaries.
- **Pure Nix devShell:** `mentci-user` bootstraps environment variables purely within the Nix shell logic, removing the need for external shell wrappers.
- `dev` lineage continues to enforce the **Rust-Only Mandate**, officially deprecating Python, Clojure, and ad-hoc shell patching. Ad-hoc extraction scripts must now be explicitly marked for rewriting into Sema-grade components like `mentci-dig`.
- **UI Abstraction Standardization:** All User Interfaces (Pi, VTCode, Gemini) strictly separate Logic and Data. Secrets (`GEMINI_API_KEY`, etc.) are injected purely via `mentci-user export-env`, never via local `.env` files.
- **VTCode Integration:** The Rust-native semantic terminal coding agent `vtcode` is integrated into the `nix develop` environment via the `mentci-vtcode` wrapper, ready for trial alongside `pi`.
- **Philosophical Alignment:** The "Local Machine of Fit" mandate is deeply codified (`Core/PHILOSOPHY_OF_INTENT.md`), explicitly rejecting manipulative AI governance and enforcing local, patient agent execution.
- **Data-Logic Separation:** Hardcoding API keys or configuration strings in logic is a Severe Violation. `mentci-user` dynamically fetches secrets via `gopass`.
- **Programmatic Refactoring:** Regex/string file patching is forbidden. We are moving to AST/CST lossless transformations via the `mentci-mcp` Rust server, the `logical-edit` query engine, and the newly scaffolded `mentci-dig` component.
- **Web-Aware Validation:** All ecosystem and maturity claims must be verified via `linkup` before being presented to the user.

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
