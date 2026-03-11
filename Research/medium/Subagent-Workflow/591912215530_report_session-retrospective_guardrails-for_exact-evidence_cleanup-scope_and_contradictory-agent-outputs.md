# Report: Session Retrospective — Guardrails for Exact Evidence, Cleanup Scope, and Contradictory Agent Outputs

## Prompt
The user asked to look at the problems we had in this session, add guardrails and guidance to avoid them in the future, and save any valuable insight and data into the filesystem before context compaction.

## Context
This session involved JJ history cleanup, repeated duplicate change-ID analysis, packaging `agentic-jujutsu`, creating an `agentic-jj-expert` lane, and renaming the Rust `LargeAI` symbol. The conversation exposed several recurring failure modes: incomplete cleanup scope, contradictory subagent summaries, overconfident preservation claims, exact-token search mismatches, and protocol-guard failures that were reported inconsistently.

## Summary
### Key failure modes observed
1. **Cleanup scope was too narrow.**
   A request to clean a time-bounded window of history was initially treated as “clean the active target lineage,” which missed detached rewrite debris and duplicate-change remnants like the `xynn` / `wpyp` cluster.

2. **Contradictory subagent summaries were too easy to trust.**
   At least one `jj-expert` report returned a mixed state (reporting a blocker while simultaneously claiming success/push completion). The correct answer only became clear after direct post-gate checks.

3. **Preservation claims were made from history, not always current surviving file contents.**
   Saying a change was “preserved” is only correct if the current visible code still contains it. Historical presence in older commits is not enough.

4. **Exact-token evidence from the user was underweighted.**
   The user’s exact search (`rg LargeAI`) was correct. The problem was not that the user was wrong; it was that the code used `LargeAi` and serialized `largeAI`. Literal-token absence and semantic absence are different claims and must be handled explicitly.

5. **Guard failures need durable recording.**
   A `root-guard` failure caused by a missing sidecar/config prerequisite should be treated as a real blocker and written down durably, not buried inside a mixed subagent summary.

### Guardrails added in response
- Exact-evidence challenge rule: when the user provides an exact command result, reproduce the exact check first.
- Contradictory-report rule: mixed success/blocked summaries trigger direct bounded post-gates.
- Preservation rule: verify current surviving file contents, not only historical commits.
- Time-window cleanup rule: inspect detached visible heads and rewrite debris inside the requested window, not just the active target lineage.
- Guard-failure recording rule: when `session-guard` or `root-guard` fails because of missing prerequisites, record the blocker in Research before treating the session as complete.

## Files Updated
- `Core/VersionControlProtocol.md`
- `.pi/skills/independent-developer/SKILL.md`
- `.pi/skills/systematic-debugging/SKILL.md`
- `.pi/skills/verification-before-completion/SKILL.md`
- `.pi/agents/jj-expert.md`
- `.pi/agents/agentic-jj-expert.md`

## Why these patches matter
- They convert this session’s failures into reusable doctrine.
- They force stronger weight on literal user evidence.
- They reduce false confidence from optimistic subagent prose.
- They broaden JJ cleanup requests to the actual visible history clutter the user sees.
- They make protocol blockers durable so future sessions start with the right context.

## Durable Insights
### Exact evidence is a first-class authority signal
If a user cites exact output, the assistant must reproduce that exact check before broadening or translating the claim. This avoids talking past the user and prevents semantic overreach.

### History cleanup requests are about user-visible clutter, not just target-line hygiene
When the user says “clean the last day” or “clean the last 3 days,” they care about detached visible heads, duplicate clusters, and rewrite debris that still pollute `jj log` and `visible_heads()`, not merely whether `dev` itself is tidy.

### Mixed status summaries must be treated as unreliable until post-gated
Subagent summaries are useful, but when they mix success and blockage, the assistant must immediately run direct bounded post-gates and defer to current raw state.

### Preservation means present surviving content
A preserved change must still exist in the current visible code path the user cares about. Historical reachability is weaker than present content and should be described as such.

## Remaining Known Blocker
- `execute root-guard` previously failed because `Components/mentci-aid/src/actors/root_guard.edn` was missing in the relevant environment. That blocker should be treated as a real protocol-health item until investigated and resolved.

programming: 889d0s1m
