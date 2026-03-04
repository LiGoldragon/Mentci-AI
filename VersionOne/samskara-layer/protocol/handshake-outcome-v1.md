# Handshake Outcome v1 — Explanation and Decision Surface

## Why this matters
With multiple Saṃskāra databases in one repository (master + sidecar DBs), a handshake is the boundary object that decides whether two nodes can:
- talk,
- exchange reads,
- replicate state,
- recover/fail back safely.

Without a strong handshake, cross-DB coordination becomes implicit and brittle.

## Three candidate handshake outcomes

### A) Capability-only
Fields:
- `nodeId`
- `dbId`
- `protocolVersion`
- `roles`
- `allowedOps`

Pros:
- smallest payload
- easiest to implement

Cons:
- no state compatibility proof
- no liveness lease
- weak failback guarantees

Best for:
- very early local prototyping only.

---

### B) Capability + state digest
Fields:
- all of A
- `schemaHash`
- `dataSnapshotHash`
- `lastTx`

Pros:
- prevents schema/state mismatch
- supports deterministic sync reasoning

Cons:
- still no active lease semantics
- cannot robustly detect stale ownership during failures

Best for:
- read federation and controlled replication where liveness is externalized.

---

### C) Capability + state + liveness lease
Fields:
- all of B
- `leaseTtl`
- `renewToken`
- `failbackTarget`

Pros:
- strongest operational safety
- explicit ownership window for live coordination
- enables clean Unix handover/failback choreography

Cons:
- most implementation complexity

Best for:
- production sidecar/master orchestration and fault-tolerant upgrades.

## Recommendation for VersionOne
Use **C** as the target handshake outcome model for production behavior.

Implementation can still stage rollout:
1. parse/validate A fields,
2. enforce B compatibility checks,
3. activate C lease/failback enforcement.

This preserves incremental delivery while keeping the final contract aligned with your architecture (multi-DB, parallel development, isolated concerns, and controlled failback).
