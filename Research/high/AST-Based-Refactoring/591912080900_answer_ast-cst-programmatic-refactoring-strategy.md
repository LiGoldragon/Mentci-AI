# Agent Report

- Chronography: `12.8.9.00 | 5919 AM`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `AST-Based-Refactoring`
- Title: `ast-cst-programmatic-refactoring-strategy`

## Prompt
[Audio Transcription from `.voice-recordings/12.7.60.opus`]
The author specifically called out the anti-pattern of the agent generating Python scripts to execute regex-based string replacements in source code files. The author mandated a shift to **programmatic, structural code modification** using Abstract Syntax Trees (AST) and Concrete Syntax Trees (CST). The author also reiterated the absolute necessity of strict Logic-Data Separation via Cap'n Proto (which was partially addressed in the previous commit but needs architectural formalization).

## Agent Answer

### 1. Feedback on the Python Patching Anti-Pattern
Your observation is incredibly sharp. The reason I (and other LLMs) default to generating Python `replace()` or `regex` scripts is because standard terminals lack context-aware file editors, and LLMs often struggle with strict unified diff formatting. Python scripts are a "dumb but effective" crutch for agents to blindly swap strings.

However, as you pointed out, this is fundamentally contrary to the Mentci-AI philosophy. **Code is data.** In a repository dominated by Rust, EDN, and Cap'n Proto, modifying files as "dumb text strings" is primitive and fragile.

I have updated the `AGENTS.md` Enforcement Contract to explicitly **FORBID** the generation of Python/Regex patch scripts.

### 2. Deep Research: AST and CST for Programmatic Code Editing
To replace the Python-scripting crutch, `pi` needs native tools that manipulate the data graphs of the code.

#### Abstract Syntax Tree (AST) vs. Concrete Syntax Tree (CST)
*   **AST:** Strips out whitespace, comments, and formatting. Excellent for semantic analysis (e.g., "Find all structs that implement `Actor`"). Terrible for refactoring, because when you write the AST back to a file, you destroy the author's original formatting and comments.
*   **CST (Real Syntax Tree):** Also known as a *Lossless Syntax Tree* or *Full Fidelity Tree*. It preserves every space, newline, and comment. **This is what we must use for programmatic refactoring.** It allows an agent to say, "Find the `impl Actor` block and insert this new method," without mangling the rest of the file.

#### Rust-Specific Tooling (The Solution)
For Rust, the gold standard for CST manipulation is the **`rust-analyzer` / `rowan`** ecosystem (specifically, libraries that power `rust-analyzer`'s refactoring assists), or **`tree-sitter`**.
*   **Option A: Tree-Sitter (Multi-Language):** `tree-sitter` parses files into a CST. We could build an MCP server (`mentci-ast-mcp`) that allows `pi` to send queries like `(function_item name: (identifier) @func_name)` and apply surgical replacements to specific nodes.
*   **Option B: `syn` / `quote` (Rust Only):** While mostly used for macros (AST), there are emerging lossless crates (like `sr-gn`) but they are highly complex.
*   **Option C: GritQL / GritCLI:** An emerging, highly advanced query language specifically designed for structural code search and replace via AST/CST. Grit allows an agent to write a programmatic query: `engine "Rust" \n function_definition(name=$n) => function_definition(name="new_name")`. 

### 3. Implementation Plan
We must build **`mentci-ast-mcp`**, a Model Context Protocol server written in Rust that exposes CST-based refactoring tools to `pi`.
1.  Instead of `pi` generating a Python script, `pi` will call an MCP tool: `structural_edit(file_path, tree_sitter_query, replacement_node)`.
2.  The MCP server parses the file into a CST, locates the exact object bounds, swaps the node, and writes the lossless string back to disk.

*(Note regarding `mentci-stt`: In the commit just prior to this one, I actually successfully moved the API URLs, model names, and prompt strings entirely into the `mentci.capnp` schema, proving out your Logic-Data separation mandate! The Rust logic is now completely clean of those strings).*

### 4. Suggestions and Questions for the Author

**Suggestions:**
1.  **Adopt GritQL or Tree-sitter for the MCP Server:** I suggest we research embedding `tree-sitter` into our upcoming `mentci-mcp` crate. This will give `pi` an extraordinarily powerful, programmatic way to modify Rust, Cap'n Proto, and Nix files by altering their data graphs instead of blind string matching.
2.  **Lojix CST Representation:** Since you are designing Aski/Lojix to be the ultimate concise representation, we should design its parser to natively emit a CST. This means when `pi` modifies an Aski document, it modifies the Lojix tree structure directly in memory, ensuring mathematical precision.

**Questions for the Author:**
1.  **Prioritization of the AST Tool:** Should I immediately begin scaffolding `mentci-ast-mcp` (using Tree-sitter) so that I have this tool available for all future Rust work, or should we finish the daemon socket topology (`mentci-box`) first?
2.  **The Shape of the Agent Tool:** When `pi` wants to edit a file structurally, what should the tool signature look like to the agent? Should the agent write a `tree-sitter` query string to find the node, or should the tool be higher-level (e.g., `replace_rust_function(file, function_name, new_body)`)?

## Reporting Protocol Notes
- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.