# wrale Live Survey Results (Pi-native extension tools)

## Scope
Validate that wrale tools are callable from the Pi extension surface and run a quick project survey.

## Environment
- Registered project: `mentci`
- Tool path: Pi wrappers (`wrale_register_project`, `wrale_get_ast`, `wrale_run_query`)

## Results

### 1) Project registration works
`wrale_register_project(path: ".", name: "mentci")` returned language inventory and root path.

Observed major language counts:
- rust: 170
- typescript: 274
- markdown: 795
- go: 140
- c: 61
- python: 18

### 2) AST retrieval works
`wrale_get_ast(project: "mentci", path: "Components/mentci-user/src/main.rs", maxDepth: 1)` returned valid Rust syntax tree.

Quick signal from AST root children:
- multiple `function_item` nodes in `mentci-user/src/main.rs`
- one nested `mod_item` test module

### 3) Query execution has a runtime issue
`wrale_run_query` failed with:
- `Error querying ...: 'tree_sitter.Query' object has no attribute 'captures'`

Interpretation:
- wrale server is reachable and functional for project registration and AST traversal.
- query path appears broken in the installed wrale runtime version/environment.

## Immediate implication
For Mentci Phase-1 adoption:
- Use wrale now for project registration + AST survey tasks.
- Treat `run_query` as unstable until wrale runtime/version issue is resolved.
- Keep local deterministic analysis/apply path as primary for production operations.
