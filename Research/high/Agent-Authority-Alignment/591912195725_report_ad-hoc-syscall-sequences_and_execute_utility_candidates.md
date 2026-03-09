# Ad-hoc Syscall Sequences → Utility Consolidation Candidates

## Context
User request: identify common multi-syscall ad-hoc sequences and consolidate low-hanging fruit into utilities (prefer `execute`) to reduce prompt burden and improve operational reliability.

## Implemented (Low-Hanging Fruit)
1. **Runtime target bookmark resolver in `execute`**
   - Command: `execute target-bookmark`
   - Resolution order:
     1) `MENTCI_TARGET_BOOKMARK` env var
     2) non-VC runtime JSON (`MENTCI_RUNTIME_CONTEXT_FILE` or `$PI_CODING_AGENT_DIR/runtime-context.json`, key `targetBookmark`)
     3) cwd-name fallback (`mentci-ai--dev -> dev`, `mentci-ai -> main`, etc.)
     4) safe default `dev`

2. **Session metadata utility in `execute`**
   - Command: `execute session-meta`
   - Outputs one structured JSON payload combining:
     - `solarTime`
     - `coreVersion`
     - `targetBookmark`
     - `bookmarkSource`
     - `cwd`
   - This collapses repeated ad-hoc command chains (`chronos`, version, branch inference) into one syscall.

3. **Devshell short-term branch contract**
   - `Components/nix/dev_shell.nix` now auto-sets `MENTCI_TARGET_BOOKMARK` if unset, using cwd dirname fallback.

## Common Ad-hoc Sequences Worth Utility Consolidation

### A) JJ preflight safety packet (very high frequency)
Current ad-hoc pattern:
- `jj status`
- `jj bookmark list`
- bounded `jj log` around `@/@-/<target>`
- optional emptiness check before bookmark move

Suggested utility:
- `execute jj-preflight --bookmark <target> --json`
- emits structured packet with risk flags (empty commit, bookmark mismatch, unpushed ahead count).

### B) Finalization packet (already partial in `execute finalize`)
Current ad-hoc pattern:
- gather prompt/context/changes/model
- compute solar time
- describe commit
- move bookmark
- push
- verify remote alignment

Suggested increment:
- `execute finalize` should consume `execute target-bookmark` by default unless explicit `--bookmark` override.
- add post-push explicit verification summary in JSON mode.

### C) Verification packet (build/test/evidence synthesis)
Current ad-hoc pattern:
- run targeted tests/checks
- parse output manually
- include snippets in final response

Suggested utility:
- `execute verify --cmd <...> --expect <regex> --json`
- standardized evidence record for completion claims.

### D) Branch ownership guard for agents
Current ad-hoc pattern:
- infer/remember intended branch in prompt text

Suggested utility:
- `execute branch-guard --require-target`
- fails if runtime target bookmark unresolved or if current state disagrees with target policy.

## Why this improves intent reproduction
- Removes implicit operator memory from critical workflows.
- Converts branch targeting from prose to deterministic resolution.
- Reduces per-agent prompt complexity and branch-routing mistakes.
- Creates structured outputs that can be audited and consumed by subagents.

## Follow-up (next low-risk increments)
1. Add `execute jj-preflight` (JSON + human mode).
2. Wire `execute finalize` to runtime bookmark resolver by default.
3. Add `execute branch-guard` for fail-closed push safety.
4. Introduce runtime context file producer in `mentci-user` (non-VC state lane).

---
Solar Time: `591912195725`
