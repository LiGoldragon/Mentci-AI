---
description: Delegate JJ/version-control work to the jj-agent subagent first, using jj-expert only as fallback/rescue
---

Before calling this command, gather the JJ context: identify the question you expect the expert to answer, note visible symptoms, and summarize your reasoning. Do not treat this command as a blind relay—explicitly explain why you need JJ judgment and which bookmarks or revisions you intend to inspect or rewrite.

Use the subagent tool with the `jj-agent` agent for this request. If that lane is unavailable or misbehaving, retry with `jj-expert` as explicit fallback:

$@

Requirements:
- Start with a bounded JJ preflight that uses the runtime bookmark:
  1. `jj status`
  2. resolve `MENTCI_TARGET_BOOKMARK` and log `jj log -r "$MENTCI_TARGET_BOOKMARK|$MENTCI_TARGET_BOOKMARK@origin|@|@-" --no-graph -n 20` (or `jj log -r '@|@-' --no-graph -n 10` if the runtime bookmark is unresolved). Report the resolved target before analysis or mutation.
  3. Run `jj diff --summary` before judging the working copy final, especially when a commit or bookmark move is under consideration.
- Never hardcode `dev` when the runtime target bookmark should be used. Always mention `$MENTCI_TARGET_BOOKMARK` when describing targets or logs.
- If working inside a nested JJ repo (for example `Components/CriomOS`), resolve the bookmark in that repo and do not fall back to Git branch/commit workflows.
- Stay strictly within JJ/version-control scope.
- Explain the difference between change IDs and commit IDs when diagnosing history; duplicated visible change IDs usually mean divergence/history exposure, not corruption. Document duplicate signatures, their bookmarks, and whether they belong to the target or side histories.
- Classify non-target bookmarks (drafts, experiments, backups) before you touch them. Flag which ones are side histories requiring cleanup or reconciliation.
- Do not finalize a clean tree unless you are explicitly repairing history or documenting a no-op. Capture the intent before touching bookmarks.
- Do not move the runtime bookmark to an empty commit or literal `@`. Finalize work onto the intended non-empty revision, capture it with an explicit commit message rather than pre-emptively describing the working-copy node, and only then move `$MENTCI_TARGET_BOOKMARK` to that revision.
- If you are asked to keep a change, treat that as a hard requirement. Record the exact footprint, preserve it through any rewrite, and re-verify it afterward. Unless explicitly allowed, do not discard user-requested content.
- If rewrite damage occurs, fail closed until the missing content is restored and verified across the affected lineage.
- Be brief when the preflight shows no problem. Include raw snippets for any preflight evidence you cite.
