# Aski-FS Unification Blueprint

## Objective
Unify filesystem governance under Aski-FS so path semantics, root policy, and context derivation are controlled by one schema authority.

## Canonical Root Contract (Target)
- `[Sources Components Outputs Research Development Core Library]`
- Legacy `Inputs` is transitional only and must resolve to `Sources` through explicit mapping.

## Deliverables
1. Single Aski-FS root contract data source.
2. Guard scripts that load this contract instead of hardcoded root vectors.
3. Compatibility translation map (`Inputs` -> `Sources`) with removal criteria.
4. Context projection artifacts for component/script execution.

## Phase Plan

### Phase 1: Contract Extraction
- Extract canonical root enum and allowlists into one Aski-FS artifact.
- Artifact: `Library/specs/ASKI_FS_ROOT_CONTRACT.edn`.
- Include runtime exception roots (`.git`, `.jj`, `.direnv`, `target`) as explicit non-domain nodes.
- Add version marker to contract artifact for guard compatibility checks.

### Phase 2: Guard Refactor
- Refactor root/session guards to read enum and allowlist from the Aski-FS contract.
- Replace duplicated root constants in scripts with contract lookups.
- Fail fast when the loaded contract is stale or missing required keys.

### Phase 3: Path Translation Window
- Add compatibility translator for legacy `Sources/*` references.
- Emit deprecation warnings on legacy path usage.
- Track unresolved references as report artifacts under `Research/high/Top-Level-FS-Spec/`.

### Phase 4: Trusted Context Flow
- Generate context payloads from Aski-FS for scripts/components.
- Reduce per-file path boilerplate by using generated context objects.
- Keep generated context auditable (checked into report/output artifacts where needed).

### Phase 5: Cutover and Lock
- Remove legacy translator after reference sweep is zero.
- Enforce `Sources/*`-only path policy in root/session guards.
- Update `Library/specs/ASKI_FS_SPEC.md` and `Library/RESTART_CONTEXT.md` to mark convergence complete.

## Validation Gates
1. `bb Components/scripts/root_guard/main.clj` passes with contract-driven checks.
2. No unresolved `Inputs/` references in active code paths.
3. Session guard and report generation remain functional after cutover.
4. Top-level root inventory matches canonical enum + runtime exceptions only.

## Risks
- Hidden legacy paths inside generated files or archived docs.
- Drift between contract schema and guard loader expectations.
- Partial migration causing mixed `Inputs`/`Sources` behavior.

## Mitigation
- Run iterative sweeps with deterministic grep patterns.
- Introduce contract schema tests before guard cutover.
- Keep translator window short and observable via report logs.
