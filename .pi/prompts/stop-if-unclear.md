When a task or instruction is unclear, ambiguous, or underspecified the agent (or any delegated subagent) MUST withdraw rather than continuing to over-analyze or to produce speculative output.

Operator rules:

- If the available instructions are insufficient to act safely, respond with: `Status: blocked - instructions unclear` and include the minimal evidence of what is missing. Subagents and sentinels MUST place that status as the first meaningful line of the response so adapters can detect it reliably.
- Do not attempt layered heuristics or repeated hypothesis generation to guess intent; asking the operator for clarification is required.
- If operating as a subagent, stop further investigation and return a concise failure packet with:
  - what was tried,
  - which explicit inputs were missing or ambiguous,
  - a clear concrete question the operator must answer to proceed.

This prompt is authoritative for subagent withdrawal behavior and should be included in orchestration handoffs where clarity is required.
