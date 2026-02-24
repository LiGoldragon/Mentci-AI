# Mentci Launch Specification

**Status:** Draft / High Priority
**Objective:** Define a dedicated launcher surface for opening Mentci-Box in a new terminal through systemd-controlled execution, with all runtime data carried in one Cap'n Proto init message.

## 1. Component Intent
- Planned component: `Components/mentci-launch/`.
- Role: decode `MentciLaunchRequest` and start `mentci-box` in either:
  - `terminal` mode (interactive terminal process), or
  - `service` mode (long-running managed unit).

## 2. Canonical Init Envelope
Authority schema: `Components/schema/mentci.capnp`.

Primary object: `MentciLaunchRequest`.

Required semantics:
1. Launch intent and identity (`runId`, `testProfile`).
2. Mentci-Box request location (`boxRequestCapnpPath`).
3. Execution location (`workingDirectory`).
4. Launch transport (`launchMode`, `systemdTarget`).
5. Terminal behavior (`terminalProgram`, `terminalArgs`).
6. Service behavior (`serviceUnitName`).
7. Process environment map (`environment`) as structured key/value entries.
8. Agent front-end selection (`agentInterface`).

## 3. Default Behaviors
1. `launchMode`: `terminal`.
2. `systemdTarget`: `userScope` (`systemd-run --user --scope`).
3. `terminalProgram`: `foot`.
4. `agentInterface`: `piTypescript`.

## 4. Data Purity Contract (Very High Importance)
1. Domain configuration is read only from `MentciLaunchRequest`.
2. Environment variables are process-layer concerns only.
3. No launch-domain logic may depend on ad-hoc env inputs when data exists in the Cap'n Proto envelope.
4. This applies equally across Rust, Clojure, and Nix boundaries.

## 5. Testing Direction
`mentci-launch` will become the standard path for dedicated terminal test sessions of `mentci-box`:
1. full integration test suites;
2. interactive TUI test sessions when agent capabilities support terminal interaction;
3. reproducible terminal/service launch paths through systemd.

## 6. Agent Interface Default
Current default operator interface is `pi` TypeScript (`pi` package). The Rust `pi` variant remains available but non-default until parity and stability are proven by test evidence.
