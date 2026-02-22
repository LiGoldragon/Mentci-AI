# Strategy: System Debugging

**Linked Goal:** `Goal 0: mentci-aid Stabilization`

## 1. Goal
Provide a deterministic protocol for resolving system-wide failures identified during sweeps.

## 2. Debugging Protocol
1.  **Isolation:** When a sweep fails, identify the specific component (Nix, Rust, Clojure).
2.  **Reproduction:** Run the failing test command identified in the tool's `TESTING_CONTEXT.md` in isolation.
3.  **Path Verification:** Check if the failure is due to recent structural changes (e.g., `scripts/` reorganization). Verify `load-file` and `PATH` exports.
4.  **Schema Alignment:** Verify that inputs/outputs still match `schema/mentci_tools.capnp`.
5.  **Log Analysis:** Consult `Logs/RELEASE_MILESTONES.md` to see what changed since the last successful sweep.

## 3. Tooling
- `cargo test`: Authority for core daemon logic.
- `bb <path/to/test.clj>`: Authority for orchestration glue.
- `nix build .#<attr>`: Authority for reachability and environment purity.

## 4. Reporting
Every debug session must culminate in an entry in `Logs/SYSTEM_SWEEP_REPORT.md` (even if it was a partial fix).

*The Great Work continues.*
