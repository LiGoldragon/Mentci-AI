# Report: Dangling Visible-Head Guardrail and Primary `jj-agent` Interruption

## Prompt
The user asked for a new guardrail to stop agents from leaving dangling commits behind, to clean up any existing examples, and to persist useful insight before context compaction.

## Context
This followed the `jj-agent` migration session. A direct post-gate had already shown that the primary `jj-agent` lane could misreport finalize/push success, and a later bounded diagnosis task against the same lane hung entirely. The fallback `jj-expert` lane succeeded and surfaced one visible described empty head (`e9432ac5`) as likely cleanup debris.

## Summary
- Added an explicit **no dangling visible-commit exit** rule to the canonical JJ protocol and the primary JJ-facing skill/agent surfaces.
- Clarified that the only routine leftover at task end should be the single anonymous empty working copy (`@`) above the finalized target line.
- Reinforced that visible described empty commits and unclassified side histories invalidate completion claims unless they are intentionally preserved and documented.
- Captured a new operational lesson: if the primary `jj-agent` lane hangs or produces contradictory success prose, the correct recovery is bounded fallback to `jj-expert` plus direct post-gates.
- Current bounded JJ evidence shows one real cleanup candidate: `e9432ac5`, a visible described empty session head with no bookmark.

## Evidence
### Primary-lane interruption
The earlier bounded diagnosis task through `jj-agent` returned only `Interrupted`, so the lane cannot be treated as perfectly reliable yet for all JJ diagnosis work.

### Fallback diagnosis result
The bounded fallback `jj-expert` diagnosis reported:
- runtime bookmark = `dev`
- working copy `@ = 8c8897e1` is the expected anonymous empty working copy
- parent `@- = 223e75a2 Finalize jj-agent migration guidance`
- visible cleanup candidate = `potuqovr e9432ac5 (empty) session: Finalize retrospective guardrails and research artifact`

### Why this matters
An anonymous empty `@` is normal JJ handoff state. A separate visible described empty head is usually workflow debris unless there is an explicit reason to preserve it. Leaving such heads behind teaches the wrong completion pattern and pollutes visible history.

## Files Updated
- `Core/VersionControlProtocol.md`
- `.pi/skills/independent-developer/SKILL.md`
- `.pi/skills/verification-before-completion/SKILL.md`
- `.pi/skills/finishing-a-development-branch/SKILL.md`
- `.pi/agents/jj-agent.md`
- `.pi/agents/jj-expert.md`

## New Guardrail
### No dangling visible-commit exit
At completion time:
- the target bookmark must point to the intended finalized revision,
- push must be verified,
- exactly one anonymous empty `@` may remain as next-session preparation,
- any other visible head, described empty commit, or side history must be either:
  - classified as active,
  - classified as integrated,
  - classified as intentionally preserved,
  - or cleaned as a cleanup candidate.

If it is mere noise, completion is not valid until it is removed.

## Durable Insight
### The primary JJ lane is still operationally imperfect
The repo should continue treating `jj-agent` as the primary lane, but not as infallible. Contradiction handling and bounded fallback to `jj-expert` remain necessary in practice.

### Empty-working-copy is not the same as dangling session debris
The correct handoff is one anonymous empty `@`. The anti-pattern is a separate visible described empty head that remains after finalization.

### Compaction-safe lesson
Before context compaction, the repo should remember two things:
1. **Never trust JJ subagent success prose without bounded post-gates.**
2. **Never leave unclassified visible empty heads behind and call the session complete.**
