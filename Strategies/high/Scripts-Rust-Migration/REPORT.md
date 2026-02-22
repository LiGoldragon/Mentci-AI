# Strategy Report: Scripts Rust Migration

## Status
Drafted.

## Current Output
- Migration objective defined for all `Components/scripts/*` entrypoints.
- Risk-based rollout order defined.
- Per-script acceptance gate model defined.
- Dual-runtime compatibility policy defined for safe cutover.
- Explicit authority rule set: CLJ remains default until Rust passes identical shared tests.
- Dual-run parity harness requirement added to roadmap.
- Consolidated architecture rule added: one primary Rust tool with subcommands; standalone bins only when independently useful.
- Primary tool naming set to `execute`.
- Draft `execute` binary added as unified surface over current script entrypoints.
- Added integration tests and Nix wiring for `execute` (`packages.execute`, `apps.execute`, `checks.execute`).

## Next Action
Add parity harness integration so `execute` can run CLJ-vs-Rust comparison modes per script domain.
