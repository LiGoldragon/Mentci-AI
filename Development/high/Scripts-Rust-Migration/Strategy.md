# Strategy: Scripts Rust Migration

**Linked Goal:** `Goal 0: mentci-aid Stabilization`

## 1. Goal
Migrate `Components/scripts/*` operational logic from Babashka/Clojure to Rust without interrupting current jail/bootstrap workflows.

## 2. Scope
- In scope:
  - All script entrypoints under `Components/scripts/<name>/main.clj`.
  - Shared script utilities in `Components/scripts/lib/`.
  - Script validation and execution wiring in Nix/runtime launch paths.
- Out of scope:
  - Rewriting `Sources/` code.
  - Non-script Rust components already in `Components/src/`.

## 3. Non-Negotiable Constraints
1. No big-bang rewrite.
2. One script domain migrated at a time with rollback path.
3. Runtime behavior parity required before default-switch.
4. Clojure implementation remains authoritative until Rust passes the same test corpus.
5. Existing CI/validation sweeps must stay green after each migration slice.

## 4. Migration Architecture
1. Default to one consolidated Rust tool for script replacements:
- single primary binary named `execute` with subcommands matching script domains.
- avoid one-bin-per-script unless a binary is independently useful on its own.
2. Standalone binary exception rule:
- create dedicated binaries only for tools with clear independent utility across contexts (example: `chronos`).
3. Keep a dual-runtime bridge phase:
- Existing `bb` entrypoints stay callable.
- Each migrated script maps to a Rust subcommand and a compatibility wrapper.
4. Move object contracts to schema-first boundaries:
- Reuse existing Cap'n Proto and JSON contracts where present.
- Replace ad-hoc shell/text parsing with typed Rust request/response objects.
5. Preserve CLI contract stability:
- Argument shapes and exit semantics remain compatible unless an explicit migration note is published.

## 5. Rollout Order (Risk-Based)
1. Low-risk pure utilities:
- `program_version`, `solar_prefix`, `test_deps`, `tool_discoverer`.
2. Validation and reporting:
- `validate_scripts`, `answer_report`, `reference_guard`, `root_guard`, `session_guard`.
3. Workflow orchestration:
- `session_finalize`, `session_metadata`, `intent`, `aski_flow_dot`, `component_registry`.
4. Jail and source orchestration:
- `sources_mounter`, `sources_remount`, `launcher`, `mk_shell`, `agent_launcher`, `jail_key_setup`.
5. VCS-critical paths:
- `commit`, `jj_workflow` (last; highest blast radius).

## 6. Acceptance Gates Per Script
1. Dual-run parity tests (mandatory):
- Keep one shared test corpus per script and execute it against both implementations.
- Run shape:
  - `script-parity <script> --impl clj`
  - `script-parity <script> --impl rust`
- Rust may only advance if all shared tests pass with equivalent observable behavior.
2. Golden behavior diff:
- Capture baseline stdout/stderr/exit code for representative invocations.
- Rust candidate must match expected behavior or ship documented intentional delta.
3. Contract tests:
- Required success and failure-path tests.
4. Integration sweep:
- `nix develop -c bb Components/scripts/validate_scripts/main.clj` stays green during dual-runtime phase.
5. Operational drill:
- For workflow-critical scripts, run end-to-end smoke command using repo-standard flows.
6. Authority gate:
- If parity and shared-test equivalence are incomplete, route default execution to Clojure path.

## 7. Cutover Policy
1. Mark script as `rust-primary` only after passing all gates and shared-test equivalence.
2. Keep Clojure implementation and wrapper callable while Rust is canary.
3. Remove Clojure path only after one stable release window with no parity regressions.

## 8. Key Risks and Mitigations
1. Risk: behavior drift in shell-heavy workflows.
- Mitigation: golden test fixtures + explicit parity checklist.
2. Risk: migration blocks active development due to broad churn.
- Mitigation: narrow vertical slices (one script domain per session).
3. Risk: policy drift between docs and runnable paths.
- Mitigation: update `TestingContext.md`, `Library/RestartContext.md`, and report artifacts per migration slice.

## 9. Completion Criteria
1. No operational dependency on Babashka for `Components/scripts/*` flows.
2. One primary Rust tool covers script responsibilities via subcommands.
3. Any standalone binaries are justified by independent utility and documented.
4. Validation and session protocol tooling run through Rust-native equivalents.
5. Nix/jail launcher paths reference Rust tools as first-class execution targets.

*The Great Work continues.*
