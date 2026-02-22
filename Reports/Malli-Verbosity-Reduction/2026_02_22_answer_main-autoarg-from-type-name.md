# Agent Report

- Date: `2026-02-22`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Malli-Verbosity-Reduction`
- Title: `main Macro Auto-Arg Derivation from Type Name`

## Prompt

Ok I meant (main Inputs [input] ...) then ... but can we turn Input into [input]? So we end up with (main Input ...) and 'input' is used in the function body (un-capitalized from the capitalized type name)?

## Agent Answer

Implemented auto-arg derivation in `main` macro.

New supported form:
- `(main Input ...body...)` -> derives args as `[input]`
- `(main MainInput ...body...)` -> derives args as `[mainInput]`

Existing forms still supported:
- `(main Input [input] ...body...)`
- `(main {:args [:vector :string]} ...body...)` (Malli lite input form)
- `(main [:=> ...] [args] ...body...)` (explicit function schema)

Implementation:
- `Components/scripts/lib/malli.clj`
  - Added auto-arg mode when second form is not a vector.
  - Symbol schemas derive arg name by lowercasing first character.
  - Non-symbol schemas in auto mode use fallback arg `[input]`.

Validation:
- Macro expansion verified for all forms, including:
  - `Input -> [input]`
  - `MainInput -> [mainInput]`
  - lite map schema form.

Docs synced:
- `Core/SEMA_CLOJURE_GUIDELINES.md`
- `Strategies/Malli-Verbosity-Reduction/STRATEGY.md`

## Reporting Protocol Notes

- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.
