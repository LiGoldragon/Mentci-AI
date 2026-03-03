# VersionOne — Saṃskāra-Centered Architecture Workspace

## Purpose
`VersionOne/` is the durable workspace for the first full protocolized architecture cycle where agent collaboration, lane governance, and cross-repository mutation are expressed as Saṃskāra messages.

The center of gravity is `VersionOne/samskara-layer/`, which defines:
- communication objects,
- deliberation/review loops,
- lane-governed execution of `jj` mutations,
- and the migration path from filesystem-centric state to schema-addressed data state.

## Scope
This directory tracks evolving architecture and logical components across three maturity states:
- `selected` — approved direction and active build target,
- `in-review` — proposals under active forum/argument cycle,
- `complete` — accepted v1 logical units with stable behavioral contract.

## Core Principle
All state transitions are represented as objects. In v1, object duality is preserved:
1. text projection (Aski/Datalog-oriented documents for reasoning and review),
2. binary projection (content-addressed Cap'n Proto transport for execution and replay).

## Directory Topology
- `samskara-layer/` — message model, governance flow, representation equivalence.
- `logical-components/` — selected/in-review/complete logical units.
- `decisions/` — accepted and in-review architectural decisions.
- `data/` — structured EDN authority snapshots and protocol sidecars.
- `templates/` — standardized message and review artifacts.

## Operational Intent
`VersionOne/` documents the transition where filesystem writes become progressively replaced by schema-validated data mutation and replay, while preserving compatibility with current repository-based workflows.
