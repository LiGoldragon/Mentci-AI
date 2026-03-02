# CozoDB Usage Patterns & Suggestions (Mentci Context)

## Overview
CozoDB is our primary tool to specify the world, which is then emitted into a Cap'n Proto specification. It is a transactional, relational-graph-vector database using Datalog for queries.

## Obvious Usage Patterns

### 1. Defining the World Ontology
We use CozoDB's declarative `CozoScript` to define our facts. The `BTreeSet` inside `mentci-datalog` is being replaced by an embedded CozoDB instance.
- **Rules as Relations:** Use rules to establish relationships between components, importance ratings, and agent skills.
  ```datalog
  :create component_rating { component_name: String => rating: Int, description: String }
  ```

### 2. Emitting to Cap'n Proto
The single source of truth for the world lives in the CozoDB substrate. We run fixed Datalog queries to extract subsets of this world, which are then encoded directly into Cap'n Proto binaries for agent initialization and schema-validated inter-component messaging.

### 3. Component Ratings & Skills Database
Start tracking the repository's major components immediately.
- Define a table for components and their "importance rating".
- Define a table for agent skills, referencing their physical locations and dependencies.
- This allows our agents to query the database (e.g., "Which components depend on `mentci-stt`?") and pull the graph structure natively using CozoDB's recursive features.

### 4. Embedded Execution in Rust
- Run CozoDB embedded in `mentci-datalog` using the `rocksdb` backend for persistence and `mem` for ephemeral task states.
- Use `ractor` to wrap the `cozo::DbInstance` in a single-owner actor. This satisfies our Sema Object rules (Actor-First Concurrency).

### 5. Vector Search for Agent Context
Since CozoDB supports HNSW vector indexing, we will eventually store semantic embeddings of our markdown files (`Research/`, `Core/`) alongside relational metadata, allowing agents to run hybrid Datalog queries that combine hard relational logic with semantic similarity.