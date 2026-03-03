# Role-Capability Model

## Principle
Mutability is role-scoped and capability-scoped, not inferred from path visibility.

## Base Roles
- `MASTER_AGENT`
- `COMPONENT_AGENT`
- `RESEARCH_AGENT`
- `MAIN_AGENT`
- `AUDITOR_HUMAN_PROXY`

## Capability Examples
- `CAN_MUTATE_COMPONENT_CODE`
- `CAN_MUTATE_INTERCOMPONENT_CONTRACTS`
- `CAN_REQUEST_LANE_REWRITE`
- `CAN_ISSUE_EXECUTIVE_DECISION`
- `CAN_AUDIT_AND_RESOLVE`

## Filesystem Interaction
`AgentWorkspaceInit.workspaceComponents[]` declares mount mode per repo:
- `READ_ONLY` for consultation,
- `READ_WRITE` for mutation.

Capability checks remain mandatory even on writable mounts. Writable mount does not imply contract mutation authority.

## Contract Mutation Rule
Inter-component contracts are writable only when:
1. target repo mount is `READ_WRITE`, and
2. agent capability includes `CAN_MUTATE_INTERCOMPONENT_CONTRACTS`, and
3. lane policy allows lane operation context.
