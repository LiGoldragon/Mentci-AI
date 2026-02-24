# Research Artifact: Implement Mentci-Launch Component and Tests

- **Solar:** ♓︎ 7° 2' 35" | 5919 AM
- **Subject:** `Mentci-Launch`
- **Title:** `implement-mentci-launch-component-and-tests`
- **Status:** `finalized`

## 1. Intent
Execute implementation and verification for the new `mentci-launch` component planned in the prior strategy slice.

## 2. Implemented Surface
1. Added new Rust crate:
   - `Components/mentci-launch/`
   - `Cargo.toml`, `build.rs`, `src/lib.rs`, `src/main.rs`
2. Implemented launch-domain objects:
   - `LaunchRequest` (Cap'n Proto decode)
   - `LaunchPlan` (systemd command synthesis)
   - `MentciLaunch` (runner execution orchestration)
3. Implemented mode contracts:
   - terminal mode requires `userScope`
   - service mode requires `userService` + non-empty `serviceUnitName`
4. Terminal default behavior:
   - default program `foot`
   - default command synthesis uses `-e mentci-box <box-request.capnp>` when terminal args are empty

## 3. Testing
1. Unit tests cover:
   - terminal launch-plan shape
   - service launch-plan shape
   - invalid mode/target combinations
   - missing service unit validation
   - launcher runner contract
2. Integration test covers:
   - packed Cap'n Proto request encode/decode
   - request-to-plan conversion path

## 4. Ecosystem Integration
1. Added workspace membership for `Components/mentci-launch`.
2. Added component registry entry in `Components/index.edn`.
3. Added Nix package wiring:
   - `Components/nix/mentci_launch.nix`
   - `flake` package export `mentciLaunch`
   - `flake` app export `mentci-launch`
4. Updated README usage and structure notes.

## 5. Verification Summary
- `nix develop --command cargo test -p mentci-launch` passed.
- `nix develop --command cargo test --workspace` passed (expected ignored tests remain ignored).
- `nix build .#mentciLaunch` passed.
- `nix run .#mentci-launch -- --help` passed.
- Guard suite passed (`root-guard`, `link-guard`, `session-guard`, `unify` dry run).
