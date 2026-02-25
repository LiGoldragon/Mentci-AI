# Research Artifact: Structural Editing for AI Agents

- Chronography: `12.8.20.00 | 5919 AM`
- Subject: `Structural-Editing`
- Title: `structural-editing-agent-ecosystem`
- Status: `active`

## 1. Executive Summary

Traditional AI coding agents rely on string manipulation or regex-based patching, which is brittle and context-blind. The next generation of tools leverages Abstract Syntax Trees (AST) and Concrete Syntax Trees (CST/Tree-sitter) to perform semantic edits. This research evaluates the tools mentioned in the Gemini Pro report and identifies the most suitable candidates for Mentci-AI's `structural_edit` component.

## 2. Tool Analysis

### 2.1. VT Code & ast-grep (sg)
- **VT Code** is a Rust-based terminal agent that uses **ast-grep** for semantic edits.
- **ast-grep** is a powerful engine for structural search and replace. It uses Tree-sitter to parse code into CSTs, ensuring that edits are lossless (preserving whitespace and comments).
- **Suitability:** High. `ast-grep` provides exactly the functionality needed to replace "adhoc python scripts" with precise, structural transformations.

### 2.2. Model Context Protocol (MCP) for Structural Editing
- Ecosystems like Lisp and Clojure (which inspire Aski) have robust structural editing support (`clojure-mcp`).
- **Suitability:** High. MCP provides the standard interface for Mentci-AI to expose structural tools to the `pi` agent.

### 2.3. Rigour, Difftastic, and Aider
- **Rigour:** Acts as a quality gate by extracting facts from ASTs. Useful for post-edit validation.
- **Difftastic:** A syntax-aware diffing tool. Excellent for feeding meaningful change summaries back to the LLM.
- **Aider:** Uses Tree-sitter for repository mapping. Mentci-AI should adopt similar repository-mapping techniques for its `explore` subagent.

## 3. Recommended Approach for Mentci-AI

1.  **Engine:** Utilize `ast-grep` (lib version if possible, or CLI wrapper) as the primary engine for `structural_edit`.
2.  **Interface:** Implement a native Rust MCP server (`mentci-mcp`) that exposes `structural_edit`.
3.  **Data Policy:** Strictly follow the Logic-Data Separation mandate. Configuration of the search-and-replace patterns must be passed via Cap'n Proto messages.
4.  **CST Priority:** Always prefer CST over AST to ensure the codebase's formatting is respected.

## 4. Suggestions for the Author

1.  **Lojix Native Support:** As we develop the Aski/Lojix parser, we should ensure it is compatible with `ast-grep`'s pattern matching logic or implement a similar CST-query system for it.
2.  **Difftastic Integration:** We should consider using `difftastic` in the `reviewer` subagent flow to provide more semantic diffs instead of line-based diffs.
3.  **Rigour-style Validation:** After a `structural_edit` is applied, we should run a validation pass (like `Rigour`) to ensure the code still compiles and follows project invariants.

## 5. Questions for the Author

1.  **Rule Enforcement:** Should the `structural_edit` tool strictly enforce the "Methods on Objects" rule by refusing to apply edits that create free functions?
2.  **Pattern Library:** Should we establish a library of "Standard Patterns" (stored in EDN/Aski) that the agent can refer to by name instead of writing raw `ast-grep` patterns?
