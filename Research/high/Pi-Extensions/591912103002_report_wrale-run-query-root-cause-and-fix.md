# wrale `run_query` failure: root cause and local fix

## Symptom
`wrale_run_query` returned:
- `Error querying ...: 'tree_sitter.Query' object has no attribute 'captures'`

## Root cause
Installed runtime combination:
- `mcp-server-tree-sitter==0.5.1`
- `tree-sitter==0.25.2`

In py-tree-sitter 0.25+, query execution moved to `QueryCursor.captures(...)`.
wrale 0.5.1 code path in `tools/search.py` still calls `query.captures(...)`.

## Evidence
- AST operations worked (`register_project_tool`, `get_ast`).
- `run_query` failed consistently.
- Fresh runtime test succeeded after patching search code to support QueryCursor API.

## Local fix applied
Patched file in local wrale venv:
- `/home/li/git/Mentci-AI/.venv-wrale/lib/python3.13/site-packages/mcp_server_tree_sitter/tools/search.py`

Patch behavior:
- try legacy `query.captures(...)` if available
- otherwise use:
  - `from tree_sitter import QueryCursor`
  - `QueryCursor(query).captures(tree.root_node)`
- normalize captures to `capture_name -> [nodes]`

## Validation
Fresh mcporter runtime (single process):
1. register project
2. run query `(function_item) @fn` against `Components/mentci-user/src/main.rs`
3. received function captures with snippet text (success)

## Operational notes
- Existing long-lived runtime/daemon can keep old broken server code loaded.
- Pi-visible success must come from Pi tool calls, not shell-only checks.

## Current Mentci config/extension adjustment
- Removed `lifecycle: keep-alive` from `config/mcporter.json`.
- Updated `.pi/extensions/mentci-wrale.ts` to create/close mcporter runtime per tool call.
  - avoids stale in-memory wrale process reuse
  - ensures each Pi wrale tool call runs against current runtime state.

## Pi verification sequence (required)
1. `/reload`
2. `wrale_register_project` with `path:"."`, `name:"mentci"`
3. `wrale_run_query` with:
   - `project:"mentci"`
   - `filePath:"Components/mentci-user/src/main.rs"`
   - `query:"(function_item) @fn"`
   - `language:"rust"`
   - `maxResults:2`
4. Confirm non-error capture results in Pi tool output.

## Follow-up
- Upstream bug should be patched in wrale release.
- Once upstream ships compatible `run_query`, drop local venv patch requirement.
