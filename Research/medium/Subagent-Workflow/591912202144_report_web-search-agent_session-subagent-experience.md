# Report: Web-Search Agent Session Subagent Experience

## Summary

Using subagents during the work to add a new web-search agent was net positive once the session stabilized. The strongest pattern was splitting discovery, planning, and implementation into distinct handoffs: parallel research surfaced current agent-template patterns and "superpowers" ideas quickly, then planner and implementation handoffs converted that research into actionable work. Earlier in the broader session, however, intermittent empty-output and interruption behavior made subagent results feel unreliable. After recent fixes/reloads, live probes for `web-search`, `browser`, `explore`, and `reviewer` all worked, which materially improved confidence in the subagent workflow.

## What Worked Well

- **Parallel research paid off**
  - Running independent research tracks in parallel on current agent templates and superpowers ideas was efficient and produced useful comparative context.
  - The outputs were complementary rather than duplicative, which made synthesis easier.

- **Planner handoff worked**
  - A dedicated planning step created a cleaner separation between exploration and execution.
  - This reduced ambiguity for downstream implementation work.

- **Implementation handoff worked**
  - Passing a narrowed, concrete task to an implementation-oriented subagent was effective.
  - The handoff model helped keep the main thread focused on orchestration instead of low-level execution.

- **Recent reload/fix cycle improved reliability**
  - After the latest fixes/reloads, live probes against `web-search`, `browser`, `explore`, and `reviewer` succeeded.
  - That suggests the subagent surface is viable when the runtime is healthy.

## What Failed or Was Fragile

- **Earlier intermittent empty outputs**
  - In prior parts of the session, some subagent invocations returned empty or effectively unusable output.
  - This is especially damaging because it is ambiguous whether the agent failed, was interrupted, or completed with no findings.

- **Interruptions reduced trust**
  - Session interruptions made it harder to distinguish platform instability from task-specific failure.
  - That increases operator overhead because every result needs extra validation.

- **Main-thread shell overuse was harmful**
  - Broad direct shell use in the main context appears to have contributed to transcript poisoning.
  - That in turn motivated later skill hardening, which seems like the right response but also indicates the workflow was too fragile under noisy main-thread activity.

- **Recovery depended on manual resets/reloads**
  - The fact that things worked after fixes/reloads is encouraging, but also highlights that recovery was operational rather than fully self-healing.

## Suggestions

- **Prefer subagents over broad shell-heavy main-thread work**
  - Keep the main context focused on orchestration, synthesis, and narrow verification.
  - Push exploratory or high-volume work into subagents to reduce transcript contamination.

- **Treat empty output as a first-class failure mode**
  - Detect and label empty-output returns explicitly rather than letting them appear as silent success.
  - Require a minimal structured payload from subagents, even on failure.

- **Preserve the planner → implementer pipeline**
  - The handoff chain worked well and should be the default for non-trivial agent additions.
  - Parallel research remains valuable before planning when there are multiple plausible designs.

- **Add lightweight live-probe checks after reloads**
  - A small probe suite for known agents like `web-search`, `browser`, `explore`, and `reviewer` is a good confidence gate before resuming deeper work.

- **Continue skill hardening around shell discipline**
  - The experience supports stricter guidance limiting broad shell use in the primary transcript.

## Open Questions

- What was the actual root cause of the earlier empty-output/interruption behavior: transport, orchestration, buffering, or agent runtime instability?
- Can empty-output failures be surfaced automatically with retry or fallback behavior?
- Should successful live probes be required before dispatching complex multi-agent work?
- How much of the improvement came from fixes to subagent plumbing versus simply reducing transcript pollution?
- Is there a durable policy boundary for when to use direct shell commands in the main context versus delegating to subagents?
