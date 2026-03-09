# Note: Web-Search Agent Follow-Up Context

## Session Outcomes
- Added a new project agent: `.pi/agents/web-search.md`
- Verified live probes for:
  - `web-search`
  - `browser`
  - `explore`
  - `reviewer`
- Added a transferable first-line `Status:` success rule to adjacent agents.
- Fixed agent tool-boundary mismatches by adding `lsp` to:
  - `.pi/agents/explore.md`
  - `.pi/agents/reviewer.md`
- Switched repo and active subagent defaults earlier in the session to:
  - `google/gemini-3.1-flash-lite`

## Current Architectural Conclusion
Do **not** merge `browser` and `web-search` right now.

### Why
`browser` is not just a weaker `web-search` agent. It is a specialized **single-URL render/extract** capability with a distinct execution path:
- `browser` uses `bash` + `omp render-web`
- it handles single-page rendering concerns like:
  - content negotiation
  - llms.txt/llms.md discovery
  - alternate feeds
  - HTML-to-text fallback
  - per-page rendering quality checks via Method/Notes

`web-search` is a distinct **Linkup-first multi-source research/synthesis** agent:
- `linkup_web_search`
- `linkup_web_answer`
- `linkup_web_fetch`
- it is intentionally scoped away from:
  - single-URL extraction
  - local repo search
  - mutation/execution work

## Recommended Direction
Keep both agents separate.

If consolidation is ever desired later, prefer a **hybrid orchestration/documentation merge** rather than a full agent merge.

### Meaning of hybrid
- keep `browser` as the specialized single-page extractor
- keep `web-search` as the external research/synthesis lane
- improve routing/documentation between them instead of collapsing the prompts and tool stacks into one agent

## Skill Update Made
Updated `.pi/skills/independent-developer/SKILL.md` so the main workflow routes external research through the `web-search` agent rather than direct main-session Linkup usage.

## Open Question: browser vs web-search convergence
Should `browser` and `web-search` eventually merge?

### Current answer
**Not yet.** Keep them separate for now.

### Why this remains open
The unresolved design question is not whether their current prompts overlap completely — they do not — but whether `web-search` should eventually gain a very narrow “single supporting URL fetch” fallback beyond its current scope, while `browser` remains the authority for standalone URL extraction.

If convergence is revisited later, prefer a **routing/orchestration merge** rather than collapsing both prompts and tool stacks into one agent.

## Future Work: Telemetry
Document only; do **not** implement yet.

Add adapter-level telemetry for each subagent dispatch so reliability work is evidence-driven rather than inferred from `(no output)`.

Suggested telemetry fields:
- agent name
- prompt contract id or task kind
- resolved model
- start timestamp / end timestamp
- child exit status
- whether `agent_end` was observed
- stdout present / stderr present
- final captured text length
- empty-output flag
- retry count
- fallback path taken

Also add a lightweight probe/KPI layer for known agents such as:
- `web-search`
- `browser`
- `explore`
- `reviewer`

This would let future sessions track empty-output frequency, interruption rate, and recovery rate explicitly.

## Future Work: Automatic Retry/Fallback
Document only; do **not** implement yet.

Prompt hardening is not enough on its own. The next reliability step should be adapter-level behavior:
1. On empty final capture, retry **once** with simplified scope and an explicit model pin.
2. If the child run still succeeds but captured text is empty, return a structured failure packet instead of bare `(no output)`.
3. If metadata proves the task ran, surface a minimal fallback summary from tool metadata so chained workflows can fail closed with context instead of silently losing state.
4. Keep the retry ladder bounded to one retry, then escalate as blocked/no-output rather than looping.

## Open Questions
- Should a reusable command/prompt template be added that explicitly dispatches `web-search` for external research tasks?
- Should `web-search` eventually gain a narrow fallback path for one-off URL fetches when they are part of multi-source synthesis, or should that remain strictly delegated to `browser`?
- If empty-output subagent failures recur, should the next step be adapter-level retry/fallback telemetry rather than more prompt-level hardening?
