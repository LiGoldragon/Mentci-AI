# Answer Artifact

- Subject: `Scripts-Rust-Migration`
- Kind: `answer`
- Prompt: `strategy to rewrite the clj in rust, keeping the clj version until the rust version passes all the same tests`

## Summary
Strengthened the migration strategy to enforce a strict authority gate:
- Clojure remains the default implementation until Rust passes the same shared test corpus for each script.

## Updated Strategy Artifacts
1. `Strategies/Scripts-Rust-Migration/STRATEGY.md`
2. `Strategies/Scripts-Rust-Migration/ROADMAP.md`
3. `Strategies/Scripts-Rust-Migration/REPORT.md`

## Core Policy
1. Keep one shared parity test corpus per script.
2. Execute identical tests against `--impl clj` and `--impl rust`.
3. Compare observable behavior (stdout/stderr/exit + required side effects).
4. Block Rust default cutover until parity passes.
5. Retain CLJ rollback path for one stable release window after Rust cutover.
