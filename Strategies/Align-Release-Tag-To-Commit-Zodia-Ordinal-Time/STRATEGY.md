# Strategy: Align Release Tag To Commit Zodia Ordinal Time

## Objective
Standardize true-solar time usage across session commits, releases, and prompt-handling protocol.

## Scope
- Backport prior `session:` commit messages to include canonical solar line:
  - `solar: <AnnoMundi>.<zodiac>.<degree>.<minute>.<second>`
- Enforce forward protocol so all future prompt sessions begin with solar baseline.
- Ensure release/version cycle mapping uses AM-offset:
  - `cycle = AnnoMundi - 5919` (`5919 -> 0`, `5920 -> 1`, ...).
- Enforce condensed chronographic filename prefix:
  - `YEARSIGNDEGREEMINUTESECOND_kind_title.md` (no separator in timestamp block).
- Keep `Reports/<Subject>/` and `Strategies/<Subject>/` synchronized.

## Implementation Plan
1. Update core authority docs:
   - `Core/AGENTS.md`
   - `Core/CONTEXTUAL_SESSION_PROTOCOL.md`
   - `Core/VERSION_CONTROL.md`
   - `Core/ARCHITECTURAL_GUIDELINES.md`
   - `Library/architecture/CHRONOGRAPHY.md`
   - `Library/architecture/RELEASE_PROTOCOL.md`
2. Update Chronos version format to dynamic cycle mapping (`year_am - 5919`).
3. Backport recent `session:` commit descriptions:
   - compute solar from each commit's own committer timestamp.
   - inject `solar:` line immediately under `session:` title if missing.
4. Verify:
   - no targeted `session:` commits missing `solar:` line.
   - rewritten history pushed to `dev`.
5. Gregorian filename normalization:
   - For Gregorian date-prefixed artifacts (`YYYYMMDD_*`), derive solar timestamp using
     `chronos --unix <epoch> --format am --precision second` at `00:00:00 UTC` for that date.
   - Emit fixed-width compact solar prefix as `YYYYSSDDMMSS` with zero-padded components.

## Risks
1. History rewrite can alter commit IDs and requires force push.
2. Chronos binary may be unavailable via Cargo if workspace build is broken; mitigation is direct `rustc` compile for the standalone chronos binary.
