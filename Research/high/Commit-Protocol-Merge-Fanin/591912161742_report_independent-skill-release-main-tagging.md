# Independent-developer release clarification (main + v0.12 tags)

## Prompt
Create a new main tagged release and ensure release instructions are clear in `.pi/skills/independent-developer/SKILL.md`.

## Changes
- Added **Tagged Release Mode (Main-Only)** section in `.pi/skills/independent-developer/SKILL.md`.
- Explicitly documented required current-era tag style `v0.12.x.x.x` and canonical form `v<cycle>.<sign>.<degree>.<minute>.<second>`.
- Clarified release push sequence: commit on main, create tag, push main, push tag, verify alignment.
- Updated completion checklist wording to avoid hardcoding `dev` and state `main` for release mode.
- Updated `Library/RestartContext.md` latest release tag marker for this release.

## Release Value
This reduces future ambiguity around bookmark target and tag semantics for operational releases.
