- Prompt: `each rust component has its own cargo and build files, the only file (non-dir) in Components/ right now should be Components/index.edn`
- Date: `5919.12.5.19.36`

## Outcome
Implemented the Rust component split so `Components/` root now contains only `Components/index.edn` as a file, and each Rust component owns its local Cargo/build metadata.

## Implemented Changes
1. Moved daemon crate files into `Components/mentci-aid/`:
- `Cargo.toml`, `Cargo.lock`, `build.rs`
- `src/*` (main + loader/bootstrap modules)
- Rust integration tests moved to `Components/mentci-aid/tests/*`

2. Moved chronos crate into `Components/chronos/`:
- `Cargo.toml`, `Cargo.lock`
- `src/main.rs`

3. Removed root Rust build files from `Components/`:
- removed `Components/Cargo.toml`
- removed `Components/Cargo.lock`
- removed `Components/build.rs`

4. Made `mentci-aid` crate self-contained for Nix builds:
- added `Components/mentci-aid/schema/atom_filesystem.capnp`
- added `Components/mentci-aid/schema/mentci.capnp`
- updated `Components/mentci-aid/build.rs` to compile local `schema/*`

5. Updated runtime and tooling paths:
- Nix bootstrap command now points to `Components/mentci-aid/Cargo.toml`
- Chronos fallback paths now point to `Components/chronos/Cargo.toml`
- Updated spec/docs/workflow references away from `Components/src/*` and root `Components/Cargo.toml`

6. Fixed Nix jail wiring regression discovered during validation:
- `flake.nix` now passes `Sources = namespace.jail_sources` to `Components/nix/jail.nix`
- `Components/nix/jail.nix` shell hook now references `../scripts/launcher/main.clj`

## Verification
- `find Components -maxdepth 1 -type f` returns only `Components/index.edn`.
- `cargo run --manifest-path Components/chronos/Cargo.toml -- --format am --precision second` passes.
- `nix develop -c cargo test --manifest-path Components/mentci-aid/Cargo.toml --test mentci_bootstrap` passes.
- `nix build .#mentciAi` passes.
