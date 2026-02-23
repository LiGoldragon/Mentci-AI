# Scripts Rust Migration Roadmap

## Phase 0: Baseline Inventory
1. Freeze script inventory (`Components/scripts/*/main.clj`) and classify by risk.
2. Capture invocation matrix:
- required args
- env var dependencies
- file IO boundaries
- external command dependencies (`jj`, `nix`, `gopass`, `ssh`, etc.)

## Phase 1: Rust Tooling Foundation
1. Build one primary Rust tool binary named `execute` with subcommand routing for script domains.
2. Add shared Rust utility modules for:
- command execution
- argument parsing
- structured errors
- JSON/EDN interop where needed
3. Define common CLI output/error conventions equivalent to current scripts.
4. Add shared dual-run parity harness:
- one fixture corpus consumed by both CLJ and Rust implementations
- normalized comparison for stdout/stderr/exit and side-effect artifacts.
5. Define standalone binary exception criteria and registry (example baseline: `chronos`).

## Phase 2: Low-Risk Script Port
1. Port utility scripts first.
2. Add golden tests for each migrated script.
3. Keep CLJ path as default authority until Rust passes the shared parity suite.
4. Maintain wrapper entrypoints to prevent caller breakage.

## Phase 3: Guard and Report Layer
1. Port guards and report generators.
2. Run repo guard sweep after each script migration.
3. Stabilize release/session compliance via Rust path.

## Phase 4: Runtime and Jail Orchestration
1. Port source/jail setup scripts.
2. Validate in both dev shell and jail execution contexts.
3. Verify key paths (`Sources`, `Outputs`, `.mentci`) remain contract-compatible.

## Phase 5: VCS-Critical Workflows
1. Port `commit` and `jj_workflow` last.
2. Add integration tests against local JJ repo fixtures.
3. Validate push-policy enforcement and bookmark safety invariants.

## Phase 6: Decommission Babashka Script Runtime
1. Switch default invocations to Rust binaries only after parity gate passes per script.
2. Keep CLJ implementations callable as rollback path during one stable release cycle.
3. Remove wrappers and unused Clojure script code only after no parity regressions in that window.
4. Keep one-tool architecture as default; add standalone bins only with documented independent value.
