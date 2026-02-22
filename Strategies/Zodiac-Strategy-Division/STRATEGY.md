# Strategy: Zodiac Strategy Division

## Objective
Reorganize all strategy subjects into 12 symbolic categories that mirror the zodiac, so agents can orient quickly and find the right strategy domain without ambiguity.
The same zodiac category lattice must be applied to both `Strategies/` and `Reports/`
so discovery is bilateral: consulting one side requires consulting the mirrored side.

Naming format for category directories:
- `ZodiacSign-ShortAppropriateCategoryName`
- Example: `Aries-Initiation-Execution`

## Target Category Set (12)
1. `Aries-Initiation-Execution`
2. `Taurus-Stability-Infrastructure`
3. `Gemini-Interfaces-Communication`
4. `Cancer-Continuity-Context`
5. `Leo-Leadership-Authority`
6. `Virgo-Validation-Quality`
7. `Libra-Alignment-Coordination`
8. `Scorpio-Debugging-Recovery`
9. `Sagittarius-Expansion-Research`
10. `Capricorn-Operations-Delivery`
11. `Aquarius-Systems-Innovation`
12. `Pisces-Synthesis-Reflection`

## Scope
- Keep strategy subjects intact, but nest them under one zodiac-category container.
- Mirror report subjects under the same zodiac-category containers.
- Add symbolic correspondence spec for discoverability and orientation.
- Add deterministic mapping rules so agents can classify new subjects consistently.

## Filesystem Shape
- `Strategies/<Zodiac-Category>/<Subject>/...`
- `Reports/<Zodiac-Category>/<Subject>/...`

Mirror invariant:
- Every strategy subject path has a corresponding report subject path under the same
  zodiac category.
- Every report subject path has a corresponding strategy subject path under the same
  zodiac category.

## Classification Rules
For each strategy subject, choose category by primary intent:
- If focus is hardening, reliability, consistency -> `Taurus-Stability-Infrastructure`.
- If focus is validation, linting, guarantees, policy checks -> `Virgo-Validation-Quality`.
- If focus is protocol/governance/authority -> `Leo-Leadership-Authority`.
- If focus is debugging/recovery/purge/rewrite repair -> `Scorpio-Debugging-Recovery`.
- If focus is experimentation/research/expansion -> `Sagittarius-Expansion-Research`.
- If focus is session synthesis/finalization/integration -> `Pisces-Synthesis-Reflection`.
- If focus is release/commit pipelines/ops -> `Capricorn-Operations-Delivery`.

Tie-breaker:
- classify by dominant failure mode the strategy is designed to prevent.

## Migration Plan
1. Author spec first
- Create symbolic correspondence spec file (`Library/specs/STRATEGY_ZODIAC_CORRESPONDENCE.md`).

2. Build mapping table
- Create deterministic map: `Subject -> Zodiac-Category`.
- Include rationale sentence per mapping.

3. Move strategy and report directories
- Use `git mv` to preserve history.
- Update path references in `Core/`, `Library/`, strategy/readme/report indexes, and helper scripts.
- Update subject-unification tooling to resolve zodiac category + subject pairs.

4. Add compatibility aliases (temporary)
- Add lookup fallback for old `Strategies/<Subject>` paths where scripts assume flat layout.
- Add lookup fallback for old `Reports/<Subject>` paths where scripts assume flat layout.
- Remove fallback after full rewrite verification.

5. Enforce
- Extend guard tooling to fail on strategies or reports outside zodiac category containers.
- Extend guard tooling to fail if mirrored counterpart is missing on either side.

6. Consult-both workflow rule
- When an agent opens `Strategies/<Zodiac-Category>/<Subject>/...`, it must also open
  `Reports/<Zodiac-Category>/<Subject>/README.md` (or latest report entry) before
  declaring context acquisition complete.
- Same requirement in reverse when starting from reports.

## Risks
- High path churn across references.
- Tooling assumptions about `Strategies/<Subject>` flat layout.
- Duplicate symbolic interpretation across agents.

## Mitigations
- Phase migration: spec + map before any move.
- Introduce one canonical mapping file and require all tools to read it.
- Add guard checks before and after move.

## Acceptance Criteria
- Exactly 12 zodiac category directories exist under `Strategies/`.
- Exactly 12 zodiac category directories exist under `Reports/`.
- Every strategy subject is inside one (and only one) category.
- Every report subject is inside one (and only one) category.
- Symbolic correspondence spec is present and referenced from core guidance.
- Strategy/report discovery tooling resolves by `(category, subject)` pair.
- Mirror invariant passes: no orphaned subject on either side.
