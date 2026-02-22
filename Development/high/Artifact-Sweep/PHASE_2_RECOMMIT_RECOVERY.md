# Artifact-Sweep Phase 2: Re-Commit Recovery

**Queue Position:** Next actionable strategy (`Artifact-Sweep`, Rank 2)
**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Audit Integrity + Token/History Purity)

## 1. Context
After the Sources history purge, repository object history is clean, but commit-graph hygiene remains degraded by rewrite-import conflict branches and state drift.

## 2. Objective
Complete Artifact-Sweep hardening by removing recommit-level artifacts from the active workflow:
1. conflicted non-trunk heads
2. stale recommit branches that can re-pollute trunk
3. protocol/documentation drift that causes recommit inconsistency

## 3. Sources (Current State)
- `Logs/RECOMMIT_BREAK_AUDIT.md`
- `jj log -r 'conflicts()'`
- `Library/STRATEGY_QUEUE.md`
- `Core/VERSION_CONTROL.md`

## 4. Execution Plan
### A. Publish Baseline
1. Fetch remote and inspect divergence.
2. Move `dev` bookmark explicitly to approved rewrite tip.
3. Publish with explicit non-fast-forward policy (force only after final confirmation).

### B. Conflict-Head Containment
1. Enumerate all heads in `conflicts()` not on `::dev`.
2. Classify each as:
- recover to trunk
- archive-only
- abandon
3. Generate allowlist file in `Logs/` before any destructive DAG operations.

### C. Trunk Reconstruction Sweep
1. Re-apply missing intentional artifacts from conflict lineage that are still desired:
- strategy entries
- process docs
- tooling scripts
2. Commit each recovered intent atomically.

### D. Guardrails Against Re-break
1. Add DAG health check script (proposed: `scripts/recommit_guard/main.clj`) that fails when:
- unresolved `conflicts()` heads exist outside approved exceptions
- session work ends without clean trunk synthesis
2. Add queue drift check (strategy registry vs filesystem strategy dirs).

## 5. Deliverables
- `Logs/RECOMMIT_HEAD_ALLOWLIST.edn`
- `Logs/RECOMMIT_RECOVERY_EXECUTION.md`
- Updated `Library/STRATEGY_QUEUE.md` reflecting reconciled strategy set
- Optional guard script: `scripts/recommit_guard/main.clj`

## 6. Acceptance Criteria
1. `jj log -r 'conflicts() & ::dev'` is empty except explicitly allowlisted archival nodes.
2. `dev` is published and remote-aligned.
3. Strategy queue reflects intended active strategy set.
4. Recommit guardrails exist and are runnable.

## 7. Risks
1. Accidentally abandoning desired intent commits.
2. Force-push without synchronized team migration.
3. Partial queue restoration causing planning incoherence.

Mitigation: enforce pre-action allowlist and require per-batch verification logs.

*The Great Work continues.*
