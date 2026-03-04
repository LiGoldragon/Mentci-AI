# Aski–Cozo Bridge Report: Reader Rules and Delimiter Search

## Scope
Follow-up after implementing VersionOne CozoScript examples: assess Aski surface alignment with CozoScript and search prior repository research for a proposed `(||)` delimiter rule for markdown blobs with indentation normalization.

## Findings
1. **Aski already emphasizes delimiter-typed syntax** in existing specs:
   - `Library/specs/AskiDslGuidelines.md`
   - `Library/specs/AskiAstralDsl.md`
2. Current documented rule baseline:
   - `{}` => struct-like
   - `[]` => enum-like
   - macro expansion to canonical EDN required
3. **No direct `(||)` delimiter proposal was found** in current repository artifacts searched (Research/Library/Core/Sources and transcript-indexed files), including extracted ChatGPT-lineage research.
4. Closest matching intent in extracted research:
   - emphasis on delimiter-driven typing and advanced reader logic
   - mention of strict two-way minify/beautify formatting utility for Aski authoring

## Interpretation
The repository currently has strong support for delimiter-governed Aski semantics, but not yet a codified blob/dedent delimiter contract equivalent to `(||)`.

## Suggested Next Step
Create a dedicated v1 proposal artifact in `VersionOne/samskara-layer/representations/` defining:
- literal/blob delimiter candidates (including `(||)`),
- deterministic dedent/indent rules,
- serialization stability constraints for messaging/LLM input,
- one-to-one projection examples into CozoScript-compatible forms.
