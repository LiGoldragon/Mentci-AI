# Strategy: Session Aggregation Guard

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Process Hardening)

## 1. Problem Statement
Prompt-aggregating session commits were missed even though atomic intent commits were created correctly.

## 2. Root-Cause Analysis
1. **Partial Protocol Loading**
- `core/VERSION_CONTROL.md` was loaded and followed for atomic commits.
- The stronger end-of-session synthesis mechanics in `core/CONTEXTUAL_SESSION_PROTOCOL.md` were not operationalized as a hard completion gate.

2. **No Completion Gate**
- There is no automated check that blocks prompt completion unless a `session:` aggregating commit exists for the current prompt range.

3. **Execution Bias Toward Atomic Loop**
- The workflow strongly reinforces immediate `intent:` commits.
- Without an explicit finalization checklist, session synthesis is easy to omit during rapid iteration.

## 3. Strategy Objective
Make session-aggregating commit creation mandatory, visible, and mechanically verifiable before prompt completion.

## 4. Implementation Plan
1. **Protocol Clarification**
- Update `core/AGENTS.md` to explicitly include `core/CONTEXTUAL_SESSION_PROTOCOL.md` in mandatory loaded context.
- Add a one-line rule: prompt completion is invalid without session synthesis.

2. **Operational Checklist**
- Add end-of-prompt checklist to agent workflow docs:
- verify atomic commits complete
- run session metadata summary
- create final `session: <summary>` aggregating commit
- verify aggregate commit is parent-visible in `jj log`

3. **Automation Guard**
- Add a script (for example `scripts/session_guard/main.clj`) that checks:
- at least one `intent:` commit exists for prompt range
- a trailing `session:` commit exists after those intents
- script exits non-zero when session synthesis is missing

4. **Validation Integration**
- Hook session guard into the standard validation pass and/or end-of-flow command path.
- Keep non-interactive failure output with exact remediation command suggestions.

5. **Retroactive Hygiene**
- For prompts already closed without aggregation, create a follow-up `session:` recovery commit referencing covered `intent:` commits and prompt summary.

## 5. Acceptance Criteria
- Every completed prompt has both atomic `intent:` commits and one final `session:` aggregating commit.
- Missing session synthesis fails local validation.
- `jj log` clearly shows prompt narrative at session granularity without losing atomic history.

*The Great Work continues.*
