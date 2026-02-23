# Strategy: System Debugging

**Linked Goal:** `Goal 0: mentci-aid Stabilization`

## 1. Goal
Provide a deterministic protocol for resolving system-wide failures identified during sweeps.

## 2. Debugging Protocol
1.  **Isolation:** When a sweep fails, identify the specific component (Nix, Rust, Clojure).
2.  **Reproduction:** Run the failing test command identified in the tool's `TESTING_CONTEXT.md` in isolation.
3.  **Path Verification:** Check if the failure is due to recent structural changes (e.g., `scripts/` reorganization). Verify `load-file` and `PATH` exports.
4.  **Schema Alignment:** Verify that Sources/outputs still match `schema/mentci_tools.capnp`.
5.  **Log Analysis:** Consult `Outputs/Logs/RELEASE_MILESTONES.md` to see what changed since the last successful sweep.

## 3. Tooling
- `cargo test`: Authority for core daemon logic.
- `bb <path/to/test.clj>`: Authority for orchestration glue.
- `nix build .#<attr>`: Authority for reachability and environment purity.

## 4. Reporting
Every debug session must culminate in an entry in `Outputs/Logs/SYSTEM_SWEEP_REPORT.md` (even if it was a partial fix).

## 5. Sweep Program (Backfilled + Active)
Run this deterministic bug sweep for repository health checks:
1. `bb Components/scripts/validate_scripts/main.clj`
2. `bb Components/scripts/root_guard/main.clj`
3. `bb Components/scripts/session_guard/main.clj`
4. Script test batch:
   - `bb Components/scripts/tool_discoverer/test.clj`
   - `bb Components/scripts/interrupted_job_queue/test.clj`
   - `bb Components/scripts/sources_remount/test.clj`
5. Static marker scan:
   - `rg -n "TODO|FIXME|BUG|HACK|XXX" Components Core Library Development Research -S`
6. Path drift scan:
   - `rg -n "Inputs/|Sources/|tasks/high_level_goals/goal_1_attractor_dot_job_handoff.md" Core Library Development Research -S`

## 6. Current Known Issues from Sweep
1. Root guard drift remains:
   - `bb Components/scripts/root_guard/main.clj` fails on lowercase top-level `outputs`.
2. Historical path-text drift remains in some docs/strategies:
   - `Inputs/` references still appear where migration to `Sources` is transitional or incomplete.

## 7. Fix Workflow
1. Fix highest-severity failures first (`root_guard`, `session_guard`, broken task paths).
2. Commit each logical fix atomically.
3. Re-run full sweep after each fix batch.
4. Close prompt with session synthesis and a `Research/high/Debugging/` artifact.

*The Great Work continues.*
