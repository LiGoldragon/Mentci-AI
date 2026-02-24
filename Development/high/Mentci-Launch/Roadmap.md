# Roadmap: Mentci Launch Component

## Phase 1: Spec and Contract
1. Define `MentciLaunchRequest` in `Components/schema/mentci.capnp`.
2. Publish launch behavior spec in `Library/specs/MentciLaunchSpec.md`.
3. Record default operator interface (`pi` TypeScript) in architecture docs.

## Phase 2: Component Scaffold
1. Create `Components/mentci-launch/` crate.
2. Add Cap'n Proto decode path for `MentciLaunchRequest`.
3. Add validation for required request fields and launch mode routing.

## Phase 3: Terminal Launch Path
1. Implement `systemd-run --user --scope` launch path.
2. Default terminal command to `foot`.
3. Route execution to `mentci-box` with request object path.

## Phase 4: Service Launch Path
1. Implement user-service mode execution.
2. Add unit naming and lifecycle controls from request object fields.
3. Persist launch result metadata in `Outputs/Logs/`.

## Phase 5: Test Harness Integration
1. Add integration tests for terminal and service modes.
2. Add dedicated profile for interactive TUI validation.
3. Extend `execute` with launcher forwarding command when component is mature.

## Exit Criteria
1. Terminal and service launch paths are deterministic.
2. Data purity rule is satisfied (Cap'n Proto envelope only).
3. `mentci-box` dedicated terminal runs can support full suite and interactive test preparation.
