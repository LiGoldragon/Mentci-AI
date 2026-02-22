# Survey: core/ -> Core/ Migration Audit

## Objective
Identify overlap, deficiencies, and obsolete artifacts that would interfere with migrating all `core/` files into `Core/`.

## Source Inventory
Current authoritative files in `core/`:
- `AGENTS.md`
- `ARCHITECTURAL_GUIDELINES.md`
- `ASKI_FS_SPEC.md`
- `ASKI_POSITIONING.md`
- `CONTEXTUAL_SESSION_PROTOCOL.md`
- `HIGH_LEVEL_GOALS.md`
- `INTENT_DISCOVERY.md`
- `MENTCI_AID.md`
- `SEMA_CLOJURE_GUIDELINES.md`
- `SEMA_NIX_GUIDELINES.md`
- `SEMA_RUST_GUIDELINES.md`
- `VERSION_CONTROL.md`
- plus sub-program set under `core/programs/`

## High-Impact Reference Density
- `core/` references across repository: 116
- `core/programs/` references: 22

This implies migration must be phased and guard-assisted; direct rename is likely to create broad path drift.

## Overlap Findings
1. Version-control authority overlap/drift
- `core/ARCHITECTURAL_GUIDELINES.md` points to `docs/architecture/VERSION_CONTROL.md`.
- `docs/architecture/JAIL_COMMIT_PROTOCOL.md`, `docs/architecture/TOOLS.md`, and `docs/architecture/RELEASE_PROTOCOL.md` also reference `docs/architecture/VERSION_CONTROL.md`.
- Canonical source is currently `core/VERSION_CONTROL.md`.

2. Restart-context authority overlap/drift
- `docs/architecture/RELEASE_PROTOCOL.md` references `docs/guides/RESTART_CONTEXT.md`.
- Canonical restart context is `core/programs/RESTART_CONTEXT.md`.

3. Historical supersession residue
- `docs/architecture/JjAutomation.md` states supersession but still points to `docs/architecture/VERSION_CONTROL.md` (non-canonical path in current model).

## Deficiencies
1. No alias contract
- There is no formal alias map declaring transitional equivalence:
  - `core/*` -> `Core/*`
  - `core/programs/*` -> `Library/*` (if adopted)

2. Guard gaps
- Current guards detect some path drift patterns (for `Inputs/`) but not stale `core/`/`core/programs/` references after a root migration.

3. Partial root canonicalization
- Root model now includes capitalized domain roots in spec/strategy language, but enforcement and file layout are still mixed (lowercase `core/` still active).

## Obsolescence Candidates (Path-Level, not content-level)
- `docs/architecture/JjAutomation.md` (if retained, should only point to canonical `core/VERSION_CONTROL.md` or future `Core/VERSION_CONTROL.md`).
- Any docs that still declare `docs/architecture/VERSION_CONTROL.md` as operational source.
- Any references to `docs/guides/RESTART_CONTEXT.md` where canonical is `core/programs/RESTART_CONTEXT.md`.

## .gitignore Relationship
- `.gitignore` has no rules blocking `core/`, `Core/`, or `Library/`.
- Therefore migration risk is not ignore-based; it is reference and tooling consistency based.
