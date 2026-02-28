# Session Synthesis: The Rise of the Logical Plane (v0.12.10.48.58)

## 1. Context and Mission
This session was a high-authority stabilization and evolution sprint centered on the **Logical Plane**. The primary mission was to move beyond text-based primitives into structure-aware (AST) development, establishing durable authority and high-fit tooling.

## 2. Key Technical Discoveries & Fixes

### A. The "Double Unwrap" Handoff
*   **Discovery:** Tool execution returned valid JSON to the LLM, but the Pi UI showed "No matches."
*   **Root Cause:** `mcporter` versions (specifically 0.7.3) return a JSON string nested inside a `content[0].text` property of a larger envelope.
*   **Fix:** Implemented recursive double-unwrapping in `.pi/extensions/mentci-logical-edit.ts`.

### B. Ephemeral Process Coherence
*   **Discovery:** Queries would fail with "Project not found" because registration and query happened in different subprocesses.
*   **Fix:** Overhauled the extension wrapper to execute `register_project_tool` immediately followed by the target tool in the **same fresh process** for every call.

### C. Tree-Sitter 0.25+ Compatibility
*   **Discovery:** `run_query` crashed because it called `query.captures()`, which was removed in `tree-sitter 0.25.x` in favor of `QueryCursor`.
*   **Fix:** Applied a local monkey-patch to the wrale venv (`tools/search.py`) to support both legacy and modern `QueryCursor` APIs.

### D. CPU Stability & Rendering
*   **Discovery:** Complex regex and filesystem mirroring caused an 82% CPU spike and UI unresponsiveness.
*   **Mitigation:** Stripped the highlighter regex in favor of a simpler, more stable greedy replacement pattern and optimized the mirror hook (`ui_mirror.txt`).

## 3. Durable Outcomes

### Protocols Established:
1.  **Logical Plane Guidelines (`Core/LOGICAL_PLANE_GUIDELINES.md`):** Canonical rules for Reading, Editing, Search, and Self-Debugging.
2.  **Web Validation Mandate (`Core/AGENTS.md`):** Routine Linkup checks required for all ecosystem status claims.
3.  **Mirror Hook:** A filesystem anchor (`.mentci/ui_mirror.txt`) to synchronize agent perception with the actual UI display.

### Tooling Stabilized:
*   `logical_run_query` (with EDN intent headers).
*   `logical_get_ast` (with hierarchical coordinates).
*   `mentci-user` (packed Capnp support).
*   `logical-reload` (Pi command).

### Research Pillars:
*   **LFS Design:** SQLite-backed semantic file indexing.
*   **Search Stack:** Roadmap for Linkup + Tavily + LFS.
*   **DVCS Split:** Design for thousands of `jj` repositories with mmap-based contract management.

## 4. Unfinished Business (For Next Session)
1.  **LFS Implementation:** Convert the SQLite indexer design into Rust logic in `mentci-policy-engine`.
2.  **Pi API Spec:** Implement the concrete `.capnp` schema for the extension bridge.
3.  **Highlighter Perfection:** Re-introduce IntelliJ-level keyword highlighting once the greedy regex is proven stable under high load.

*End of Session Synthesis.*
