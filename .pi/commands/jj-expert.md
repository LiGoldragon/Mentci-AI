---
description: Delegate JJ/version-control work to the jj-expert subagent
---

Use the subagent tool with the `jj-expert` agent for this request:

$@

Requirements:
- Start with a bounded JJ preflight that uses the runtime bookmark:
  1. `jj status`
  2. resolve `MENTCI_TARGET_BOOKMARK` and log `jj log -r "$MENTCI_TARGET_BOOKMARK|$MENTCI_TARGET_BOOKMARK@origin|@|@-" --no-graph -n 20` (or `jj log -r '@|@-' --no-graph -n 10` if the runtime bookmark is unresolved). Report the resolved target before analysis or mutation.
  3. Run `jj diff --summary` before judging the working copy final, especially when a commit or bookmark move is under consideration.
- Never hardcode `dev` when the runtime target bookmark should be used. Always mention `$MENTCI_TARGET_BOOKMARK` when describing targets or logs.
- Stay strictly within JJ/version-control scope.
- Explain the difference between change IDs and commit IDs when diagnosing history; duplicated visible change IDs usually mean divergence/history exposure, not corruption. Document duplicate signatures, their bookmarks, and whether they belong to the target or side histories.
- Classify non-target bookmarks (drafts, experiments, backups) before you touch them. Flag which ones are side histories requiring cleanup or reconciliation.
- Do not finalize a clean tree unless you are explicitly repairing history or documenting a no-op. Capture the intent before touching bookmarks.
- Do not move the runtime bookmark to an empty commit or literal `@`. Finalize work into a described commit first; only then move `$MENTCI_TARGET_BOOKMARK` to that revision.
- If you are asked to keep a change, treat that as a hard requirement. Record the exact footprint, preserve it through any rewrite, and re-verify it afterward. Unless explicitly allowed, do not discard user-requested content.
- If rewrite damage occurs, fail closed until the missing content is restored and verified across the affected lineage.
- Be brief when the preflight shows no problem. Include raw snippets for any preflight evidence you cite.
