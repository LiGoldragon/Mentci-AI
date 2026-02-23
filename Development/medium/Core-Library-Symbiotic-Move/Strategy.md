# Strategy: Core Library Symbiotic Move

## Objective
Symbiotically move and streamline authority/program artifacts by enforcing a strict split:
- `Core/` = instructions and constitutional governance.
- `Library/` = executable/operational programs and system loops.

## Streamlining Classification
### Keep in Core
- global mandates and authority hierarchy (`AGENTS`, architecture, goals, version-control, session protocol)
- language SEMA guidelines
- filesystem ontology and positioning docs

### Keep in Library
- state resumption and operational program map (`RESTART_CONTEXT`)
- strategy lifecycle/queue programs
- obsolescence protocol
- intent discovery program

### Cross-over Moves Applied
- `core/programs/*` -> `Library/*`
- `core/IntentDiscovery.md` -> `Library/IntentDiscovery.md`
- `core/*` mandate files -> `Core/*`

## Risks
- naive path rewrites can break code identifiers (example class of risk: namespace text like `clojure.core`)
- historical report/path quotations can be unintentionally normalized

## Hardening Controls
1. Path rewrite scripts must protect known code identifiers and namespace tokens.
2. Reference guards must scan `Core/` + `Library/` and fail on new stale lowercase-root references.
3. Preserve historical quotations only with explicit archival markers.

## Acceptance Criteria
- all active authority links point to `Core/*`
- all active program-loop links point to `Library/*`
- no active `core/` or `core/programs/` references remain
- subject/report counterpart remains synchronized
