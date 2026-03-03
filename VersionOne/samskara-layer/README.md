# Saṃskāra Layer (VersionOne)

## Role
The Saṃskāra Layer is the canonical exchange substrate for agent coordination, lane governance, and mutation authorization.

In v1, every significant action is represented as a message object before execution, including operations currently implemented through `jj` commands.

## Contract
The layer preserves two equivalent projections:
1. **Text projection** (Aski/Datalog-oriented): reviewable, searchable, argument-friendly.
2. **Binary projection** (Cap'n Proto + content address): executable, replayable, deterministic.

Neither projection is a secondary note. They are equivalent views over the same logical object.

## Execution Principle
`jj` is treated as an execution backend. Agents do not perform lane mutation directly. Instead:
- agent emits message object,
- policy actor evaluates authority,
- forum loop resolves contested operations,
- execution actor applies approved mutation,
- audit bundle records complete causal chain.

## Data Direction
The architecture begins with filesystem-governed mutation and moves toward schema-addressed data mutation, preserving practical compatibility during transition.
