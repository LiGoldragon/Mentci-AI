# Testing Context: component_registry

## Quick Checks
- `bb Components/scripts/component_registry/main.clj`
- `bb Components/scripts/component_registry/main.clj --format json`
- `bb Components/scripts/component_registry/main.clj --id scripts`

## Expected
- Valid index resolves to map keyed by component id.
- `--id` returns one resolved component entry.
- Missing path or duplicate id fails with non-zero exit.
