# Symbolic Correspondence Spec (Strategy Zodiac)

## Purpose
Provide a shared symbolic vocabulary so agents can rapidly situate strategy intent and discover relevant strategy domains.

## Canonical Table
- `Aries` -> initiation, decisive execution, first-implementation moves
- `Taurus` -> durability, stability, infrastructure hardening
- `Gemini` -> interfaces, translation layers, communication contracts
- `Cancer` -> continuity, context persistence, session memory
- `Leo` -> authority, governance, command hierarchy
- `Virgo` -> validation, linting, quality and correctness enforcement
- `Libra` -> alignment, parity, bidirectional consistency
- `Scorpio` -> debugging, deep failure analysis, recovery operations
- `Sagittarius` -> expansion, exploration, research and external scanning
- `Capricorn` -> operations, release discipline, delivery pipelines
- `Aquarius` -> systems redesign, unconventional architecture, innovation
- `Pisces` -> synthesis, reflection, final integration and session closure

## Naming Rule
Category directory names must use:
- `<Zodiac>-<ShortAppropriateCategoryName>`
- ASCII only, hyphen-separated, title-cased segments.

## Assignment Rule
Each strategy subject must be assigned by dominant operational intent (not by incidental tasks).
The corresponding report subject must use the same zodiac category assignment.

## Bilateral Mirror Rule (Strategy <-> Report)
For each subject assignment:
- Strategy path: `Development/<Category>/<Subject>/`
- Report path: `Research/<Category>/<Subject>/`

Consultation contract:
- Category lookup is not complete until both sides are checked:
  - strategy intent/plan (`Development/...`)
  - report evidence/state (`Research/...`)

## Future Tooling Contract
A machine-readable map should be kept at:
- `Development/Zodiac-Strategy-Division/MAP.edn`

Schema (proposed):
- `{:version 1 :assignments [{:subject "..." :category "Aries-Initiation-Execution" :rationale "..."}]}`
