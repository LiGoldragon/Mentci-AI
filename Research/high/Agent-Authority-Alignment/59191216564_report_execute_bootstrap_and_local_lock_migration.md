# Execute/Bootstrap Decoupling + Local Lock Migration

## What `mentci-aid` was dependency for
`execute` is currently compiled from the `mentci-aid` crate (`src/bin/execute.rs`) and imports orchestrator actors from `mentci_aid::actors::{Orchestrator, SymbolicMessage}`. That is why `execute` tracks `mentci_aid_src` today.

## Decoupling changes completed
1. Added minimal bootstrap component package:
   - `Components/nix/mentci_bootstrap.nix`
   - Exposes `mentci-bootstrap launcher` (delegates to `mentci-launch`) and `version`.
2. Removed `execute` from default devshell package set:
   - `Components/nix/common_packages.nix` now includes `mentci_bootstrap` instead.
3. Updated jail startup to use the minimal bootstrap command:
   - `Components/nix/jail.nix` now runs `mentci-bootstrap launcher`.
4. Exported bootstrap package/app through flake:
   - `flake.nix` packages: `mentciBootstrap`
   - `flake.nix` apps: `mentci-bootstrap`

This isolates build/startup surface from frequent `execute` iteration.

## Local lock migration completed
Added component-local lock artifacts under `Components/nix/locks/` and rewired derivations:
- `mentci_ai.Cargo.lock`
- `execute.Cargo.lock`
- `chronos.Cargo.lock`
- `mentci_box.Cargo.lock`
- `mentci_user.Cargo.lock`
- `mentci_mcp.Cargo.lock`
- `mentci_stt.Cargo.lock`
- `mentci_launch.Cargo.lock`

All target derivations now consume lock files from `Components/nix/locks` instead of `../../Cargo.lock` or mixed source-root lock references.

## Validation sequence
1. Built target package set successfully:
   - `mentciAi`, `execute`, `mentciBootstrap`, `chronos`, `mentciStt`, `mentciUser`, `mentciMcp`, `mentciLaunch`, `mentciBox`, `mentciBoxDefault`, `pi`, `piWithExtensions`, `unifiedLlm`, `vtcode`.
2. Tiny unrelated commit #1 (docs punctuation) -> dry-run build: no scheduled rebuilds.
3. Tiny unrelated commit #2 (text hyphenation) -> dry-run build: no scheduled rebuilds.

## Remaining architectural next step
`execute` is still sourced from `mentci-aid` codebase. Full source-authority decoupling would require a dedicated `mentci-execute` repository/input and moving the `execute` actor/control surface there.
