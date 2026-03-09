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
Updated `.pi/skills/independent-developer/SKILL.md` in `### 5. Implementation Flow` so the high-level workflow now explicitly starts with web discovery:
- `Use linkup_web_search for broad discovery, then use Linkup to validate assumptions.`

## Open Questions
- Should a reusable command/prompt template be added that explicitly dispatches `web-search` for external research tasks?
- Should `web-search` eventually gain a narrow fallback path for one-off URL fetches when they are part of multi-source synthesis, or should that remain strictly delegated to `browser`?
- If empty-output subagent failures recur, should the next step be adapter-level retry/fallback telemetry rather than more prompt-level hardening?
