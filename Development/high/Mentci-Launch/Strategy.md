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
5. Primary agent front-end defaults to TypeScript `pi` (`codingAgent`) until Rust parity is proven.

## 3. Data Purity Rule (Very High Importance)
1. `mentci-launch` accepts exactly one launch-init object.
2. The launch-init object points to a Cap'n Proto Mentci-Box request object.
3. Terminal/service behavior, environment map, and test profile are all encoded in schema data.
4. Launcher code does not compose launch behavior from ad-hoc env switches.

## 4. Architecture Shape
1. **Schema authority:** `Components/schema/mentci.capnp` (`MentciLaunchRequest`).
2. **Component:** `Components/mentci-launch/` (planned).
3. **Execution modes:**
   - `terminal`: invoke terminal process command via systemd user scope.
   - `service`: install/launch user service unit with init envelope path.
4. **Delegation:** launcher always delegates sandbox execution to `mentci-box`.

## 5. Integration Points
1. `mentci-aid execute` adds a launch subcommand that forwards Cap'n Proto request path.
2. Nix package exports:
   - `mentciLaunch` (future package),
   - optional app alias for launch testing.
3. Test harness links:
   - future interactive TUI test profile for agent-driven session checks.

## 6. Acceptance Criteria
1. `mentci-launch` can open `mentci-box` in a new `foot` terminal using systemd user scope.
2. Service-mode launch path is reproducible and auditable.
3. All launch semantics originate in `MentciLaunchRequest`.
4. No launch-domain behavior requires ad-hoc env vars.
5. Documentation states TypeScript `pi` as default operator interface for this phase.

## 7. Risks
1. Terminal availability drift (`foot` absent).
2. User-session systemd availability drift.
3. Interactive TUI tests require robust pseudo-terminal orchestration.

## 8. Risk Mitigations
1. Schema includes terminal command override while default remains `foot`.
2. Add explicit validation checks for `systemd --user` readiness.
3. Keep launch mode and test profile explicit in Cap'n Proto request object.
