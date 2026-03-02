# Strategy: Mentci Launch Component

**Linked Goal:** `Goal 0: mentci-aid Stabilization`

## 1. Objective
Design and deliver a dedicated `mentci-launch` component that starts `mentci-box` in a new terminal through systemd, with all launch state sourced from one Cap'n Proto init message.

## 2. Core Requirements
1. Launch target supports both:
   - interactive terminal (`systemd-run --user --scope`),
   - managed service unit (`systemd --user` service mode).
2. Default interactive terminal is `foot`.
3. Launch data must be read exclusively from `MentciLaunchRequest`.
4. Environment-variable domain configuration is forbidden when the init message carries the value.
5. Primary agent front-end defaults to TypeScript `pi` (`pi`) until Rust parity is proven.

## 2.1. Test-Driven Transition
A specialized `execute transition` command exists to refresh the developer environment.
1.  **Mandatory Checks:** Transition only occurs if `root-guard`, `link-guard`, and `session-guard` pass.
2.  **Terminal Spawning:** The transition uses `mentci-launch` to spawn a new `foot` terminal session via `systemd --user`.
3.  **UI Integrity:** This model avoids terminal multiplexing/recursion glitches by opening a fresh, OS-managed window.

## 3. Data Purity Rule (Very High Importance)
1. `mentci-launch` accepts exactly one launch-init object.
2. The launch-init object points to a Cap'n Proto Mentci-Box request object.
3. Terminal/service behavior, environment map, and test profile are all encoded in schema data.
4. Launcher code does not compose launch behavior from ad-hoc env switches.

## 4. Architecture Shape
1. **Schema authority:** `Components/schema/mentci.capnp` (`MentciLaunchRequest`).
2. **Component:** `Components/mentci-launch/` (implemented).
3. **Execution modes:**
   - `terminal`: invoke terminal process command via systemd user scope.
   - `service`: install/launch user service unit with init envelope path.
4. **Delegation:** launcher always delegates sandbox execution to `mentci-box`.

## 5. Integration Points
1. `mentci-aid execute` adds `transition` and `launcher` subcommands.
2. Nix package exports:
   - `mentciLaunch` (package),
   - `execute` subcommands for launch orchestration.

## 6. Acceptance Criteria
1. `mentci-launch` can open `mentci-box` in a new `foot` terminal using systemd user scope.
2. Service-mode launch path is reproducible and auditable.
3. All launch semantics originate in `MentciLaunchRequest`.
4. No launch-domain behavior requires ad-hoc env vars.

## 7. Risks
1. Terminal availability drift (`foot` absent).
2. User-session systemd availability drift.
3. Interactive TUI tests require robust pseudo-terminal orchestration.

## 8. Risk Mitigations
1. Schema includes terminal command override while default remains `foot`.
2. Add explicit validation checks for `systemd --user` readiness.
3. Keep launch mode and test profile explicit in Cap'n Proto request object.
