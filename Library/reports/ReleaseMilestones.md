# Mentci-AI: Release Milestones and Goal Logs

This file maintains the durable record of session milestones and goal outcomes since the last major release tag.

## session: control-plane convergence slice for execute packaging and guard alignment
♓︎6°59'55" | 5919 AM

- **Nix Execute Reachability:** Fixed exported app path and package source context so `nix run .#execute -- <command>` resolves and executes.
- **Canonical Execute Surface:** Replaced legacy script-dispatch tests/checks with actor-subcommand contract checks (`root-guard`, `link-guard`, `session-guard`, `version`, `launcher`, `finalize`).
- **Guard Runtime Reconciliation:** Synced root-guard allowlists and Aski-FS root-contract docs with runtime artifacts (`.gemini`, `result`, `.mentci`, `tmp`).
- **Schema Authority Convergence:** Removed duplicated `mentci_box.capnp` copies and compile paths now target `Components/schema/` as single source of truth.

## session: core-library-rd high-importance context alignment and deep restart refresh
♓︎5°45'56" | 5919 AM

- **R&D Mirror Contract Formalization:** Added explicit mirror-model notes across authority and entry docs (`Core/AGENTS.md`, `Development/README.md`, `Research/README.md`) clarifying that R&D is the paired `Development/` + `Research/` topic system.
- **Goal/Queue Context Upgrade:** Added high-importance control context to `Core/HIGH_LEVEL_GOALS.md` and `Library/StrategyQueue.md` (mirror integrity + release-blocking control gates + sweep-progress context).
- **Deep Restart Context Rewrite:** Rebuilt `Library/RestartContext.md` with current heads, guard health, section-sweep progress, control-plane risks, and immediate realignment priorities.

## session: core-library-readme-high-priority consistency sweep and restart refresh
♓︎5°24'35" | 5919 AM

- **Authority Consistency Sweep:** Rescanned `Core/`, `Library/`, top-level README surfaces, and high-priority development/research tracks for path and naming drift.
- **Alias Correction:** Repaired transitional alias wording from broken `Sources -> Sources` phrasing to explicit `Inputs -> Sources` migration notes across core authority and FS spec files.
- **High-Priority Path Normalization:** Updated active high-priority strategy docs to canonical `Outputs/Logs/`, `Development/`, `Components/tasks/`, and `Research/high/...` references.
- **Restart Context Refresh:** Updated `Library/RestartContext.md` metadata, operational snapshot, guard status, and active risk list to current repository state.

## session: sources-root canonicalization across repository contracts
♓︎5°12'51" | 5919 AM

- **Filesystem Root Migration:** Renamed the operational source substrate root from `Inputs/` to `Sources/` and moved the mounted directory accordingly.
- **Nix Contract Migration:** Renamed jail contract keys from `inputsPath`/`inputManifest` to `sourcesPath`/`sourceManifest`.
- **Script and Guard Migration:** Renamed and updated source mount utilities (`sources_mounter`, `sources_remount`) plus launcher/root guard references to `Sources`.
- **Authority and Strategy Sync:** Updated FS authority docs and top-level FS strategy to enforce `Sources` as the sole canonical source-root name.

## release: v0.12.3.58.3 - harden architecture and establish strategy system
♓︎3°58'3" | 5919 AM

- **Core Consolidation:** Migrated authoritative architectural files to `Core/` directory.
- **Programming Versioning:** Implemented content-addressed versioning for agent logic (scripts/program_version.clj).
- **Strategy System:** Established `strategies/` system with prioritized queue and development hardening loop.
- **Aski-FS Spec:** Formalized filesystem ontology (Inputs/Outputs/Components) with Mermaid visual logic.
- **Infrastructure:** Setup `nix-direnv` and automated Emacs RO-purity hooks.
- **Sanitization:** Purged binary artifacts and established strict "No Binaries" rules.
- **Script Hardening:** Reorganized orchestration scripts into autonomous directories with schemas and tests.
- **Artifact Purge:** Implemented and executed `strategies/Artifact-Sweep/src/sweep.clj`, surgically removing redundant 'Canonical Aski framing' blocks from 80+ files.

## [Previous Historical Logs Elided for Brevity - see git history]
♓︎.6.8.53 5919AM | v0.12.6.8.53 | Major structural shift: Rust Actor Orchestration, Logic-Data Separation, and Full Repository Sweep complete. Deleted legacy Babashka scripts.
