# Research Artifact: Aski-FS Per-Directory Indexing

- **Solar:** ♓︎ 6° 1' 43" | 5919 AM
- **Subject:** `Aski-FS-Indexing`
- **Title:** `aski-fs-per-directory-indexing`
- **Status:** `proposed`

## 1. Intent
Research and implement a per-directory `index.edn` system as a prototype for the Aski-FS filesystem. The system must use relative paths (location of `index.edn` is the root) to improve efficiency and maintainability.

## 2. Current State
Existing `index.edn` files (e.g., in `Research/high/Subject/index.edn`) often use paths relative to the repository root. This creates redundancy and makes it harder to move or rename subject directories.

## 3. Proposed Design: Localized Indexing
The new system treats the directory containing `index.edn` as the implicit root for all paths mentioned within it.

### 3.1 Syntax (EDN)
```edn
{:kind :directory-index
 :title "Subject Topic"
 :entries ["report_1.md" "report_2.md" "sub-dir/"]}
```

### 3.2 Advantages
*   **Efficiency:** No need to parse and reconstruct full repository paths for every entry.
*   **Portability:** Directories can be moved without updating the `index.edn` entries.
*   **Conciseness:** Reduces token usage in agent context.

## 4. Rust Implementation: `mentci-fs`
A new Rust component `mentci-fs` will be implemented to parse these indices and provide a unified view of the filesystem to the agent.

### 4.1 Core Objects
*   **`FsIndex`**: Representation of a single `index.edn`.
*   **`FsScanner`**: Traverses the filesystem, discovering and merging `index.edn` files.
*   **`AskiValue`**: Simple EDN value enum for flexible data handling (using `edn-rs` or custom parser).

## 5. EDN Reading in Rust
The project already includes `edn-rs`. Research will focus on whether it supports the "Universal Structured Data" format (Value enum) or if a custom de/serializer is needed for maximum simplicity and reliability.

## 6. Next Steps
1.  Scaffold `Components/mentci-fs`.
2.  Implement a simple EDN-to-Value parser.
3.  Add tests for relative path resolution.
