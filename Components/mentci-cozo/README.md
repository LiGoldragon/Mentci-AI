# mentci-cozo (MVP)

Light MVP component for expressing agent handoff constraints as a compact CozoScript dialect.

## Common use-case
Translate a structured handoff contract into CozoScript facts and a query over `handoff_constraint`.

## Rust API
- `compile_handoff_to_cozoscript(task_id, &HandoffContract)`
- `validate_constraints(&[LogicalConstraint])`

## Supported operators (MVP)
- `eq`
- `neq`
- `contains`
- `not_contains`
- `exists`
