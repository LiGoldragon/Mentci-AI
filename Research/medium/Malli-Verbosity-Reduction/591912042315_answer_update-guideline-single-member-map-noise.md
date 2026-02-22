# Agent Report

- Date: `2026-02-22`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Malli-Verbosity-Reduction`
- Title: `Update Clojure Syntax Strategy: Avoid Single-Member Map Noise`

## Prompt

update strategy for new clojure syntax to avoid single-member maps like ":raw :string" which make the syntax redundant and noisy. Add to guideline and strategy that clojure is used to achieve maximum signal with minimal noise.

## Agent Answer

Applied the requested policy to both guideline and strategy.

Updated:
1. `Core/SEMA_CLOJURE_GUIDELINES.md`
- Added explicit **Signal-to-Noise Principle**: Clojure usage should maximize semantic signal with minimal syntax noise.
- Revised object definition guidance: avoid single-member maps for unary payloads unless additional named fields are semantically required.
- Updated examples to demonstrate unary scalar object schemas (`:string`) instead of `[:map [:raw :string]]` where appropriate.

2. `Development/Malli-Verbosity-Reduction/STRATEGY.md`
- Added goal/scope language for reducing redundant unary map wrappers.
- Updated schema-sugar target to include scalar-first unary declarations.
- Added plan items for optional `defscalar` alias and unary-map lint guard.
- Updated success criteria to reduce single-member maps to compatibility-only cases.

Result:
- Strategy direction now explicitly favors low-noise unary object representation while preserving Sema boundaries.

## Reporting Protocol Notes

- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.
