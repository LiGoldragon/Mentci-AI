# Survey: Core Root Migration Audit

## Current Canonical Layout
- `Core/` holds authority mandates and language/protocol guidelines.
- `Library/` holds operational program modules and resumable system maps.

## Classification Review
Core authority set:
- `AGENTS.md`
- `ARCHITECTURAL_GUIDELINES.md`
- `AskiFsSpec.md`
- `AskiPositioning.md`
- `ContextualSessionProtocol.md`
- `HIGH_LEVEL_GOALS.md`
- `MentciAidProtocol.md`
- `SEMA_CLOJURE_GUIDELINES.md`
- `SEMA_NIX_GUIDELINES.md`
- `SEMA_RUST_GUIDELINES.md`
- `VersionControlProtocol.md`

Library program set:
- `RestartContext.md`
- `StrategyQueue.md`
- `StrategyDevelopment.md`
- `ObsolescenceProtocol.md`
- `IntentDiscovery.md`

## Findings
- Symbiotic split is coherent: constitutional rules are in `Core/`, operational loops/programs are in `Library/`.
- Main residual risk is stale references in legacy documents, not filesystem structure.
