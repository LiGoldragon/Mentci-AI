# Integration: Agent Authority Alignment with Existing Strategies

## Objective
Define how `agent-authority-alignment` composes with existing strategy sub-goals, so consistency work advances stabilization rather than competing with it.

## Integration Matrix

### 1. Artifact-Sweep
- Upstream overlap:
- Remove stale authority references and duplicated policy fragments.
- Shared sub-goal:
- Treat legacy path references (`docs/architecture/AGENTS.md`, lowercase `inputs/` where now incorrect) as instruction artifacts.
- Combined deliverable:
- Extend artifact detection rules to include stale authority/path patterns.

### 2. Project-Hardening
- Upstream overlap:
- Script autonomy and test coverage for tooling hardening.
- Shared sub-goal:
- Add reference-consistency checker as a script-level quality gate.
- Combined deliverable:
- New checker + test context integrated into existing script quality workflow.

### 3. Strategy-Development
- Upstream overlap:
- Automating strategy support tooling.
- Shared sub-goal:
- Standardize a repeatable workflow for path/authority drift detection in any future strategy.
- Combined deliverable:
- Reusable audit pattern for all strategy subjects.

### 4. Debugging
- Upstream overlap:
- Deterministic diagnosis of systemic failures.
- Shared sub-goal:
- Add "authority/path drift" as an explicit debug class when docs, runbooks, or scripts fail due to moved canonical paths.
- Combined deliverable:
- Debug protocol update covering stale-reference failures.

### 5. Attractor
- Upstream overlap:
- Goal 1 handoff reproducibility and audited runbooks.
- Shared sub-goal:
- Normalize runbook paths to `Inputs/` and canonical goal references to `core/HIGH_LEVEL_GOALS.md`.
- Combined deliverable:
- Reproducible Goal 1 documentation consistent with current filesystem and authority model.

### 6. Aski-Conversion and Aski-Refinement
- Upstream overlap:
- DSL/symbolic evolution depends on stable path and authority contracts.
- Shared sub-goal:
- Keep strategy references and implementation paths canonical so language work is not blocked by doc drift.
- Combined deliverable:
- Path-authority hygiene baseline for macro and DSL iteration.

### 7. Universal Program Pack
- Upstream overlap:
- Portable canonical path contracts across repositories.
- Shared sub-goal:
- Encode `core/AGENTS.md` as canonical authority path in pack manifests and migration notes.
- Combined deliverable:
- Portability model that carries authority contracts without reintroducing dual truth.

### 8. Mentci-RFS
- Upstream overlap:
- Read-first interface requires deterministic navigation and provenance.
- Shared sub-goal:
- Ensure UI/review surfaces anchor to canonical files (`core/AGENTS.md`, `core/HIGH_LEVEL_GOALS.md`) and `Inputs/`.
- Combined deliverable:
- Stable navigation targets for DAG-centric workspace tooling.

## Unified Sub-Goals (Execution Order)
1. Canonicalize authority references to `core/AGENTS.md`.
2. Normalize repository path references to `Inputs/` where path reality requires it.
3. Repair strategy/queue/task cross-links to current canonical files.
4. Add checker and validation hooks to prevent stale reference regression.
5. Feed checker patterns into Artifact-Sweep and Debugging protocols.

## Constraints
- Preserve historical provenance text when it records past state; annotate corrections instead of rewriting run history.
- Avoid broad refactors outside authority/path consistency scope.
- Keep commits atomic per intent group.

*The Great Work continues.*
