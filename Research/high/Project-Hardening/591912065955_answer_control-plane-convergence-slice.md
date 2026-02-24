# Research Artifact: Control-Plane Convergence Slice

- **Solar:** ♓︎ 6° 59' 55" | 5919 AM
- **Subject:** `Project-Hardening`
- **Title:** `control-plane-convergence-slice`
- **Status:** `finalized`

## 1. Intent
Execute a control-plane convergence slice before new feature work, with hard gates on Nix package reachability, execute interface consistency, guard health, and schema authority.

## 2. Implemented Changes
1. **Nix packaging fix for `execute` path**
   - Updated `Components/nix/mentci_ai.nix` to build from repository root with `--manifest-path Components/mentci-aid/Cargo.toml` so path dependencies resolve in Nix builds.
   - Updated `flake.nix` `apps.execute` with `exePath = "/bin/execute"` so `nix run .#execute -- <command>` resolves the correct binary.

2. **Canonical `execute` interface alignment**
   - Rewrote `Components/mentci-aid/tests/execute_tool.rs` to validate actor-subcommand behavior instead of legacy script-dispatch behavior.
   - Updated `Components/nix/execute_check.nix` to check command catalog and `execute version` output.
   - Updated `README.md` usage to reflect exported `execute` app usage and removed unsupported `execute sandbox` call.
   - Updated `Development/high/Scripts-Rust-Migration/Strategy.md` with explicit canonical command-surface truth and retirement of `execute list`/`MENTCI_SCRIPTS_DIR`/`MENTCI_BB_BIN`.

3. **Guard baseline convergence**
   - Updated `Components/mentci-aid/src/actors/root_guard.edn` allowlists to include runtime `.gemini` directory and `result` symlink.
   - Synced root-contract docs with runtime reality:
     - `Library/specs/AskiFsSpec.md`
     - `Library/specs/AskiFsRootContract.edn`

4. **One-source schema authority for `mentci_box.capnp`**
   - Updated build scripts to compile Cap'n Proto from `Components/schema/` only:
     - `Components/mentci-aid/build.rs`
     - `Components/mentci-box-lib/build.rs`
   - Removed duplicate local schema copies:
     - `Components/mentci-aid/schema/*.capnp`
     - `Components/mentci-box-lib/schema/mentci_box.capnp`

## 3. Gate Verification
- `nix run .#execute -- root-guard` passes.
- `nix build .#checks.x86_64-linux.execute` passes.
- `nix develop --command cargo test --workspace` passes (ignored tests remain ignored by design).
- `nix run .#execute -- link-guard` passes.
- `nix run .#execute -- session-guard` passes.

## 4. Outcome
The control-plane now has a coherent execution surface across package wiring, tests, checks, and documentation, with schema authority consolidated into `Components/schema/`.
