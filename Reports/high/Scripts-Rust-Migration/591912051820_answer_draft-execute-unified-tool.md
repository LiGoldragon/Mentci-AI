# Answer Artifact

- Subject: `Scripts-Rust-Migration`
- Kind: `answer`
- Prompt: `draft execute with the functionality of all the current scripts, written in Core/SEMA_RUST_GUIDELINES.md`

## Summary
Implemented a first Rust draft of the consolidated tool `execute` as a single entrypoint that exposes all current script functionality through one command surface while preserving behavior by delegating to existing script entrypoints.

## Implementation
1. Added `Components/src/bin/execute.rs`:
- discovers all script entrypoints under `Components/scripts/*/main.clj`
- exposes:
  - `execute list`
  - `execute <script> [args...]`
- delegates execution to `bb <script>/main.clj ...` for compatibility parity.

2. Added tests `Components/tests/execute_tool.rs`:
- list behavior includes valid script entrypoints and excludes non-entry directories.
- unknown script handling returns failure with explicit error.

3. Added shell command exposure:
- `Components/nix/common_packages.nix` adds `execute` command in dev shell, routed through `mentci-jail-run`.

4. Updated usage docs:
- `README.md` includes `execute` command usage summary.

## Notes
This draft is the migration bridge: one Rust tool surface with full script coverage, while CLJ implementations remain authoritative until parity-gated rewrite of each script domain completes.
