# Strategy: AST/CST Based Programmatic Refactoring

## 1. Goal
Eliminate the severe anti-pattern of LLM agents generating ad-hoc Python/regex scripts to modify source code. Establish a programmatic, structured capability for the agent to modify files by manipulating their Concrete Syntax Trees (CSTs) or Abstract Syntax Trees (ASTs).

## 2. Rationale
Mentci-AI operates on the principle that *Code is Data*. Modifying Rust, Cap'n Proto, or EDN files by treating them as dumb text strings via regular expressions leads to fragile, context-blind errors. The agent possesses the intent and knows exactly *what* structural object it wants to change. We must provide tools that allow it to operate directly on the data graph of the code.

## 3. Architecture: `mentci-ast-mcp`
We will implement a Model Context Protocol (MCP) server that exposes structural editing tools to the `pi` agent.

### Core Technologies
*   **Tree-sitter:** The engine for generating Concrete Syntax Trees (CSTs). Unlike pure ASTs, CSTs are lossless (they preserve whitespace and comments), allowing us to write the modified graph back to a file without destroying the author's formatting.
*   **GritQL (Optional/Future):** A structural search-and-replace query language that the agent can use to cleanly define graph transformations.

### Agent Workflow
1.  The agent decides to modify a Rust implementation block.
2.  Instead of generating `patch.py`, the agent calls the MCP tool `structural_edit`.
3.  The agent passes the target file, a structural query (e.g., "Find the `impl Actor` block for `SessionActor`"), and the replacement code graph.
4.  The Rust MCP server executes the modification with mathematical precision on the CST.

## 4. Implementation Phasing
1.  **Phase 1: Prohibition.** Update `AGENTS.md` to strictly forbid python/regex file patching.
2.  **Phase 2: MCP Scaffolding.** Build the foundational `mentci-mcp` Rust server to establish the JSON-RPC pipeline to `pi`.
3.  **Phase 3: Tree-Sitter Integration.** Embed `tree-sitter-rust` into the MCP server and expose basic structural replace tools.
4.  **Phase 4: Lojix Native CST.** Ensure that the future Aski/Lojix parser natively emits a CST, allowing the agent to manipulate the system's core knowledge graphs with zero data loss.