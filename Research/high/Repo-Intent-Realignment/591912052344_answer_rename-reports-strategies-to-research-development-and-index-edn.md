# Agent Report

- Chronography: `12.5.23.44 | 5919 AM`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Repo-Intent-Realignment`
- Title: `rename-reports-strategies-to-research-development-and-index-edn`

## Prompt

update all references to a Report/ or Strategy/ to an index.edn file will will be kept to document all R&D - oh and rename Report to Research, and Strategy to Development

## Agent Answer

Implemented a full Research/Development namespace migration with index-based subject documentation.

Changes:
1. Renamed top-level trees:
- `Reports/` -> `Research/`
- `Strategies/` -> `Development/`

2. Updated repository references from old path namespaces to new ones:
- `Reports/...` -> `Research/...`
- `Strategies/...` -> `Development/...`

3. Added/standardized `index.edn` files for Research and Development topic indexes:
- `Research/index.edn`
- `Development/index.edn`
- `Research/<priority>/<Subject>/index.edn` generated from existing topic README entries.

4. Updated tooling to operate on the new namespace and index model:
- `Components/scripts/subject_unifier/main.clj`
  - now scans `Research/` + `Development/`
  - topic index path now resolves to `index.edn`
- `Components/scripts/answer_report/main.clj`
  - report output now writes into `Research/<tier>/<Subject>/...`
  - subject tier resolved from `Development/<tier>/<Subject>/`
  - topic index bootstrap uses `index.edn`
- `Components/scripts/reference_guard/main.clj`
  - roots switched from old trees to new trees
- `Components/scripts/root_guard/main.clj`
  - canonical root set switched to `Research` and `Development`

5. Updated core/library references to new names and index format:
- `Core/AGENTS.md`
- `Core/ContextualSessionProtocol.md`
- `Core/VersionControlProtocol.md`
- `Library/RestartContext.md`
- `Library/StrategyDevelopment.md`
- `Library/StrategyQueue.md`
- `Library/specs/AskiFsSpec.md`
- `Library/specs/ASKI_FS_ROOT_CONTRACT.edn`

Validation:
1. `execute root-guard` passed.
2. `execute unify` passed (26/26 parity, no missing indexes).
3. `execute link-guard` passed.
4. `execute root-guard` still fails on pre-existing top-level drift: `outputs`, `result`.

## Reporting Protocol Notes

- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.
