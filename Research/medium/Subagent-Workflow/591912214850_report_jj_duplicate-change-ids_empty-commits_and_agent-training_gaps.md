# Report: JJ Duplicate Change IDs, Empty Commit Churn, and Agent Training Gaps

## Prompt
The user requested a deep investigation into why there are many duplicate JJ change IDs, why there are so many empty commits and dangling-looking commits outside trunk, and asked for stronger JJ training across skills/agents—especially `jj-expert`.

## Context
This repository uses JJ as the authoritative DVCS layer and Git only as transport. Repeated recent session evidence showed accidental empty finalization commits, duplicate visible change IDs that looked suspicious, and side bookmarks that appeared abandoned. The goal of this report is to separate normal JJ mechanics from actual workflow mistakes so the training surface can be corrected precisely.

## Summary
- JJ change IDs are stable identifiers for a logical change; commit IDs are the rewritten immutable snapshots.
- Multiple visible commits with the same change ID are usually **divergent changes** or re-exposed history, not corruption.
- Empty working-copy commits are normal in JJ after `jj new`, `jj commit`, `jj describe`, `jj edit`, and some rewrite/abandon flows.
- The real problem in this repository is not that empty working-copy commits exist; it is that agents sometimes **describe or move bookmarks onto clean/empty commits**, creating churn and confusion.
- Additional confusion comes from side bookmarks (`research`, `pzsskzpy`, `roomkzpy`) that remain outside the main integration lineage without clear merge/abandon discipline.
- The right fix is stronger JJ mental models and sharper preflight/finalization guardrails across skills, agent prompts, and command prompts.

## External JJ Mechanics
### Change ID vs commit ID
JJ distinguishes between:
- **change ID**: stable identity of the logical change
- **commit ID**: immutable snapshot hash that changes when JJ rewrites the change

This means one logical change can accumulate multiple commit IDs over time while keeping the same change ID.

### Why duplicate visible change IDs happen
Per JJ divergence guidance, multiple visible commits with the same change ID can appear when:
- a previously hidden revision becomes visible again,
- a bookmark points at an older hidden revision,
- `jj new REV`, `jj edit REV`, or a fetch/rebase re-exposes the older revision,
- two writers/processes independently rewrite the same change.

So duplicate visible change IDs are often a **history-shape symptom**, not a corruption symptom.

### Why empty commits happen
JJ intentionally keeps a working-copy commit. Empty commits normally arise from:
- `jj new` creating the next empty working revision,
- `jj describe` / `jj commit` finalizing the current revision and creating a new empty working-copy child,
- rewrite flows like `abandon`, `edit`, fetch/import, or restore that replace the working copy with a new empty revision.

So empty **working-copy** commits are normal and useful. The dangerous pattern is a **described empty commit** or moving an important bookmark onto one.

## Local Repository Diagnosis
### Duplicate visible change IDs in this repo
Bounded local evidence shows duplicated visible/hidden change IDs such as `knypsmkm` and `wsmrttls`. These were produced by repeated finalization on already-clean history:
- a clean tree was described,
- the bookmark was moved,
- then the now-empty or already-finalized change was rewritten again.

That creates multiple commit IDs for the same change ID and can leave the older revision hidden while the new one stays visible.

### Empty commit churn
Current repo evidence showed `wsmrttls` and `roomkzpy` as `(empty)` revisions. The important distinction:
- one anonymous empty `@` above the target bookmark is normal handoff state,
- a described empty commit with no intended payload is usually workflow noise.

The recurring mistake has been:
1. work is already complete on `@-`,
2. agent still finalizes `@` or retargets bookmark to `@`,
3. an empty described commit gets pushed or briefly occupies the bookmark,
4. a later repair moves the bookmark back.

### Dangling-looking non-trunk commits
Bookmarks like `pzsskzpy`, `research`, and `roomkzpy` show side histories that are not clearly integrated into `dev`. Some are legitimate side lines; others look effectively abandoned because agents created/finalized them without a clear merge/abandon policy.

The operational problem is not merely “dangling commits exist.” It is that agents do not clearly classify such states as:
- still-active side bookmark,
- integrated and safe to abandon,
- intentionally preserved research branch,
- or accidental leftover requiring cleanup.

## Root Causes in Agent Behavior
1. **JJ mental model gap:** agents treat same-change-id visibility like corruption instead of understanding divergence/rewrites.
2. **Missing clean-tree gate:** agents do not always run `jj status` + `jj diff --summary` immediately before `jj describe` or bookmark movement.
3. **Bookmark-finalization confusion:** agents sometimes target `@` instead of the finalized non-empty parent revision.
4. **Poor empty-commit distinction:** agents know “avoid empty commits” but do not distinguish normal anonymous empty `@` from harmful described empty commits.
5. **Weak side-bookmark discipline:** agents leave task/research bookmarks floating without integrating, preserving intentionally, or abandoning explicitly.
6. **Plan-example contamination:** some older plans still teach hard-coded `dev` / `main` pushes and `jj new main`, reinforcing static-target habits rather than runtime-bookmark discipline.

## Recommended Guardrails
1. Always run bounded preflight before finalization:
   - `jj status`
   - runtime bookmark resolution
   - bounded `jj log`
   - `jj diff --summary` when commit movement/finalization is in play
2. Never `jj describe` or finalize a clean tree unless doing a deliberate repair and explicitly justifying it.
3. Never move the runtime target bookmark to literal `@`; move it to the finalized non-empty immutable revision.
4. Teach explicitly that one anonymous empty working-copy commit above the bookmark is normal; multiple described empty commits are churn.
5. When duplicate visible change IDs are seen, diagnose divergence/hidden-history exposure first; do not call it corruption without stronger evidence.
6. When side bookmarks exist, classify them explicitly as active / integrated / intentionally preserved / cleanup candidates.
7. Remove hard-coded `dev` and `main` examples from training surfaces unless clearly labeled as examples for a specific release flow.

## Evidence Packet
### External sources
- JJ tutorial on changes/commits/revisions: explains stable change IDs vs mutable commit IDs.
- JJ divergence guide: explains how multiple visible commits can share one change ID.
- JJ CLI/config docs and user guides: explain bookmark push checks, `jj new`, and working-copy empty revisions.

### Local bounded evidence
- `jj log -r "knypsmkm|5de109ac7ebf" --no-graph -n 5`
- `jj log -r "wsmrttls|500ecb60" --no-graph -n 5`
- `jj log -r "roomkzpy|parents(roomkzpy)" --no-graph -n 5`
- `jj log -r "pzsskzpy|parents(pzsskzpy)" --no-graph -n 5`
- `jj log -r "research|parents(research)" --no-graph -n 5`
- `jj op log -n 5`

These bounded checks showed:
- repeated rewrites of the same change IDs,
- empty described revisions,
- explicit bookmark moves onto the wrong revision during finalization,
- and side bookmarks outside the integration lineage.

## Training Targets
Highest-value files to strengthen:
- `Core/VersionControlProtocol.md`
- `.pi/skills/independent-developer/SKILL.md`
- `.pi/skills/subagent-driven-development/SKILL.md`
- `.pi/skills/finishing-a-development-branch/SKILL.md`
- `.pi/agents/jj-expert.md`
- `.pi/agents/task.md`
- `.pi/agents/planner.md`
- `.pi/agents/reviewer.md`
- `.pi/agents/explore.md`
- `.pi/commands/jj-expert.md`
- `.pi/commands/jj-preflight.md`
- older plan/example docs that still hard-code `dev` / `main`

## Conclusion
The tree is not suffering from mysterious JJ corruption. It is suffering from incomplete JJ training:
- misunderstanding divergence,
- over-finalizing clean trees,
- poor bookmark targeting,
- and weak side-bookmark cleanup/integration habits.

The correct implementation response is to retrain the repository’s skills, agent prompts, and helper command prompts around JJ’s actual model, especially the distinction between change IDs vs commit IDs, normal empty working-copy commits vs harmful described empty commits, and runtime-bookmark-safe finalization.

programming: f2n0sy1d
