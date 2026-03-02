# Strategy: Cross-Component Integration

## Objective
Enforce a single source of truth for component topology and guarantee integration parity between `Components/index.edn` and live component roots.

## Research Alignment
- `Development/medium/Component-Dependency-Flow/Strategy.md`
- `Development/high/Top-Level-FS-Spec/Strategy.md`
- `Development/high/Project-Hardening/PLAN.md`

## Implementation Plan
1. Keep `Components/index.edn` synchronized with actual component directories.
2. Add a hard integration check in flake `checks` that fails on index/filesystem drift.
3. Keep packaging integration work isolated from registry integrity so topology checks remain reliable while package-specific failures are remediated.

## Acceptance
- `nix build .#checks.x86_64-linux.componentsIndex` passes.
- Component additions/removals require index updates in the same change.
