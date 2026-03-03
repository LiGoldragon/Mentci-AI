# Lane Governance Model (v1)

## Executive Rule
`main` lane is executive authority for final integration decisions.

## Working Rule
`dev` and other non-main lanes perform active implementation and periodically converge with `main`.

## Mirroring Rule
Top-level lanes are mirrored into component repositories by name.

Example:
- `mentci-ai:dev` governs mutation rights for `component:dev` in mapped component repos.

## Rewrite Policy
Default lane rewrite policy is deny-by-default. Rewrite is allowed only through explicit approved governance path.

## Governance Paths
1. Direct executive decision path.
2. Forum path with deliberation objects and decision object.

## Forum Constraint
A refusal must return a structured response package to the request source:
- refusal reasons,
- concrete suggestions,
- open questions,
- required missing evidence.

## Escalation
If two full cycles do not resolve, system emits `HumanAuditorRequired` and keeps mutation blocked until resolution message arrives.
