# Chronos default format + execute devshell + chronos-lib wiring

## Prompt
- make chronos output AM-second format by default for direct invocation
- ensure execute is in devshell
- make execute depend on a new `chronos-lib` component

## What was changed
1. Added new component `Components/chronos-lib`
   - Provides `chronos_lib::apply_defaults` and default profile (`--format am --precision second`).
   - Ships a `chronos` binary reusing existing solar chronology logic with default args auto-applied.
   - Includes tests for default argument merge and stable unix-epoch output.

2. Nix wiring
   - `Components/nix/chronos.nix` now builds `chronos` from `Components/chronos-lib` (`--bin chronos`).
   - Added `Components/nix/chronos-lib.nix` package.
   - Exposed `chronosLib` in `flake.nix` package outputs.

3. execute + devshell
   - Added `execute` to `Components/nix/common_packages.nix` so it is present in devshell PATH.
   - Updated `Components/nix/execute.nix` to depend on `chronos_lib` via propagated build inputs.

4. component registries
   - Added `chronos-lib` entries to:
     - `Components/index.edn`
     - `Components/stability-index.edn`
     - `Components/contracts/rust-component-repos.toml`

## Validation evidence
- `cargo test --manifest-path Components/chronos-lib/Cargo.toml` passed.
- `nix run .#chronos -- --unix 0` -> `5863.10.11.10.13` (AM-second profile by default).
- `nix build .#execute --no-link` passed.
- `nix develop . --command bash -lc 'command -v execute >/dev/null && execute version >/dev/null && chronos --unix 0'` passed.
