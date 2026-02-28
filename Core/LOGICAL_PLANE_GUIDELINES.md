# Logical Plane Guidelines

This document defines the operating protocols for the **Logical Plane**—the symbolic layer of Mentci-AI where code is treated as structured data rather than raw text.

## 1. Logical Reading (Visibility)

**Rule:** Never assume the content of a large file based on a simple `grep`.
**Protocol:**
1.  Use `logical_get_ast` to understand the hierarchical skeleton of a file.
2.  Use `logical_run_query` with Tree-sitter S-expressions to find specific patterns (e.g., "Find all functions that use the `tractor` framework").
3.  **Context Density:** Favor high-density queries over dumping large file bodies into context.

## 2. Logical Editing (Mutation)

**Rule:** Structural changes must be AST-aware.
**Protocol:**
1.  Prefer `structural_edit` over standard `edit` for code transformations.
2.  **Atomicity:** Each structural edit must be a single intent commit.
3.  **Fallback:** If AST-matching fails (e.g., due to unexpanded macros), document the limitation in a research report and fallback to a precise text `edit`.

## 3. Logical File Search (LFS)

**Rule:** Repository discovery is architecture-aware.
**Protocol (Nascent):**
1.  Discovery uses the **Shadow SQLite Index** (`.mentci/logical_fs.db`).
2.  Queries must filter by `semantic_type` (logic, schema, data, research, library) and `component`.
3.  **Fast Response:** LFS tools provide instant metadata without walking the entire disk.

## 4. Self-Debugging (The Mirror)

**Rule:** Verify the machine's output before claiming success.
**Protocol:**
1.  The agent must "see" the rendered TUI state via the **Mirror Hook**.
2.  **Verification:** Read `.mentci/ui_mirror.txt` after any UI-impacting tool call (e.g., formatting updates).
3.  **Impeccability:** If the mirror shows rendering exceptions or malformed spans, the agent must correct its code immediately.

## 5. Intelligence Portability

**Rule:** The logic of the agent must be harness-independent.
**Protocol:**
1.  The Pi API is treated as a fixed Cap'n Proto contract.
2.  Harness-specific proxy wrappers (e.g., `mentci-wrale.ts`) should remain "dumb," delegating all complex logic to the Rust component (`mentci-mcp`).
3.  This ensures the same logical power is available in `pi`, `vtcode`, and future harnesses.
