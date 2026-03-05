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

Default integration target is `main` unless the user says otherwise.

### Step 3: Present Options

Present exactly these options:

1. Move bookmark to finalized commit (no release tag)
2. Create a tagged main release
3. Keep branch state as-is
4. Discard branch work

### Step 4: Execute Choice

#### Option 1: Move bookmark (no tag)

- Finalize commit description.
- `jj bookmark set main -r <finalized-rev>`
- `jj git push --bookmark main`
- Verify with `jj log -r main -r 'main@origin' --no-graph`

#### Option 2: Tagged main release (required release path)

For release flows, use the original zodiac-ordinal version style:

- **Tag format:** `v<cycle>.<sign>.<degree>.<minute>.<second>`
- **Current-era expected prefix:** `v0.12.x.x.x`

Required sequence:

1. Ensure release commit is on `main`.
2. Create a **signed** git tag on that commit:
   - `git tag -s v0.12.<degree>.<minute>.<second> -m "release: v0.12.<degree>.<minute>.<second>"`
   - Or full canonical form when not in current-era shorthand: `v<cycle>.<sign>.<degree>.<minute>.<second>`
3. Push bookmark and tag:
   - `jj git push --bookmark main`
   - `git push origin <tag>`
4. Verify:
   - `jj log -r main -r 'main@origin' --no-graph`
   - `git tag -v <tag>`

Release notes/commit body should summarize major changes and include the solar date line.

#### Option 3: Keep as-is

Report current bookmark/revision and stop.

#### Option 4: Discard

Confirm intent, then abandon target revisions with `jj abandon <revset>`.

## Rules

- JJ is primary; do not switch to git-branch workflows for normal integration.
- Release integration target is `main`.
- Release tags must use the original version style (`v0.12.x.x.x` in current-era shorthand).
- Do not claim release completion without tag verification.
- End with clean handover commit (`jj new main`) when flow is complete.
