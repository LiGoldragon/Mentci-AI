# Research Artifact: Strategy for Mentci Launch via Systemd + Foot

- **Solar:** ♓︎ 6° 60' 56" | 5919 AM
- **Subject:** `Mentci-Launch`
- **Title:** `strategy-mentci-launch-systemd-foot-capnp-init`
- **Status:** `finalized`

## 1. Intent
Define the strategy and specification direction for a dedicated launcher component (`mentci-launch`) that opens `mentci-box` in a dedicated terminal and later supports full interactive suite testing.

## 2. Strategic Decisions
1. Introduce `MentciLaunchRequest` as Cap'n Proto init envelope authority.
2. Default terminal program is `foot`.
3. Launch path is systemd-first (`systemd-run --user --scope` for terminal mode).
4. Service mode is included for persistent launch workflows.
5. Domain-state launch data is sourced from init message only (no env-spread config).

## 3. Data Purity Elevation
1. Core architecture now treats Cap'n Proto init-envelope transport as a very high importance rule.
2. Per-language SEMA guidelines (Rust, Clojure, Nix) now include explicit init-envelope purity requirements.
3. Library launch spec captures the same rule as implementation contract.

## 4. Operator Interface Decision
Current default operator interface is `pi` TypeScript (`codingAgent`) due observed stability in current repository testing. Rust `pi` remains available as non-default validation lane.

## 5. Implementation Readiness Outcome
The repository now has a dedicated high-priority strategy track for `Mentci-Launch`, a schema contract for launch-init objects, and documentation links for future component implementation and interactive TUI suite integration.
