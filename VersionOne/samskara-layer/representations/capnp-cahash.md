# Binary Projection — Cap'n Proto + Content Address

## Purpose
Provide deterministic execution and transport encoding for protocol objects.

## Characteristics
- schema-validated object boundaries,
- efficient binary transport,
- deterministic replay of governance/execution traces,
- robust cross-component interchange.

## Content Addressing
Each execution-relevant message carries or derives a content hash reference for:
- tamper-evident lineage,
- deduplication,
- exact replay and audit reconstruction.

## Typical Use
- `AgentWorkspaceInit` transport,
- lane mutation request execution,
- execution result and audit bundle emission,
- inter-component protocol exchange.
