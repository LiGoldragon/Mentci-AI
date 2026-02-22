# Top-Level Minimization Strategy

## Objective
Reduce repository root contents to:
1. The canonical 7 typed directories: `Sources/`, `Components/`, `Outputs/`, `Reports/`, `Strategies/`, `Core/`, `Library/`.
2. A strict allowlist of top-level files that must exist at root by design.

## Root Contract
Allowed top-level directories:
- `Sources`
- `Components`
- `Outputs`
- `Reports`
- `Strategies`
- `Core`
- `Library`

Allowed runtime/tooling directories (non-domain):
- `.git`
- `.jj`
- `.direnv`
- `target` (must stay gitignored)

Allowed top-level files (design-required):
- `flake.nix`
- `flake.lock`
- `Components/nix/jail.nix`
- `.gitignore`
- `.envrc`
- `AGENTS.md` (entry pointer to `Core/AGENTS.md`)

Conditional top-level files (keep only if still actively consumed by tooling):
- `README.md`
- `.attrs.json`
- `.opencode.edn`

Everything else must move under one of the 7 typed directories.
Temporary migration note:
- `Sources/` may exist only as a short-lived compatibility alias while references are rewritten to `Sources/`.

Current implementation notes:
- `ARCHITECTURE.md` moved to `Library/architecture/ARCHITECTURE.md`.
- `Work.md` moved to `Library/reports/WORK_CONTEXT.md`.
- `BUGS.md` moved to `Library/reports/BUGS.md`.
- `agent-sources.edn` moved to `Core/agent-sources.edn`.
- `jj-project-config.toml` replaced by local ignored `.mentci/jj-project-config.toml` and tracked template `Core/jj-project-config.example.toml`.

## Placement Rules
- Governance/protocol docs -> `Core/`
- Supporting docs/specs/guides/reports corpus -> `Library/`
- Executable/source/test/schema/workflow assets -> `Components/`
- Session/answer artifacts -> `Reports/`
- Planning artifacts -> `Strategies/`
- Runtime outputs/logs -> `Outputs/`
- External mounted substrate -> `Sources/` (read-only, gitignored)

## Execution Plan
1. Inventory and classify
- Build machine-readable inventory of all root paths.
- Classify each path as `domain`, `runtime`, `required-file`, `conditional-file`, or `violation`.

2. Freeze root allowlist
- Add root allowlist to `Core/ASKI_FS_SPEC.md` and guard scripts.
- Encode exception set (`.git`, `.jj`, `.direnv`, `target`).

3. Move violations
- Move non-allowlisted root paths into canonical destination roots.
- Prefer `git mv` to preserve history.
- Rewrite all path references in scripts/docs/tests immediately after each move batch.

4. Prune conditionals
- For each conditional top-level file, either:
  - keep at root with explicit rationale in `Core/ASKI_FS_SPEC.md`, or
  - relocate and leave a short deprecation stub only if needed for compatibility.

5. Enforce
- Add a root-check guard script that fails when unknown top-level entries appear.
- Run the guard in session checks and CI hooks.

## Guard Specification
Root check fails if:
- Any top-level directory is not in `{Sources, Components, Outputs, Reports, Strategies, Core, Library}` or runtime exception set.
- Any top-level file is not in the required-file set or explicit conditional allowlist.
- Any lowercase duplicate of canonical roots appears (`core/`, `strategies/`, `sources/`, etc.).

## Risks
- Tooling still hardcodes legacy root paths.
- Overzealous rewrites can produce malformed paths.
- Hidden operational dependencies on conditional root files.

## Mitigations
- Perform moves in small logical batches with immediate grep-based verification.
- Run guard + smoke tests after each batch.
- Keep a temporary compatibility map during transition, then remove.

## Acceptance Criteria
- Root tree contains only 7 typed domain directories + runtime exceptions + allowlisted root files.
- `rg` finds no active legacy root links in `Core/`, `Library/`, `Components/`.
- Root guard is enforced and documented in `Core/VERSION_CONTROL.md` and/or `Core/ARCHITECTURAL_GUIDELINES.md`.
- New sessions cannot reintroduce root sprawl without a failing check.
