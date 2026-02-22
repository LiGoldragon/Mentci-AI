# Strategy: Core/Library Astral Migration

## Objective
Plan a safe transition to:
- `Core/` for core programming authority content (currently `core/`).
- `Library/` for current `core/programs/` modules.
- Aski-Astral layer with `{ Core Sol Library Luna }` and planet mapping for remaining FS domains.

## Target Model

### Canonical Typed Roots
- Existing canonical root enum:
  - `(enum [Inputs Components Outputs Reports Strategies Core Library])`
  - Sugared Aski-FS: `[Inputs Components Outputs Reports Strategies Core Library]`

### Aski-Astral Overlay
- Inner governance set: `{ Core Sol Library Luna }`
- Proposed meaning:
  - `Core`: constitutional programming mandates (protocols, architecture, sema).
  - `Sol`: central execution/orchestration gravity (runtime engine and primary control flow).
  - `Library`: reusable program modules and resumable program map (today's `core/programs/`).
  - `Luna`: reflective/adaptive control loops (debugging, obsolescence, strategy evolution interfaces).

### Planet Mapping (remaining FS)
- `Inputs` -> `Mercury` (external signal ingress, fastest change exposure).
- `Components` -> `Mars` (active implementation and transformation surface).
- `Outputs` -> `Venus` (presentation/export artifacts for upstream consumers).
- `Reports` -> `Jupiter` (session knowledge accumulation and audit payloads).
- `Strategies` -> `Saturn` (long-horizon planning, ringed decomposition).
- `Schema`/formal spec substrate (`schema/`, `docs/specs/`) -> `Uranus` (axiomatic/structural layer).
- `Logs`/observability (`Logs/`) -> `Neptune` (deep memory and telemetry).

## Migration Phases

### Phase 1: Compatibility Contracts (no path move)
1. Introduce explicit alias table in `core/ASKI_FS_SPEC.md`:
   - `core/` => future `Core/`
   - `core/programs/` => future `Library/`
2. Document astral overlay in FS spec + dedicated strategy notes.
3. Add guards that reject new lowercase-root references for migrated domains.

### Phase 2: Mechanical Rewrite Preparation
1. Enumerate all path references for `core/` and `core/programs/`.
2. Add scripted rewrite plan (ordered): docs -> scripts -> strategies -> reports.
3. Validate toolchain assumptions (jj helpers, launchers, guards, report scripts).

### Phase 3: Root Moves
1. Move `core/` to `Core/`.
2. Move `Core/programs/` to top-level `Library/`.
3. Update all references atomically in one bounded rewrite window.

### Phase 4: Enforcement
1. Add reference guard checks for stale `core/` and `core/programs/` references.
2. Add FS-root validation script for enum roots + astral annotations.
3. Add CI/local gate command sequence.

## Risks
- High reference volume causes partial-migration drift.
- Tooling may assume lowercase paths.
- Cross-platform case sensitivity can hide stale references.
- Historical docs/reports quoting old paths may need explicit legacy-note exemptions.

## Acceptance Criteria
- No active references to `core/` or `core/programs/` outside explicit legacy notes.
- `Core/` and `Library/` recognized by guards/specs as canonical.
- Aski-FS + Aski-Astral docs both define consistent root typing and planet mapping.
- All core automation scripts pass with new paths.

