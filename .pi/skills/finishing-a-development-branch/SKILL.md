---
name: finishing-a-development-branch
description: Use when implementation is complete, tests pass, and you need to integrate, release, or clean branch state in JJ
---

> **Related skills:** Verify tests first with `/skill:verification-before-completion`. Consider `/skill:requesting-code-review` before release.
>
> **JJ guidance:** @.pi/skills/jj-basic/SKILL.md, @.pi/skills/jj-intermediate/SKILL.md, @.pi/skills/jj-expert/SKILL.md

## Repo-Local Nix Purity Rule
- Treat every repository as a self-contained world during Nix evaluation.
- Never reference files from a parent repo, sibling checkout, ad-hoc absolute path, or undeclared local path escape inside Nix code.
- If reusable Nix code is needed, it must live inside the active repository or arrive through a declared flake input; if we create that code, it belongs in a repository and our repository workflow remains JJ-first, with Git only as backend transport.
- Deep modules must not `../`-escape repo boundaries to find package code. Root-wire shared derivations from the active repo root and pass them down through module arguments / `specialArgs`.


# Finishing a Development Branch

## Overview

Mentci completion flow is JJ-first and release-aware.

**Core principle:** Verify → choose integration path → execute atomically → leave clean handover.

**Announce at start:** "I'm using the finishing-a-development-branch skill to complete this work."

## Process

### Step 1: Verify

Run relevant checks for the change. Do not proceed if failing.

### Step 2: Identify Target

Default integration target is the runtime target bookmark (`$MENTCI_TARGET_BOOKMARK`) unless the user says otherwise. Use `main` only for explicit release/integration flows.

### Step 3: Present Options

Present exactly these options:

1. Move bookmark to finalized commit (no release tag)
2. Create a tagged main release
3. Keep branch state as-is
4. Discard branch work

### Step 4: Execute Choice

Follow @.pi/skills/jj-intermediate/SKILL.md for routine finalization mechanics and @.pi/skills/jj-expert/SKILL.md for recovery/cleanup judgment. This skill defines the completion policy and option selection; `jj-agent` performs the bounded JJ execution and reports raw evidence. Use `jj-expert` only as fallback/rescue when the JJ state is ambiguous or recovery-heavy.

#### Option 1: Move bookmark (no tag)

Ask `jj-agent` to:
- finalize the current intent into the correct described revision,
- move `$MENTCI_TARGET_BOOKMARK` to that finalized revision,
- push the bookmark,
- return bounded verification showing `$MENTCI_TARGET_BOOKMARK` and `$MENTCI_TARGET_BOOKMARK@origin`.
Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.

#### Option 2: Tagged main release (required release path)

For release flows, use the original zodiac-ordinal version style:

- **Tag format:** `v<cycle>.<sign>.<degree>.<minute>.<second>`
- **Current-era expected prefix:** `v0.12.x.x.x`

Required sequence:

Ask `jj-agent` to:
1. ensure the release commit is on `main`,
2. create the signed release tag with the required version style,
3. push `main` and the tag,
4. return bounded verification for `main`, `main@origin`, and tag presence/signature.
Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.

Release notes/commit body should summarize major changes and include the solar date line.

#### Option 3: Keep as-is

Report current bookmark/revision and stop.

#### Option 4: Discard

Confirm intent, then ask `jj-agent` to abandon the target revisions with bounded before/after evidence. Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.

## Rules

- Follow @.pi/skills/jj-basic/SKILL.md for JJ authority and Git prohibition.
- Follow @.pi/skills/jj-intermediate/SKILL.md for bookmark movement, push verification, empty-working-node hygiene, and routine completion mechanics.
- Follow @.pi/skills/jj-expert/SKILL.md for duplicate change IDs, side-bookmark classification, cleanup judgment, or recovery.
- Release integration target is `main`.
- Release tags must use the original version style (`v0.12.x.x.x` in current-era shorthand).
- Do not claim release completion without tag verification.
- End with a clean handover state via `jj-agent` after push verification, using `jj-expert` only as fallback/rescue.

## Finalization Guardrails
Use @.pi/skills/jj-intermediate/SKILL.md for bounded preflight, bookmark safety, and empty-working-node hygiene before finalizing. Use @.pi/skills/jj-expert/SKILL.md when duplicate change IDs, side bookmarks, or cleanup classification enter the flow.

After the work is described, run `execute session-guard` and `execute root-guard` through `jj-agent` to certify the session narrative and filesystem invariants. Use `jj-expert` only as fallback/rescue when the JJ state is ambiguous or recovery-heavy. Confirm that a research artifact has been created or updated in `Research/<priority>/<Subject>/` for the completed prompt, because prompts without research coverage are not complete. Include this verification in your completion notes and relay any outstanding bookmark classifications before the final handoff.
