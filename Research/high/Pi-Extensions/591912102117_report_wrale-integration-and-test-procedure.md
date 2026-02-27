# wrale Tree-sitter MCP Integration in Mentci + Test Procedure

## Goal
Add `wrale/mcp-server-tree-sitter` into Mentci environment and verify practical usage for analysis/blast-radius intelligence.

## What was added
- Added project MCPorter config:
  - `config/mcporter.json`
- Registered server name: `wrale-tree-sitter`
- Command points to local venv python module runner:
  - `/home/li/git/Mentci-AI/.venv-wrale/bin/python -m mcp_server_tree_sitter.server`
- Set lifecycle:
  - `"lifecycle": "keep-alive"`

Why keep-alive matters:
- wrale stores project registrations in server memory.
- Without daemon/keep-alive, each `mcporter call` spawns a new process and project state disappears.

## Installation used for test
```bash
python -m venv .venv-wrale
.venv-wrale/bin/pip install mcp-server-tree-sitter
```

## Verification performed

### 1) Tool discovery works
```bash
npx -y mcporter list wrale-tree-sitter --all-parameters
```
Observed tools include:
- `register_project_tool`
- `list_projects_tool`
- `get_ast`
- `run_query`
- `get_symbols`
- `analyze_project`
- `find_text`

### 2) Stateful session works with daemon
```bash
npx -y mcporter daemon start
npx -y mcporter daemon status
```

### 3) Register project
```bash
npx -y mcporter call 'wrale-tree-sitter.register_project_tool(path: ".", name: "mentci")'
```
Result returned language inventory and root path for `/home/li/git/Mentci-AI`.

### 4) AST query works
```bash
npx -y mcporter call 'wrale-tree-sitter.get_ast(project: "mentci", path: "Components/mentci-user/src/main.rs", max_depth: 1, include_text: false)'
```
Result returned valid Rust AST with root `source_file` node and child declarations.

### 5) Shutdown
```bash
npx -y mcporter daemon stop
```

## Cap'n Proto status in wrale
- Cap'n Proto tree-sitter grammar exists in ecosystem (`tree-sitter-capnp`).
- Current wrale language surfacing does not clearly advertise capnp support in FEATURES/README language tables.
- Treat capnp in wrale as **unknown/unverified** until explicit language check passes in your runtime.

Suggested probe:
```bash
npx -y mcporter call 'wrale-tree-sitter.check_language_available(language: "capnp")'
```

## Recommended usage in Mentci (Phase 1)
- Use wrale for:
  - repository analysis,
  - symbol/query traversal,
  - blast-radius intelligence.
- Keep local `mentci-mcp-edit` as deterministic apply path (especially for capnp + policy-coupled workflows).

## Common failure modes
1. `Project 'X' not found` after register:
   - daemon not running OR server not configured as keep-alive.
2. `spawn ... ENOENT`:
   - command path in `config/mcporter.json` invalid/relative mismatch.
3. Slow first use:
   - language parser load/cache warm-up.
