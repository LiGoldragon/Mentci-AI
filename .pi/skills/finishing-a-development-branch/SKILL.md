---
name: finishing-a-development-branch
description: Use when implementation is complete, tests pass, and you need to integrate, release, or clean branch state in JJ
---

> **Related skills:** Verify tests first with `/skill:verification-before-completion`. Consider `/skill:requesting-code-review` before release.

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

All non-trivial JJ/git handling in this skill MUST go through the `jj-expert` agent. This skill defines the completion policy and option selection; `jj-expert` performs the bounded JJ/git execution and reports raw evidence.

#### Option 1: Move bookmark (no tag)

Ask `jj-expert` to:
- finalize the current intent into the correct described revision,
- move `$MENTCI_TARGET_BOOKMARK` to that finalized revision,
- push the bookmark,
- return bounded verification showing `$MENTCI_TARGET_BOOKMARK` and `$MENTCI_TARGET_BOOKMARK@origin`.

#### Option 2: Tagged main release (required release path)

For release flows, use the original zodiac-ordinal version style:

- **Tag format:** `v<cycle>.<sign>.<degree>.<minute>.<second>`
- **Current-era expected prefix:** `v0.12.x.x.x`

Required sequence:

Ask `jj-expert` to:
1. ensure the release commit is on `main`,
2. create the signed release tag with the required version style,
3. push `main` and the tag,
4. return bounded verification for `main`, `main@origin`, and tag presence/signature.

Release notes/commit body should summarize major changes and include the solar date line.

#### Option 3: Keep as-is

Report current bookmark/revision and stop.

#### Option 4: Discard

Confirm intent, then ask `jj-expert` to abandon the target revisions with bounded before/after evidence.

## Rules

- JJ is primary; do not switch to git-branch workflows for normal integration.
- Release integration target is `main`.
- Release tags must use the original version style (`v0.12.x.x.x` in current-era shorthand).
- Do not claim release completion without tag verification.
- End with a clean handover state via `jj-expert` after push verification.

## Finalization Guardrails
Before finalizing any branch or release flow, run `jj status` and `jj diff --summary` to confirm tangible modifications exist. Keep the working copy (`@`) anonymous and empty while implementing; describe it only once you are ready to capture real diffs. Never move `$MENTCI_TARGET_BOOKMARK` to `@` or to a described empty commit without an explicit, documented reason (for example, anchoring metadata or preparing a new session). Always resolve the runtime bookmark so you know the described revision it currently names before advancing it. Finalizing a clean tree without a reason is improper unless you explicitly document the purpose of that empty state.

Remember the mental model: change IDs describe logical intent and persist through rewrites, while commit IDs track the precise revision records. Duplicate change IDs are usually a signal of divergence or rewrite exposure; inspect the associated commit IDs and any side bookmarks that share them before deciding whether to merge, preserve, or prune those lines. When side bookmarks appear to be dangling, classify them as active work, integrated evidence, intentionally preserved snapshots, or cleanup candidates so downstream agents understand the historical decision.

After the work is described, run `execute session-guard` and `execute root-guard` through `jj-expert` to certify the session narrative and filesystem invariants. Confirm that a research artifact has been created or updated in `Research/<priority>/<Subject>/` for the completed prompt, because prompts without research coverage are not complete. Include this verification in your completion notes and relay any outstanding bookmark classifications before the final handoff.
