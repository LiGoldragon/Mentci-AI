# Strategy: Improving Strategy-Development

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Process Hardening)

## 1. Goal
Automate strategy execution support while preserving durable, restartable context across sessions.

## 2. Methodology
- **Context Persistence First:** Persist high-value session state into:
  - `Library/RESTART_CONTEXT.md`
  - `Strategies/<priority>/<Subject>/...`
  - `Reports/<priority>/<Subject>/...`
- **Code-First Utilities:** Use Babashka scripts for repeatable workflows (discovery, validation, guardrails) instead of ad hoc prompt instructions.
- **Deterministic Sweeps:** Use standard guard/test sweeps before and after strategy implementation batches.
- **Subject Pairing Discipline:** Ensure every strategy delta has a report counterpart and vice-versa.

## 3. Active Program
1. Tool discovery support:
- Maintain `Components/scripts/tool_discoverer/main.clj` for package/tool lookup.
2. Session context capture:
- Treat `Library/RESTART_CONTEXT.md` as the primary session-resume snapshot and update it after major structural changes.
3. Sweep protocol:
- `bb Components/scripts/validate_scripts/main.clj`
- `bb Components/scripts/session_guard/main.clj`
- `bb Components/scripts/root_guard/main.clj`
4. Backfill protocol:
- If an exchange lacks a visible commit/report artifact, backfill immediately via `Reports/<priority>/<Subject>/` and corresponding strategy update.

## 4. Current Focus
1. Keep strategy/report indexes current (`README.md` entries).
2. Reduce stale references during `Sources/` -> `Sources/` migration window.
3. Maintain release/session protocol reliability with `session_finalize` and `session_guard`.

*The Great Work continues.*
