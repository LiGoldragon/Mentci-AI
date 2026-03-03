# Message Flow — JJ as Saṃskāra Requests

## Objective
Convert lane-mutating VCS operations into explicit protocol requests to enforce authority, traceability, and deterministic replay.

## Canonical Flow
1. `AgentWorkspaceInit` establishes role, lane scope, and writable surfaces.
2. Agent emits `JjCommandRequest`.
3. `lane-governor` evaluates request against `PolicyContext`.
4. If allowed, operation executes and emits `ExecutionResult`.
5. If blocked, flow enters `DeliberationRequest` cycle.
6. If refused, source receives `RefusalFeedbackMessage` and may submit `RevisionResponseMessage`.
7. Second unresolved cycle emits `HumanAuditorRequired`.
8. Every step appends to `AuditTrailBundle`.

## Guard Conditions
- No lane mutation without valid init envelope.
- No rewrite without explicit policy allowance.
- No refusal without structured feedback payload.
- No silent overrides.

## Minimal v1 Mutation Surface
- `bookmark.set`
- `bookmark.move`
- `git.push-bookmark`
- `rebase`
- `sync-lane`
