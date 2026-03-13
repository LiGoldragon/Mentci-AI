# CriomOS deployer and GitHub-source Maisiliym operator guidance

## Intent
Persist the execution-handoff decisions for the manifest-driven CriomOS deployment lane after the Prometheus/Ouranos recovery work.

## What changed
- Added a project-local `criomos-deployer` agent definition for bounded exact-attr CriomOS deployment work.
- Added a separate execution packet for the Prometheus lane at `docs/plans/2026-03-13-criomos-deployer-execution-packet.md`.
- Updated CriomOS operator docs to:
  - prefer manifest-driven deploys,
  - forbid `<nixpkgs>` / `NIX_PATH` style commands in favor of flake/registry usage,
  - prefer GitHub-source Maisiliym overrides (`github:LiGoldragon/maisiliym`) instead of local path overrides.
- Added `maisiliym` to the root component registry surfaces and created a component anchor directory under `Components/maisiliym/`.
- Updated the root flake input from `github:LiGoldragon/maisiliym/prometheus-node` to `github:LiGoldragon/maisiliym`.

## Verification
### Successful bounded checks
- Read-back of all touched files confirmed the new agent/doc text and component registry entries are present.

### Verification blocker discovered
A targeted run of `nix build .#checks.x86_64-linux.componentsIndex --no-link` exposed two issues:
1. A pre-existing parser bug in `Components/nix/components_index_check.nix` greedily parsed one-line EDN entries. This was fixed by switching to a bounded `grep -o ':path "[^"]*"'` extraction.
2. After the parser fix, the check still fails because the repository currently has pre-existing component catalog / filesystem mismatches (missing checked-out component directories and extra component directories without index entries), for example:
   - missing indexed directories: `aski-cli`, `chronos`, `mentci-aid`, `mentci-execute`, `maisiliym`, ...
   - missing index entries for existing directories: `contracts`, `criome-contract`, `mentci-intel`, `scripts`

This means `componentsIndex` is not green yet, but the remaining failure is broader than the Maisiliym addition alone.

## Operational guidance
- For deployment agents, use exact attrs and `execute deploy-manifest` only.
- Prefer Yggdrasil transport first for Prometheus.
- Avoid local-path Maisiliym overrides in this lane.
- Use flake-native operator commands and registry package references such as `nix shell nixpkgs#jq`.

## Follow-up
- Decide whether `Components/index.edn` should represent only checked-out component directories or the full logical component universe.
- If the former, prune or gate entries for absent components.
- If the latter, relax or redesign `components_index_check` so it tolerates declared-but-unchecked-out components.
- Verify whether the root `maisiliym` flake input should permanently track the default GitHub ref or a specific integration branch/bookmark.
