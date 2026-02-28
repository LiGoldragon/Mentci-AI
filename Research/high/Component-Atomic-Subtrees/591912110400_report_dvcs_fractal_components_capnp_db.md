# Research Report: DVCS Component Repositories & Cap'n Proto Database Integration

## 1. Vision: A Fractal Repository Architecture
The goal is to improve the "Components" aspect of Mentci-AI by moving from a monorepo structure to a highly distributed, fractal architecture. In this model, the project splits into thousands of independent `jj` (Jujutsu) repositories. 

*   **Component Repositories:** Each component becomes its own `jj` repository.
*   **Contract Repositories:** The communication channels between any two (or more) components are isolated into their own repositories. These contract repositories store exclusively Cap'n Proto schemas (`.capnp`), EDN data, and eventually Aski specifications.
*   **Central Management:** A central component manages this web of repositories, utilizing Cap'n Proto/EDN as the source of truth for the entire dependency and contract graph.

## 2. Cap'n Proto Database Research
The prompt asks whether Cap'n Proto has a "sister" project database that uses its format to store, query, and edit large numbers of objects natively.

### Current Ecosystem Status
Based on deep web research and the official Cap'n Proto documentation:
*   **There is no official, completed "sister" database project.**
*   **The Roadmap:** The official Cap'n Proto roadmap explicitly lists a "Database" as a future milestone: *"A fast storage database based on Cap’n Proto which implements the ORM interface on top of the mmap storage format."* Kenton Varda notes these are "very large projects."
*   **In-Memory & Mmap Synergy:** Cap'n Proto is inherently designed to act like a database engine's storage layer. Because its wire format is identical to its in-memory representation, you can write Cap'n Proto objects directly to an `mmap()` (memory-mapped) file and query them instantly without a serialization step.
*   **Current State of the Art:** Organizations currently building databases on Cap'n Proto (like FoundationDB proxies or custom Cloudflare Worker layers) typically wrap Cap'n Proto serialization around an existing key-value store (like LMDB, RocksDB, or SQLite) or build custom `mmap` data structures.

## 3. Implication for Mentci-AI
Since a mature, off-the-shelf Cap'n Proto native database does not exist, Mentci-AI must construct its own "Contract Database."

**Proposed Architecture:**
1.  **Contract Repos as Tables:** Each `jj` contract repository acts as a highly-versioned "table" of schemas.
2.  **Shadow Indexing:** We use the `logical_fs.db` (SQLite shadow index) to map the thousands of `jj` repos. SQLite stores the metadata (which repo holds which Cap'n Proto contract).
3.  **Memory-Mapped Queries:** When the agent needs to query a specific contract, it memory-maps the pre-compiled `.bin` Cap'n Proto file, achieving the zero-copy performance that an official Capnp database would offer.

## 4. Updates to Sema Programmer Skill
This structural shift changes what it means to be a "Sema Programmer." The skill must be updated to enforce component repository isolation and the new contract repository pattern.
