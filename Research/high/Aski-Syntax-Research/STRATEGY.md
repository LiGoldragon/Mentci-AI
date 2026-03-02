# Strategy: Aski Syntax Reader

## 1. Objective
Design a sophisticated Rust-based reader for the "Aski" syntax—a Clojure/EDN-inspired structured data format that utilizes all delimiters and symbol-prefix rules for maximum type expressiveness.

## 2. Core Philosophy
*   **Maximum Delimiter Utilization:** Different delimiters map to core domain types (e.g., in a filesystem spec, `()` could mean a file, `[]` could mean a directory). This avoids verbose attributes like `:type file`.
*   **Top-Level Symbol Triggers:** The reader operates left-to-right. A keyword preceded by whitespace (e.g., ` impl `) triggers a specific parsing macro or node-count expectation, removing the need for enclosing parentheses for highly standardized structures (like Rust `impl` blocks).
*   **The Most Beautiful Sparse Notation:** The central theme of Aski is to represent logic with maximum density and specialization using the sparse notation that ASCII allows.

## 3. Implementation Plan
1.  **Phase 1: The Lojix Core (Extended EDN)**
    *   Initialize a Rust workspace crate: `Components/aski-lib`.
    *   Build a foundational lexer capable of parsing basic EDN structures (lists, vectors, maps, sets, strings, numbers, symbols, keywords).
2.  **Phase 2: Contextual Delimiter Mapping**
    *   Implement an AST that holds "Context" state.
    *   When the context is `Filesystem`, `{}` implies a directory map, eliminating explicit type labels.
3.  **Phase 3: Macro Triggers (Parenthesis Reduction)**
    *   Implement "Reader Macros" triggered by specific naked symbols (e.g., `impl`, `fn`, `trait`).
    *   When the lexer hits these symbols, it consumes the exact number of subsequent forms required by that structural block without requiring them to be wrapped in an outer list.
4.  **Phase 4: Two-Way Formatter**
    *   Build the `mentci-fmt` binary.
    *   Mode 1 (`--minify`): Strips all non-semantic whitespace and outputs the densest possible representation for LLM context.
    *   Mode 2 (`--beautify`): Expands the AST into a visually parsed, human-readable format for VCS diffs.
