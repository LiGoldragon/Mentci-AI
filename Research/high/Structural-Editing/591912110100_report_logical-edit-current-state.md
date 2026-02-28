# Report: Current State of Logical Editing & Intelligent Search

## 1. Overview
The "Logical Editing" plane has been established as the primary structural analysis layer for Mentci-AI. It moves beyond raw text-matching to Abstract Syntax Tree (AST) aware operations, providing agents with deep architectural visibility.

## 2. Component: `logical-edit`
- **Extension:** `.pi/extensions/mentci-logical-edit.ts`.
- **Backend:** `wrale/mcp-server-tree-sitter` (wrapped via `mcporter`).
- **Tools:**
  - `logical_run_query`: Executes Tree-sitter S-expression queries. Optimized for high-density match extraction.
  - `logical_get_ast`: Returns a hierarchical tree of a source file's syntax nodes.
  - `logical_register_project`: Idempotent registration of the workspace.
- **Display Status:**
  - Custom ANSI-highlighted UI (via `highlight.js` bridge).
  - Explicit **EDN Intent Headers** clearly show what the tool is doing.
  - **Mirror Hook:** Final UI output is written to `.mentci/ui_mirror.txt` so agents can "see" what the user sees, solving synchronization hallucinations.
- **Stability:** Ported to a process-coherent ephemeral model (registration + query in one process) to prevent state loss in pure Nix environments.
- **Envelope Protocol:** Discovered that `mcporter` returns JSON strings inside `content[0].text` of an outer envelope. Implemented recursive double-unwrapping in the extension `execute` layer to ensure valid JSON reaches the UI renderers and the LLM context.
- **Memory Management:** Purposely avoid long-lived persistent runtime handles in the extension host to prevent stale server state from masking fixes; fresh runtimes are created and closed per tool call.

## 3. Component: `linkup` (Search Intelligence)
- **Status:** Integrated and operational.
- **Usage:** Mandatory for ecosystem validation per `Core/AGENTS.md`.
- **Tools:** `linkup_web_search`, `linkup_web_answer`.

## 4. Phase 2 Goal: Logical File Search (LFS)
- **Vision:** Replace "dumb" shell search (`find`, `ls`, `grep`) with a semantic, queryable index.
- **Infrastructure:** SQLite-backed shadow index maintained in `.mentci/logical_fs.db`.
- **Proposed Tools:**
  - `logical_index_repo`: Semantic classification of all repo files (logic, data, schema, research).
  - `logical_find_files`: Instant SQL-backed filtering by component or semantic type.

## 5. Agent Operational Mandate
Agents are now instructed to:
1.  **Always** use `logical_run_query` before proposing large refactors.
2.  **Verify** their own UI rendering using `logical_debug_view` (mirror hook).
3.  **Liberally** search the web via Linkup for status/ecosystem claims.
