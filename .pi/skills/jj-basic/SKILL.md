---
name: jj-basic
description: Mandatory baseline JJ mental model for Mentci-AI agents and skills when repository state, bookmarks, or completion claims are involved
---

# JJ Basic

**Authority:** `Core/VersionControlProtocol.md` remains the protocol authority. This skill is the basic consumption layer.

> **Next levels:** Routine JJ execution lives in @.pi/skills/jj-intermediate/SKILL.md. Rewrite/recovery/rescue work lives in @.pi/skills/jj-expert/SKILL.md.

## Purpose
Use this skill whenever repository state, bookmarks, change IDs, nested repos, or completion claims enter the conversation.

**JJ means Jujutsu.** In Mentci-AI, JJ is the repository workflow/VCS replacement for Git. We only use Git through JJ: Git may exist as backend storage/transport, but JJ is the workflow authority.

## Non-Negotiable Rules
- **JJ is workflow authority.** Use `jj` as the source of truth for repository workflow decisions.
- **We only use Git through JJ.** Do not use Git directly as workflow authority when JJ can answer the question.
- **Origin is truth.** A local commit does not count as complete history until the relevant bookmark has been pushed to `origin` and verified there.
- **Nested JJ repos are still JJ repos.** A visible `.git` directory or submodule boundary does not authorize Git-first reasoning.

## Core Mental Model
- **Bookmarks** are the movable named pointers used for day-to-day JJ workflow.
- **Change IDs** describe logical intent across rewrites.
- **Commit IDs** identify the exact instantiated revision.
- **The empty working-copy node is normal.** Do not treat it as accidental debris by default.
- **Working-copy wrapper:** the current working-copy node `@` that wraps in-progress edits or handoff state. It may be empty and should not be pre-emptively named/described just to make later bookmarking easier.
- **Empty working-copy node ≠ empty commit.** A normal empty working-copy node may exist as transient workspace/handoff state. That does not make empty commits valid bookmark targets or valid pushes.
- **Description does not make a commit non-empty.** A revision with no content changes is still empty even if it has a message or description.

## Safe Basic Posture
- Start with bounded inspection, not broad history scans.
- Prefer explicit revset anchors and small limits.
- Keep the working copy (`@`) anonymous while work is still in progress.
- Prefer committing non-empty content with an explicit `jj commit -m` rather than pre-emptively naming an in-progress or empty node.
- Do not use `jj describe` on a working-copy wrapper or empty node just to get a description in place.
- Do not claim completion from local state alone.
- Do not move bookmarks onto empty revisions by accident.
- If the situation becomes rewrite-, rescue-, or cleanup-heavy, escalate to @.pi/skills/jj-expert/SKILL.md.

## Target Bookmark Pattern

**CRITICAL:** Always use `$MENTCI_TARGET_BOOKMARK` for your operations unless explicitly instructed otherwise.

```bash
# The variable is set in the Nix shell environment
echo $MENTCI_TARGET_BOOKMARK  # Typically "dev" or "main"

# Use it in all jj operations:
jj new "$MENTCI_TARGET_BOOKMARK"
jj bookmark move "$MENTCI_TARGET_BOOKMARK" --to '@-'
jj git push --remote origin --bookmark "$MENTCI_TARGET_BOOKMARK"
```

**When the variable is not set:**
- Default to `dev` for development work
- Default to `main` for release/production work

**Example:**
```bash
# Safe, portable command
TARGET="${MENTCI_TARGET_BOOKMARK:-dev}"
jj new "$TARGET"
```

## When This Level Is Enough
Use this skill by itself for:
- read-only orientation,
- understanding bookmark/change/commit terminology,
- deciding whether a nested repo is a separate JJ context,
- recognizing that Git is not the workflow authority,
- basic safety checks before delegating real JJ execution.

## Escalation
- For routine bounded JJ execution, finalization, push verification, and operational checklists, use @.pi/skills/jj-intermediate/SKILL.md.
- For recovery, rewrite, divergence, duplicate change IDs, side-bookmark cleanup, or other risky history work, use @.pi/skills/jj-expert/SKILL.md.
