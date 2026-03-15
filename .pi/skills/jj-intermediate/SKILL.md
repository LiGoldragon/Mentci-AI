---
name: jj-intermediate
description: Routine bounded JJ workflow for day-to-day Mentci-AI development, bookmark-safe execution, and completion hygiene
---

# JJ Intermediate

**Authority:** `Core/VersionControlProtocol.md` remains the protocol authority. This skill is the routine JJ execution layer.

> **Companion levels:** Start with @.pi/skills/jj-basic/SKILL.md for the baseline mental model. Escalate to @.pi/skills/jj-expert/SKILL.md when rewrite/recovery/rescue judgment is required.

## Purpose
Use this skill for normal JJ work: bounded inspection, intent commits, bookmark-safe finalization, push verification, review-range preparation, and everyday completion hygiene.

**JJ means Jujutsu.** This is the normal day-to-day VCS/workflow lane in this repository. Do not fall back to Git for routine workflow questions or operations.

## Routine Rules
- **Direct Git workflow usage is forbidden.** Git is backend transport only.
- **Use no-editor JJ automation.** Prefix automated JJ shell commands with `env JJ_EDITOR=: VISUAL=: EDITOR=:`.
- **Keep revsets bounded.** Use explicit anchors and limits.
- **Treat bookmark move + push as one completion moment.** Local completion claims are invalid without remote verification.
- **Prefer `jj commit -m` on the intended non-empty revision.** Avoid pre-emptive `jj describe` on `@`, on a working-copy wrapper, or on an empty node.
- **Keep the working copy anonymous while work is still in progress.** Anonymous here means: bounded inspection (`jj status`, bounded `jj log`, `jj diff --summary`) is fine, but do not name/describe the working-copy node before real content is ready to be captured.
- **Never move a runtime bookmark to an empty commit by accident.** A normal empty working-copy node is not permission to target or push an empty commit.

## Routine Preflight
Before non-trivial JJ work:
1. Establish bounded state with `jj status`.
2. Resolve the runtime target bookmark in the current repository context.
3. Inspect a bounded log around the runtime bookmark, `@`, and `@-`.
4. Run `jj diff --summary` when commit/finalization state matters.

## Routine Execution Pattern
- Inspect first.
- Make one logical change at a time.
- Verify that real non-empty content exists before naming/finalizing the revision.
- Capture the intended non-empty revision with `jj commit -m` rather than trying to pre-name the working-copy wrapper.
- Keep completion evidence explicit.
- Verify bookmark alignment on `origin` before claiming success.
- Leave a clean handoff state after verified completion.

## Routine Uses
Use this skill for:
- preparing bounded review ranges,
- routine preflight before editing or finalizing,
- intent/finalization work through `jj-agent`,
- routine bookmark movement and push verification,
- everyday nested-repo JJ handling.

## When To Escalate
Escalate to @.pi/skills/jj-expert/SKILL.md when you encounter:
- rewrite or rebase ambiguity,
- duplicate change IDs,
- side-bookmark classification/cleanup,
- recovery from mistaken history operations,
- any situation where safety is unclear.
