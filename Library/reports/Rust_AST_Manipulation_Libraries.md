# Rust AST/CST Manipulation Libraries Research

As defined in `Core/HIGH_LEVEL_GOALS.md` (Goal 3), Mentci-AI agents must transition away from brittle text replacement (via Python or Regex) and utilize structural, programmatic code manipulation.

This document reviews native Rust libraries that can power the `mentci-mcp` code-editing component, allowing agents to manipulate Rust, EDN, and Cap'n Proto logic programmatically while preserving intent, comments, and spacing.

## 1. Concrete Syntax Trees vs Abstract Syntax Trees

For code rewriting and surgical modifications without losing formatting, an **Abstract Syntax Tree (AST)** is often insufficient because it strips "meaningless" tokens (whitespace, comments). A **Concrete Syntax Tree (CST)** or "Full-Fidelity" tree is required, which maps every single byte of the source file to a token.

## 2. Rust Candidates

### A. `tree-sitter` / `tree-sitter-rust`
**Overview:** A widely-used, multi-language parser generator with robust Rust bindings.
**Pros:** 
- Supports multiple languages instantly (Rust, Cap'n Proto, Clojure/EDN, Nix).
- High performance, incremental parsing.
- Query language allows agents to search for specific nodes (e.g., `(function_item name: (identifier) @name)`).
**Cons:** 
- While parsing is excellent, *modifying* a source file with tree-sitter requires manually splicing the original string buffer based on node byte-ranges. There isn't a high-level "AST builder" natively included.

### B. `ra_ap_syntax` (Rust Analyzer's Syntax Crate)
**Overview:** The actual syntax library powering `rust-analyzer`.
**Pros:**
- True, lossless Concrete Syntax Tree (built on `rowan`).
- Designed explicitly for IDEs to do refactorings and source modifications.
- Preserves 100% of formatting, macros, and comments.
**Cons:**
- Specific to Rust.
- Unstable API (tied to rust-analyzer releases), though perfectly adequate for local daemons.

### C. `syn` + `quote`
**Overview:** The standard macro parsing library for Rust.
**Pros:**
- Extremely well documented, standard for Rust manipulation.
**Cons:**
- It is an AST, not a CST. If you parse a file with `syn` and print it back out using `prettyplease`, all custom formatting and most comments are completely wiped out. This is a dealbreaker for surgical surgical file edits.

### D. `ast-grep` (`sg`) bindings
**Overview:** A structural search-and-replace tool written in Rust, built on top of tree-sitter.
**Pros:**
- Built specifically for the use-case we want (find structure X, replace with structure Y).
- Has Rust library crates (`ast-grep-core`, `ast-grep-language`).
**Cons:**
- May be heavyweight depending on how thin we want the Mentci daemon to be.

## 3. Recommendation for `mentci-mcp`

For an agentic workflow aiming for Level 5 autonomy, **`tree-sitter` (via `tree-sitter-rust`, `tree-sitter-clojure` / EDN) or `ast-grep-core`** provides the best multi-language foundation. 

If we choose to specialize *only* on Rust files first, **`ra_ap_syntax`** is the premier choice for lossless modifications, but **`ast-grep-core`** brings the immediate benefit of a pattern-matching engine that an LLM can easily output JSON for (e.g. `{"pattern": "impl MentciAid { $$$ }", "rewrite": "..."}`).

## Next Actions
- We have scaffolded `Components/mentci-mcp/` as a workspace.
- `mentci-mcp-core` will house the common logic (likely the `tree-sitter` or `ast-grep` evaluation engine).
- `mentci-mcp-jj` and `mentci-mcp-capnp` will use this core to edit Jujutsu and Capnp targets cleanly.
