# Projection Equivalence Contract

## Statement
Each Saṃskāra object has equivalent text and binary projections. Meaning is invariant across projection.

## Equivalence Rules
1. One logical object ID per message.
2. Text projection and binary projection share the same semantic field set.
3. Content hash verification must confirm projection identity.
4. Field omission/addition requires versioned schema evolution.

## Validation Surface
- schema version compatibility,
- canonical ordering for hash stability,
- roundtrip verification (text -> object -> binary -> object -> text).

## Failure Handling
Projection mismatch is protocol-invalid and blocks execution until corrected.

## Migration Note
Filesystem-based state remains operational during transition, but execution-critical flows should progressively rely on projection-validated objects.
