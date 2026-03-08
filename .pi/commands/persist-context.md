---
description: Persist session context to Research/medium/Session-Context-Persistence with indexed handover metadata
---

Use the subagent tool with the chain parameter to execute this workflow:

1. First, use the "task" agent to summarize current session state (decisions, commits, unresolved follow-ups) into a structured draft.
2. Then, use the "reviewer" agent to check that the summary is accurate, non-redundant, and aligned with current repo facts.
3. Finally, use the "task" agent to write/update:
   - `Research/medium/Session-Context-Persistence/<solar>_report_*.md`
   - `Research/medium/Session-Context-Persistence/index.edn`

Execute this as a chain, passing output between steps via {previous}.
