# Lane Governor Actor

## Role
Single authoritative execution gate for lane-mutating operations.

## Responsibilities
- validate request authority against role/capability context,
- enforce lane policy,
- execute approved `jj` operations,
- emit deterministic execution and audit objects.

## Inputs
- `AgentWorkspaceInit`
- `JjCommandRequest`
- `PolicyContext`
- `DeliberationDecision` (when required)

## Outputs
- `ExecutionResult`
- `PolicyViolation`
- `DeliberationRequest`
- `AuditTrailBundle`
