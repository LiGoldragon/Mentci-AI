# D-0001: JJ Commands Become Saṃskāra Requests

## Context
Direct command execution lacks uniform policy control and object-level audit linkage.

## Decision
Lane-mutating `jj` operations are represented as protocol messages (`JjCommandRequest`) and executed only through policy-governed actor flow.

## Consequences
- uniform enforcement point,
- deterministic audit lineage,
- reduced accidental policy bypass.

## Follow-Up
Finalize binary and text schema equivalence for execution messages.
