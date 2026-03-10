# Report: Duplicate `wpuw` Change-ID Root Cause, Non-Loss Repair, and `dev` Rebase Outcome

## Prompt
Use @.pi/skills/subagent-driven-development/SKILL.md To to figure out why there are two commits with the same jj change id (wpuw), write a report, fix it, then rebase dev on latest main.

## Summary
- Investigated the duplicate `wpuw` JJ change-id using bounded `jj-expert` inspection.
- Determined this is not repository corruption.
- Determined the visible duplication comes from an older visible revision of the same logical JJ change remaining in history.
- Verified that the newer visible revision already preserves the actual content introduced by `66fb0976`.
- Established the non-loss repair path: target the old revision by commit ID (`66fb0976`), not by the ambiguous change-id prefix `wpuw`.
- Separated the duplicate-change repair from the later `dev`→`main` rebase as two distinct JJ operations.

## Current Bounded State
- Runtime bookmark: `dev`
- `main` is `4ecb02f7`
- `dev` is `4f3fe3bb`
- current working copy `@` is `3c11dec5` and contains only this added report file
- only one visible `wpuw` revision remains: `c6af423c`

## Pre-Repair State
- Before repair, `dev` sat above `7172c369`
- Nearby lineage before repair:
  - `7172c369` — `intent: route skill-level vcs handling through jj-expert`
  - `589f5e33` — `intent: tighten jj expert bookmark safety wording`
  - `66fb0976` — `intent: add a jj expert subagent`
- `main` was separate in the bounded view and was not the source of the duplicate change-id concern.

## Root Cause
This is normal JJ change evolution represented in an undesirable visible shape:
- `66fb0976` and `589f5e33` are two visible revisions with the same JJ change id (`wpuw...`).
- They are not sibling heads; they are in an ancestor/descendant chain.
- The most likely cause is incomplete history cleanup: an older visible revision of the same logical change was left in history after a later rewritten/evolved revision remained visible.

So the problem is:
- **not corruption**
- **not random duplication**
- **yes, a history-shaping cleanup issue**

## Non-Loss Verification
A stricter bounded comparison was run after the concern that `66fb0976` must not lose its actual changes.

### What `66fb0976` introduced
It added:
- `.pi/agents/jj-expert.md`
- `.pi/commands/jj-expert.md`
- `.pi/prompts/jj-expert.md`
- `docs/plans/2026-03-09-jj-expert-agent-implementation-plan.md`

### What `589f5e33` added on top
It then:
- modified `.pi/agents/jj-expert.md`
- added `Research/medium/Subagent-Workflow/59191220247_report_jj-expert-agent_spec_research_and_validation.md`

### Preservation conclusion
The cumulative diff from the parent of `66fb0976` to `589f5e33` still contains all four original additions from `66fb0976`, plus the later follow-up changes.

That means the content from `66fb0976` is preserved in the newer visible lineage. Removing the older visible revision is therefore content-safe **when targeted by commit ID**.

## Repair Execution and Outcome
The actual repair was slightly more involved than the initial plan.

1. Captured bounded pre-op state and verified again that the cumulative content from `66fb0976` was already preserved in the newer lineage.
2. Repaired the duplicate visible older revision by targeting **commit ID** `66fb0976` with `jj abandon 66fb0976`.
3. That abandon caused descendant rewrite/conflict effects, so bounded `jj restore` operations were applied to restore the preserved `jj-expert` files and plan through the rewritten descendants.
4. After the repair, the old visible revisions became hidden and a single visible surviving `wpuw` revision remained at `c6af423c`.
5. The preserved cumulative content from `66fb0976` remained present in the surviving `wpuw` lineage.
6. `dev` was then rebased onto latest `main`, resulting in `dev = 4f3fe3bb` on top of `main = 4ecb02f7`.

## Critical Safety Note
Do **not** target `wpuw` directly for mutation while it is ambiguous. The repair must target the concrete commit ID `66fb0976`, not the duplicated change-id prefix.

## Evidence Sources
This report is based on bounded evidence gathered through `jj-expert`, including:
- `jj status`
- bounded `jj log`
- `jj bookmark list dev main`
- `jj log -r 'change_id(wpuw)'`
- `jj evolog -r 589f5e33`
- `jj evolog -r 66fb0976`
- bounded `jj show`
- bounded cumulative `jj diff` comparisons
- bounded `jj abandon`
- bounded `jj restore`
- bounded `jj rebase -s dev -d main`

## Post-Repair Outcome
- Duplicate visible `wpuw` issue repaired.
- Single visible `wpuw` revision now at `c6af423c`.
- `66fb0976` content preserved in the surviving visible lineage.
- `dev` rebased onto latest `main`:
  - `main = 4ecb02f7`
  - `dev = 4f3fe3bb`
- Current working copy `@` contains only this report file and remains undescribed.
