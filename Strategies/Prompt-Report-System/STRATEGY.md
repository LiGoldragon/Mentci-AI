# Strategy: Prompt Report System

## Objective
Extract user intent from interrupted prompt sequences and convert unfinished work into
explicit, finishable units without losing auditability.

## Inputs
- Chat transcript and queued/interrupted prompt sequence.
- Current VCS state (`jj log`, `jj status`, latest `session:` commits).
- Existing reports/strategies under matching subject.

## Output
- A prioritized recovery queue of unfinished jobs, each with:
  - canonical subject
  - recovered intent statement
  - concrete acceptance criteria
  - execution status (`queued`, `in_progress`, `done`, `blocked`)

## Intent Extraction Model
1. Detect interruption boundaries:
   - a new directive appears before the previous directive receives a completed response.
2. Collapse duplicate/superseded directives:
   - merge by canonical subject and goal identity.
3. Normalize each recovered job:
   - `who/what/where/constraint/definition-of-done`.
4. Assign execution class:
   - `direct-implementation`, `strategy-only`, `report-only`, `requires-confirmation`.

## Recovery Execution Loop
1. Build `Interrupted Job Queue` artifact in the matching `Reports/<Subject>/` topic.
2. Pick top job by severity and dependency order.
3. Execute one logical change at a time.
4. Finalize with required session synthesis and report emission.
5. Mark job state and continue until queue is empty or blocked.

## Priority Rules
1. Protocol integrity fixes first:
   - missing session synthesis, missing report counterpart, push invariants.
2. Data loss / history integrity second:
   - purge/recovery, rewrite correctness, stale references.
3. Requested feature/strategy work third.

## Guardrails
- Never infer completion from partial edits; require explicit acceptance checks.
- If two interrupted prompts conflict, latest explicit user directive wins.
- Preserve traceability: every recovered job must reference source prompt text.

## Validation
1. No trailing unfinished recovered jobs without status.
2. `Reports/<Subject>/README.md` indexes the new recovery artifacts.
3. Latest head is a `session:` commit and pushed to `dev`.

## Implementation Status
- Implemented extractor: `Components/scripts/interrupted_job_queue/main.clj`
- Generated queue artifact: `Reports/Prompt-Report-System/INTERRUPTED_JOB_QUEUE.md`
- Script test context: `Components/scripts/interrupted_job_queue/TESTING_CONTEXT.md`

## Current Review Result
The prior strategy file was a placeholder and did not provide a usable interrupted-job
recovery flow. This revision makes the strategy executable and testable.

## Chronos Invocation Fix Strategy (Answer Report)

### Problem
`Components/scripts/answer_report/main.clj` invokes Chronos with:
- `cargo run --quiet --bin chronos -- ...`

The repo root no longer contains `Cargo.toml`; the workspace manifest is at
`Components/Cargo.toml`. Running from repo root causes Cargo manifest lookup failure.

### Goal
Make `answer_report` Chronos resolution robust across repo layout changes and shell contexts.

### Plan
1. Replace single hardcoded Chronos call with a resolver function in
   `Components/scripts/answer_report/main.clj`.
2. Resolution order:
   - Try direct binary: `chronos --format numeric --precision second`.
   - Try cargo with explicit manifest path:
     `cargo run --quiet --manifest-path Components/Cargo.toml --bin chronos -- --format numeric --precision second`.
   - Optional fallback if needed:
     `cargo run --quiet --manifest-path Library/chronos/Cargo.toml ...` (only if such path exists in future layout).
3. Keep output contract unchanged:
   - must still parse `sign.degree.minute.second | year AM`.
4. Improve failure diagnostics:
   - include attempted commands and stderr summary.
5. Add non-destructive test path:
   - `--chronos-raw` optional override for tests to bypass runtime Chronos execution.

### Acceptance Criteria
1. `bb Components/scripts/answer_report/main.clj --prompt x --answer y --subject Prompt-Report-System --title smoke`
   succeeds in default dev shell when either `chronos` binary or `Components/Cargo.toml` path is available.
2. No behavior regression in filename/date extraction.
3. Existing report format remains unchanged except improved error messaging.
4. Add/update `TESTING_CONTEXT.md` with both binary-present and cargo-manifest fallback scenarios.
