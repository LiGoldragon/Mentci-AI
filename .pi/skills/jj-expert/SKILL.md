---
name: jj-expert
description: Advanced JJ recovery, rewrite, cleanup, and rescue judgment for difficult history situations in Mentci-AI repositories
---

# JJ Expert

**Authority:** `Core/VersionControlProtocol.md` remains the protocol authority. This skill is the advanced JJ recovery/rescue layer.

> **Lower levels:** Baseline concepts live in @.pi/skills/jj-basic/SKILL.md. Routine bounded execution lives in @.pi/skills/jj-intermediate/SKILL.md.

## Purpose
Use this skill when JJ work is no longer routine: history repair, rewrite/rebase judgment, duplicate change IDs, side-bookmark classification, cleanup of visible residue, nested-repo edge cases, or recovery from mistaken workflow actions.

**JJ means Jujutsu.** Even in rescue or recovery situations, we still only use Git through JJ; JJ remains the workflow authority and direct Git does not become the decision surface.

## Expert Rules
- **Fail closed when safety is unclear.** Do not improvise risky history mutation.
- **We only use Git through JJ.** Do not switch to direct Git because JJ state is confusing.
- **Keep all evidence bounded and current.** Do not rely on stale assumptions.
- **Prefer explanation before risky mutation.** Make the danger and intended recovery legible.

## Expert Judgment Areas
- **Duplicate change IDs** usually indicate rewrite/divergence exposure, not corruption.
- **Side bookmarks** must be classified before cleanup: active, integrated, intentionally preserved, or cleanup candidate.
- **Visible dangling residue** must not be left unexplained at completion time.
- **Nested JJ repos** must be reasoned about as separate JJ contexts even when root metadata also exists.

## Expert Use Cases
Use this skill for:
- rescue after mistaken VCS actions,
- advanced rebase/rewrite reasoning,
- side-bookmark cleanup,
- duplicate-change interpretation,
- release/recovery flows where routine instructions are insufficient,
- any contradictory evidence where a bounded direct post-gate is required.

## Expert Safety Posture
- Re-establish bounded current state before acting.
- Distinguish logical intent (change IDs) from exact revisions (commit IDs).
- Record the classification or recovery reasoning before destructive cleanup.
- If the safe path is still unclear, stop and ask for direction rather than forcing a rewrite.
