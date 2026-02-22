# Agent Report

- Date: `2026-02-22`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Malli-Verbosity-Reduction`
- Title: `main Macro: Malli Lite Input Support`

## Prompt

no, I want main macro to create malli lite syntax types, otherwise its useless!

## Agent Answer

Implemented `main` macro support for Malli lite input syntax compilation.

Code change:
- `Components/scripts/lib/malli.clj`
  - Added `malli.experimental.lite` require.
  - Updated `main` macro so concise mode compiles non-symbol input forms with `ml/schema`.
  - Symbol input (for existing named schemas) remains supported.
  - Explicit function-schema form remains supported.

Behavior now:
1. Symbol schema:
- `(main MainInput [input] ...)`
2. Lite schema form:
- `(main {:args [:vector :string]} [input] ...)`
3. Explicit function schema:
- `(main [:=> [:cat MainInput] :any] [input] ...)`

Also updated docs:
- `Core/SEMA_CLOJURE_GUIDELINES.md`
- `Strategies/Malli-Verbosity-Reduction/STRATEGY.md`

Validation:
- Macro expansion verified with Babashka + Malli deps for lite-map input form.
- Existing script behavior still passes (`session_guard`).

## Reporting Protocol Notes

- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.
