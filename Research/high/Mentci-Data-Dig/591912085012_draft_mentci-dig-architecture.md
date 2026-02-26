# Mentci-Dig: Structural Data Extraction and Mining Component

- **Solar:** 5919.12.8.50.12
- **Subject:** `Mentci-Data-Dig`
- **Status:** `design-draft`
- **Weight:** `High`

## 1. Intent
To replace ad-hoc data-mining scripts (like Python scripts used to grok large JSON chat exports, or grep/awk pipelines for codebase crawling) with a robust, "Sema-grade" Rust component. This component will maximize modern parsing technologies (like Tree-sitter, `ast-grep`, and structured JSON/EDN parsers) to dig through data files efficiently and effectively.

## 2. Motivation
During research, agents frequently resort to ad-hoc Python scripts to extract specific information from large datasets (e.g., ChatGPT exports). While effective in the short term, this violates the *Local Machine of Fit* and *Sema Rust Guidelines* by proliferating untyped, disposable, non-performant logic. We need a native Rust tool designed for complex data extraction.

## 3. Core Capabilities

### 3.1 Structured Data Mining
*   **Formats:** JSON, EDN, Lojix, YAML.
*   **Queries:** Support for structural query paths (e.g., JSONPath, JMESPath) but integrated into the Mentci Cap'n Proto / Sema object pipeline.
*   **Large File Handling:** Stream-based or memory-mapped parsing to handle massive exports (like 140MB+ `conversations.json` files) without OOM panics.

### 3.2 Semantic Code Search (AST-Based)
*   **Technologies:** Tree-sitter, `ast-grep` core.
*   **Queries:** Find all instances of a pattern (e.g., "every struct that implements trait X") without relying on regex.

### 3.3 Natural Language / Text Mining
*   **Features:** Regex support for plain text where necessary, but heavily optimized.
*   **Context Windows:** Extracting exact context windows around keywords, summarizing token usage.

## 4. Architecture (Sema-Grade)

### 4.1 Single Sema Object In / Out
*   **Input (Cap'n Proto):** A `DigRequest` containing:
    *   `targets`: List of file paths or globs.
    *   `strategy`: `JsonPath`, `AstPattern`, `Regex`, etc.
    *   `query`: The query string.
    *   `filters`: Date ranges, file extensions, max results.
*   **Output (Cap'n Proto):** A `DigResponse` containing:
    *   `matches`: List of `Match` objects (file, line, byte offset, structured extracted data).
    *   `metadata`: Execution time, files skipped, parsing errors.

### 4.2 Crates & Modules
*   `mentci-dig/core`: The pure Rust domain logic for extraction.
*   `mentci-dig/capnp`: Wire format traits.
*   `mentci-dig/bin`: The CLI interface acting as a frontend for the MCP tool or Jail execution.

## 5. Next Steps
1.  Define the `DigRequest` and `DigResponse` in `Components/schema/mentci.capnp` (or a dedicated `mentci_dig.capnp`).
2.  Scaffold the `Components/mentci-dig` workspace member.
3.  Implement a basic `JsonPath` or streaming JSON extraction module as the first proof-of-concept, directly solving the ChatGPT export problem.
