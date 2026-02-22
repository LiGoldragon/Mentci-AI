# Strategy: Top Level FS Spec

## Objective
Define and enforce typed top-level filesystem ontology where first-letter-capitalized root directories map to explicit semantic types.

## Canonical Root Enum
`(enum [Sources Components Outputs Reports Strategies Core Library])`

In Aski-FS sugared syntax, this is:
`[Sources Components Outputs Reports Strategies Core Library]`

Rationale: in Aski-FS, `[]` denotes an enum declaration when used as a type-set in schema position.

## Scope
- Specify type semantics for each root directory.
- Define migration path from lowercase legacy roots to typed/capitalized canonical roots.
- Add validation hooks so new roots cannot drift.

## Current State Review (2026-02-22)
### Canonical Enum vs Observed Roots
Canonical enum:
- `Sources`
- `Components`
- `Outputs`
- `Reports`
- `Strategies`
- `Core`
- `Library`

Observed top-level directories currently include:
- Canonical-present: `Core`, `Library`, `Reports`
- Legacy/auxiliary still present: `strategies`, `docs`, `src`, `scripts`, `schema`, `nix`, `tasks`, `tests`, `tools`, `workflows`, `target`, `Logs`
- Runtime/infra directories also present: `.git`, `.jj`, `.direnv`

### Migration Status
1. Completed/partially completed:
- `core/` -> `Core/` (done)
- `core/programs/` -> `Library/` (done)
2. Not completed:
- `strategies/` -> `Strategies/`
- introduction of explicit `Components/` root
- normalization of remaining lowercase technical roots into canonical typed roots

### Structural Contradiction to Resolve
`Core/ASKI_FS_SPEC.md` defines `Components` as a canonical root in the enum, but operational text still references `src/` and `scripts/` directly as component paths. Strategy must first establish whether:
1. `Components/` becomes an actual top-level container, or
2. enum/spec is adjusted to model existing direct technical roots.

This strategy assumes option (1): materialize `Components/` and migrate.

## Root Type Semantics
- `Sources`: read-only external substrate and mounted references (formerly `Inputs`).
- `Components`: mutable implementation components that belong to Mentci-AI but are managed as independent VC subtrees/repositories.
- `Outputs`: generated/export artifacts for downstream consumption.
- `Reports`: prompt/session answer artifacts grouped by subject.
- `Strategies`: pre-implementation planning and research per subject.
- `Core`: canonical operating protocols and ontology docs.
- `Library`: reusable shared code and schemas.

### Components VC Model (Clarification)
`Components/` is not only a folder regrouping. It is intended to host subtrees with their own version-control trees so sub-components can evolve in parallel without blocking the root repository workflow.

Implications:
1. Each component subtree should have explicit sync policy (subtree/submodule/colocated strategy to be standardized).
2. Root-level CI/validation must support parallel component evolution and pinned integration points.
3. Migration plan must preserve standalone history for component projects where applicable.

## Implementation Phases
1. Specification
- Extend `Core/ASKI_FS_SPEC.md` with enum-root model and sugared syntax mapping.
- Add root contract table and allowed child patterns.

2. Compatibility Layer
- Keep current lowercase roots operational via explicit legacy aliases.
- Map `strategies/` -> `Strategies/`, `Core/` -> `Core/` until migration cutover.

3. Enforcement
- Add FS guard script to validate root set and casing.
- Fail CI/local checks on unknown top-level roots or wrong casing.

4. Migration
- Prepare ordered rename plan for root directories.
- Rewrite path references and update all guard allowlists.
- Validate JJ history safety and external tooling expectations.

## Recommended Migration Order (Reviewed)
1. Canonicalize strategies root first:
- `strategies/` -> `Strategies/`
- update all references in `Core/`, `Library/`, `Reports/`, scripts.

2. Materialize `Components/` root:
- create `Components/`
- move technical implementation roots under it with stable sub-layout:
  - `src/` -> `Components/src/`
  - `scripts/` -> `Components/scripts/`
  - `schema/` -> `Components/schema/`
  - `tasks/` -> `Components/tasks/`
  - `tests/` -> `Components/tests/`
  - `tools/` -> `Components/tools/`
  - `workflows/` -> `Components/workflows/`
  - `nix/` -> `Components/nix/`
  - for candidate independent projects, split into VC subtrees under `Components/` instead of plain moves

3. Handle non-canonical but intentional roots:
- `Logs/`: either map into `Outputs/Logs/` or add to enum as explicit durable audit root.
- `docs/`: either map to `Library/` (support tier) or split into `Core/`+`Library/` and remove root-level docs except compatibility stubs.
- `target/`: keep gitignored build artifact, not part of canonical typed set.

4. Materialize missing canonical roots:
- create `Strategies/` and `Outputs/` (if absent at cutover)
- migrate `Inputs/` -> `Sources/` as canonical mounted/read-only non-tracked substrate
- keep a temporary compatibility alias from `Inputs/` to `Sources/` until all references are rewritten.

5. Enforce:
- fail on new lowercase top-level roots unless explicitly temporary and allowlisted.
- validate that all root dirs are within canonical set or runtime allowlist (`.git`, `.jj`, `.direnv`, `target`).
- validate component VC boundaries (component manifests/pins present, integration refs resolvable).

## Risks
- Tooling/scripts hardcoded to lowercase root paths.
- External docs or agents referencing legacy paths.
- Cross-platform case sensitivity inconsistencies.
- `jj`/`git` automation breakage if path rewrites are not done atomically across scripts and docs.
- strategy/report counterpart scripts may regenerate lowercase subjects if subject-casing policy is not enforced centrally.

## Acceptance Criteria
- Aski-FS spec contains explicit enum-root contract.
- Guard script validates canonical top-level roots and case.
- Subject/report/strategy workflows remain operational post-cutover.
- A root inventory check shows only canonical typed roots plus approved runtime roots.
- No active references to migrated lowercase root paths remain.

## Aski-FS Unification Program (Begin Now)
Objective: move all filesystem control logic under Aski-FS as the authoritative language/spec, so repository path semantics are centrally declared and machine-checked.

Phase 1: Canonical Model
- Define `Sources` as the canonical root in Aski-FS contracts.
- Encode root enum, allowlists, and runtime exceptions in a single Aski-FS data source.
- Initial artifact path: `Library/specs/ASKI_FS_ROOT_CONTRACT.edn`.
- Treat filesystem semantics as schema authority, not scattered script constants.

Phase 2: Control-Plane Centralization
- Refactor guard/check scripts to read root contracts from Aski-FS data.
- Remove duplicated hardcoded root lists from script implementations.
- Keep compatibility aliases (`Inputs` -> `Sources`) behind explicit translation maps until cutover completes.

Phase 3: Trusted Context Flow
- Use Aski-FS declarations to generate/import context maps for scripts and components.
- Reduce per-file boilerplate path setup where runtime can derive context from Aski-FS contracts.
- Ensure generated context remains explicit and auditable, not implicit magic.

Phase 4: Convergence and Lock
- Remove legacy alias paths after reference sweep is clean.
- Enforce a single-path policy (`Sources/` only) in guards and session checks.
- Record migration completion in Reports and Restart Context.

## High-Priority Remediation: Authority Drift (Implemented)
Issue addressed: strategy-level `Sources` canonical model diverged from core authority docs still naming `Inputs` as canonical.

Implemented synchronization pass:
1. `Core/ASKI_FS_SPEC.md`
- Canonical enum switched to `Sources`.
- Directory semantics section renamed to `Sources`.
- Explicit compatibility note kept for transitional `Inputs/` alias.
2. `Core/ARCHITECTURAL_GUIDELINES.md`
- Reproducibility/assimilation text aligned to `Sources` naming.
3. `Core/AGENTS.md`
- Source-substrate bullets aligned to `Sources` naming with transition note.
4. `Library/RESTART_CONTEXT.md`
- Substrate and VCS guardrail text aligned to `Sources` naming.

Remaining migration window:
- Physical directory remains `Inputs/` today; compatibility alias policy remains active until full FS cutover.
