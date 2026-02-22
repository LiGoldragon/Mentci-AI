# Strategy: Top Level FS Spec

## Objective
Define and enforce typed top-level filesystem ontology where first-letter-capitalized root directories map to explicit semantic types.

## Canonical Root Enum
`(enum [Inputs Components Outputs Reports Strategies Core Library])`

In Aski-FS sugared syntax, this is:
`[Inputs Components Outputs Reports Strategies Core Library]`

Rationale: in Aski-FS, `[]` denotes an enum declaration when used as a type-set in schema position.

## Scope
- Specify type semantics for each root directory.
- Define migration path from lowercase legacy roots to typed/capitalized canonical roots.
- Add validation hooks so new roots cannot drift.

## Root Type Semantics
- `Inputs`: read-only external substrate and mounted references.
- `Components`: mutable implementation components (runtime-building units).
- `Outputs`: generated/export artifacts for downstream consumption.
- `Reports`: prompt/session answer artifacts grouped by subject.
- `Strategies`: pre-implementation planning and research per subject.
- `Core`: canonical operating protocols and ontology docs.
- `Library`: reusable shared code and schemas.

## Implementation Phases
1. Specification
- Extend `core/ASKI_FS_SPEC.md` with enum-root model and sugared syntax mapping.
- Add root contract table and allowed child patterns.

2. Compatibility Layer
- Keep current lowercase roots operational via explicit legacy aliases.
- Map `strategies/` -> `Strategies/`, `core/` -> `Core/` until migration cutover.

3. Enforcement
- Add FS guard script to validate root set and casing.
- Fail CI/local checks on unknown top-level roots or wrong casing.

4. Migration
- Prepare ordered rename plan for root directories.
- Rewrite path references and update all guard allowlists.
- Validate JJ history safety and external tooling expectations.

## Risks
- Tooling/scripts hardcoded to lowercase root paths.
- External docs or agents referencing legacy paths.
- Cross-platform case sensitivity inconsistencies.

## Acceptance Criteria
- Aski-FS spec contains explicit enum-root contract.
- Guard script validates canonical top-level roots and case.
- Subject/report/strategy workflows remain operational post-cutover.
