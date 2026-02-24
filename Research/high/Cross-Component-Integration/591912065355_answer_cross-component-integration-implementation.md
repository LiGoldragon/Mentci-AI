# Research: Cross-Component Integration Implementation

## Prompt Scope
Deep project research on integrating components with current architecture/guideline streams, then implementation.

## Context Ingested
1. `Core/ARCHITECTURAL_GUIDELINES.md`
2. `Core/VersionControlProtocol.md`
3. `Development/medium/Component-Dependency-Flow/Strategy.md`
4. `Development/high/Top-Level-FS-Spec/Strategy.md`
5. `Development/high/Project-Hardening/PLAN.md`

## Findings
1. `Components/index.edn` drifted from reality: several live components were missing; one stale component (`tests`) was listed.
2. Existing architecture already defines `Components/index.edn` as central dependency source.
3. No enforced parity gate existed in flake checks for index vs filesystem root directories.

## Implementation
1. Updated `Components/index.edn` to mirror live top-level component directories and interfaces.
2. Added `Components/nix/components_index_check.nix`:
- Parses component paths from `Components/index.edn`.
- Compares against actual `Components/*` directories.
- Fails on either missing index entries or missing directories.
3. Wired check into flake:
- `Components/nix/default.nix`: exports `components_index_check`.
- `flake.nix`: adds `checks.componentsIndex = namespace.components_index_check`.

## Verification
- `nix build .#checks.x86_64-linux.componentsIndex` passes.

## Packaging Integration Status
- `codingAgent` packaging remains blocked by upstream TypeScript workspace resolution issues in `pi-mono` (`@mariozechner/pi-*` modules unresolved during `tsgo` build). This is independent from component-index integration and remains a separate remediation stream.
