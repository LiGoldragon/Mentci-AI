# Research Report: Modern CozoDB Rust Integration (2024-2025)

## 1. Executive Summary
CozoDB has evolved from a specialized graph database into an **Embedded Relational-Graph-Vector Substrate**, positioning itself as a "hippocampus for AI." For Mentci-AI, it provides the ideal engine for the **World Ontology**.

## 2. Best Modern Patterns
### A. Embedded Execution
The idiomatic approach in Rust is using the `cozo` crate in embedded mode. This provides direct, synchronous access to the Datalog engine without network overhead.
- **Persistence:** Use the `rocksdb` storage engine for durable world states.
- **Ephemeral:** Use the `mem` engine for sub-mentci-box task states.

### B. Declarative Mutations (CozoScript)
Modern Cozo usage favors **CozoScript** for all operations. This allows for complex relational and graph logic to be expressed in single atomic blocks.
- **Atomic Facts:** Use `::put` and `::rm` for bitemporal fact management.
- **Fixed-Point Iteration:** Leverages optimized recursive Datalog for resolving complex logical dependencies.

### C. Vector Integration (HNSW)
Cozo now includes a native Rust implementation of HNSW indices. This allows for:
- Storing semantic embeddings directly alongside relational facts.
- Hybrid Datalog queries that combine logical constraints with semantic similarity.

## 3. Implementation Strategies
1. **The World Actor:** Wrap `DbInstance` in a Rust object.
2. **Type Safety:** Implement JSON-based or trait-based bridges to overcome the `DataValue` conversion hurdle.
3. **Time-Travel:** Utilize Cozo's built-in MVCC for historical auditability.

## 4. References
- [CozoDB Official Documentation (v0.7)](https://docs.cozodb.org/)
- [CozoDB GitHub Repository](https://github.com/cozodb/cozo)
- [Rust Crates.io: cozo](https://crates.io/crates/cozo)

---
*Verified via Linkup Research Session*
*Solar: 2026.03.02.00.32*
