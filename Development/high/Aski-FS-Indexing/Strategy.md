# Strategy: Aski-FS Indexing

## Objective
Establish a per-directory `index.edn` system that uses localized paths, serving as the foundation for Aski-FS.

## Roadmap
1. [ ] **Phase 1: Component Scaffolding**
    - Create `Components/mentci-fs`.
    - Setup Cargo dependencies (`edn-rs`, `serde`, `anyhow`).
2. [ ] **Phase 2: EDN to Value Mapping**
    - Implement a `Value` enum for structured data.
    - Implement a reader that loads `index.edn` into `Value`.
3. [ ] **Phase 3: Path Resolution**
    - Implement logic to resolve entries relative to the `index.edn` location.
    - Add a `Scanner` actor/object to crawl the repository.
4. [ ] **Phase 4: Agent Integration**
    - Provide a CLI interface for agents to query the indexed filesystem.

## Architectural Notes
- **Durability:** The `index.edn` files are `Stable Contracts`.
- **Ontology:** The filesystem is a tree of `Value` objects.
