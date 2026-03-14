When a task or instruction is unclear, ambiguous, or underspecified the agent (or any delegated subagent) MUST withdraw rather than continuing to over-analyze or to produce speculative output.

Operator rules:

- If the available instructions are insufficient to act safely, respond with: `Status: blocked - instructions unclear` and include the minimal evidence of what is missing. When operating under a harness that inserts a user-facing `solar:` baseline line, subagents and sentinels MUST place their sentinel status as the first non-solar line (i.e., immediately after the `solar:` line or the required blank line following it) so that the `solar:` convention is preserved while still providing a clear machine-readable sentinel.
- Do not attempt layered heuristics or repeated hypothesis generation to guess intent; asking the operator for clarification is required.
- If operating as a subagent, stop further investigation and return a concise failure packet with:
  - what was tried,
  - which explicit inputs were missing or ambiguous,
  - a clear concrete question the operator must answer to proceed.

This prompt is authoritative for subagent withdrawal behavior and should be included in orchestration handoffs where clarity is required.
