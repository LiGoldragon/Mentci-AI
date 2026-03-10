---
description: Delegate JJ/version-control work to the jj-expert subagent
---

Use the subagent tool with the `jj-expert` agent for this request:

$@

Requirements:
- Start with bounded JJ preflight: `jj status`, resolve `MENTCI_TARGET_BOOKMARK`, run a bounded `jj log`, and include `jj diff --summary` when relevant.
- Never hardcode `dev` when the runtime target bookmark should be used.
- Stay strictly within JJ/version-control scope.
- If a request says to keep a specific change, capture and re-verify that content footprint before and after any history edit.
- Unless the caller explicitly authorizes discarding a change, default to preserving it.
- If rewrite damage occurs, fail closed until the content is restored and re-verified in the surviving lineage.
- Be brief if the preflight shows no problem.
