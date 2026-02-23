# Research Artifact: Aski-FS Indexing Test Implementation

- **Solar:** ♓︎ 6° 1' 50" | 5919 AM
- **Subject:** `Aski-FS-Indexing`
- **Title:** `aski-fs-indexing-test-implementation`
- **Status:** `finalized`

## 1. Intent
Validate the feasibility of a Rust-based localized indexing system for the Mentci-AI filesystem using EDN.

## 2. Findings
*   **EDN Parsing:** The `edn-rs` library is functional for basic EDN parsing, although its API (using `BTreeMap` and custom `Vector` wrappers) requires careful handling of ownership and types.
*   **Localized Paths:** Treating the `index.edn` location as the root for its entries significantly reduces path redundancy and makes subject directories self-contained.
*   **Conciseness:** A simple scan of a high-priority topic directory now returns a clean list of artifacts without repeating the entire repository prefix.

## 3. Implementation: `mentci-fs`
A new Rust library and CLI tool have been established in `Components/mentci-fs/`.

### 3.1 Components
*   **`IndexReader`**: Parses `index.edn` maps into a `FsIndex` struct.
*   **`FsScanner`**: Resolves relative entries into absolute or repository-relative paths based on the index location.
*   **CLI Tool**: `mentci-fs <dir>` allows for quick inspection of indexed components.

## 4. Conclusion
The per-directory indexing system is a robust prototype for Aski-FS. It provides the "Universal Structured Data" format needed for agents to grasp the filesystem concisely.

## 5. Next Steps
*   Roll out localized paths to all existing `index.edn` files.
*   Integrate `mentci-fs` into the core agent logic for subject discovery.
