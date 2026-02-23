# Strategy: Improving Strategy-Development

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Process Hardening)

## 1. Goal
Automate strategy execution support while preserving durable, restartable context across sessions.

## 2. Methodology
- **Context Persistence First:** Persist high-value session state into:
  - `Library/RestartContext.md`
  - `Development/<priority>/<Subject>/...`
  - `Research/<priority>/<Subject>/...`
- **Code-First Utilities:** Use Babashka scripts for repeatable workflows (discovery, validation, guardrails) instead of ad hoc prompt instructions.
- **Deterministic Sweeps:** Use standard guard/test sweeps before and after strategy implementation batches.
- **Subject Pairing Discipline:** Ensure every strategy delta has a report counterpart and vice-versa.

## 3. Active Program
1. Tool discovery support:
- Maintain `Components/scripts/tool_discoverer/main.clj` for package/tool lookup.
2. Session context capture:
- Treat `Library/RestartContext.md` as the primary session-resume snapshot and update it after major structural changes.
3. Sweep protocol:
- `execute root-guard`
- `execute session-guard`
- `execute root-guard`
4. Backfill protocol:
- If an exchange lacks a visible commit/report artifact, backfill immediately via `Research/<priority>/<Subject>/` and corresponding strategy update.

## 4. Current Focus
1. Keep Development/Research indexes current (`index.edn` entries).
2. Reduce stale references during `Inputs/` -> `Sources/` migration window.
3. Maintain release/session protocol reliability with `session_finalize` and `session_guard`.

*The Great Work continues.*
