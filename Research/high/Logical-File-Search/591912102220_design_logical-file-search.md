# Design Strategy: Logical File Search (LFS) Indexer

## 1. Goal
Standardize file discovery in Mentci-AI by moving from raw shell-based globbing (`find`/`ls`) to a semantic, queryable index. This achieves "Logical File Search" where the agent can filter by component, intent, and file type instantly.

## 2. Architecture: The SQLite Shadow
We will maintain a "Shadow Index" of the repository in a local SQLite database: `.mentci/logical_fs.db`.

### Schema Design
```sql
CREATE TABLE files (
  path TEXT PRIMARY KEY,       -- Relative path from repo root
  name TEXT NOT NULL,           -- Filename (e.g. main.rs)
  extension TEXT,               -- File extension (rs, capnp, edn)
  component TEXT,               -- The component this belongs to (e.g. mentci-user)
  semantic_type TEXT,           -- 'logic', 'data', 'schema', 'research', 'library'
  last_indexed INTEGER          -- Unix timestamp
);

CREATE INDEX idx_component ON files(component);
CREATE INDEX idx_type ON files(semantic_type);
```

## 3. Implementation Abstraction Points

### A. The Indexer (`logical_index_repo`)
*   **Component:** `mentci-policy-engine` (Rust)
*   **Logic:** Walks the filesystem, respects `.gitignore`, and **classifies** files based on our `Library/specs/AskiFsSpec.md`.
*   **Classification Rules:**
    *   `Components/*/src/**.rs` -> `logic`
    *   `Components/*/schema/**.capnp` -> `schema`
    *   `Library/**.md` -> `library`
    *   `Research/**.md` -> `research`
    *   `**.edn` -> `data`

### B. The Search Tool (`logical_find_files`)
*   **Interface:** MCP Tool in the `logical-edit` plane.
*   **Parameters:** `type`, `component`, `extension`, `pattern`.
*   **Execution:** Runs a prepared SQL statement against the SQLite shadow index.
*   **Fallback:** If the database doesn't exist or is empty, it auto-triggers a one-off index run.

### C. The Watcher (Optional Sync)
*   **Component:** `mentci-aid` or a `jj` post-commit hook.
*   **Action:** Triggers an incremental update to the SQLite DB whenever files change, ensuring the "fast response" requirement.

## 4. Why This is "Logical"
Unlike `find`, which only knows about strings, `logical_find_files` knows about **Architecture**.
*   Query: "Show me all schema files for the STT component."
*   Internal SQL: `SELECT path FROM files WHERE component='mentci-stt' AND semantic_type='schema';`

## 5. Next Steps
1.  **Requirement:** User to run `nix develop` (to pick up the new `sqlite` package).
2.  **Scaffold:** Create `Components/mentci-policy-engine/src/lfs.rs` for the indexing logic.
3.  **Integrate:** Add `logical_index_repo` and `logical_find_files` to the `mentci-logical-edit.ts` Pi extension.

**Shall I proceed with implementing the Rust indexer logic?**
