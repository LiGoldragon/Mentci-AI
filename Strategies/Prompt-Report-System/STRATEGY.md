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

## Current Review Result
The prior strategy file was a placeholder and did not provide a usable interrupted-job
recovery flow. This revision makes the strategy executable and testable.
