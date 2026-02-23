# Session Report

- Kind: `answer`
- Subject: `Top-Level-FS-Spec`
- Title: `move-rust-manifest-to-components`

## Prompt
move them then

## Outcome
Moved Rust crate metadata from repository root into `Components/` and rewired build/runtime tooling.

### Moved
- `Cargo.toml` -> `Components/Cargo.toml`
- `Cargo.lock` -> `Components/Cargo.lock`
- `build.rs` -> `Components/build.rs`

### Updated
- `Components/build.rs` now references `schema/...` relative to `Components/`.
- `flake.nix` uses `craneLib.cleanCargoSource ./Components`.
- `Components/nix/common_packages.nix` bootstrap command now uses:
  - `cargo run --manifest-path Components/Cargo.toml ...`
- Root-file policy/docs adjusted so cargo files are no longer expected at root.

### Validation
- `cargo metadata --manifest-path Components/Cargo.toml --no-deps` -> success.
- `execute root-guard` -> pass.
