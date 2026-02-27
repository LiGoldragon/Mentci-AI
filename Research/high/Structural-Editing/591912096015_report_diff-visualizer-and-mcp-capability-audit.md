# Report: Diff Visualizer Options for JJ + MCP Capability Audit (Pi devshell)

## Scope
- Evaluate best human-readable diff tooling with Jujutsu (`jj`) support.
- Verify web-research capability and Linkup usability in the active runtime context.
- Audit currently available MCP/tool capabilities for Pi in this workspace.

## 1) Web Research Status
Yes. Web-facing discovery was performed using both GitHub search and live Linkup API calls.

### Commands used
- `gh search repos "mcp server web search" --limit 12`
- `gh search repos "jujutsu diff" --limit 5`
- `gh search code "jj diff --tool" --limit 5`
- Linkup API (`/search`, `/fetch`, `/credits/balance`) via authenticated HTTP calls.

### Observed ecosystem signals
- Active MCP web-search servers commonly target:
  - Tavily
  - SearXNG
  - Serper
  - Brave Search
  - DuckDuckGo
- Public repos indicate this is now a common integration pattern across coding agents.

## 2) Linkup Runtime Report (actual context test)

## Result
Linkup is operational in this runtime context.

### Verified checks
1. **Auth/key presence**
   - `LINKUP_API_KEY` present in environment.
2. **Balance check**
   - `GET https://api.linkup.so/v1/credits/balance`
   - Result: `HTTP 200`, response included positive balance.
3. **Search execution**
   - `POST https://api.linkup.so/v1/search` with `outputType: sourcedAnswer`
   - Result: `HTTP 200`, answer + sources returned.
4. **Fetch execution**
   - `POST https://api.linkup.so/v1/fetch`
   - Result: mixed by URL (`200` on multiple targets; one `400` on a specific docs URL), confirming service reachability and normal endpoint behavior.

### Interpretation
- Linkup itself is not broken.
- Prior limitations were tool-surface integration differences (Pi extension invocation path vs this harness tool surface), not API failure.

## 3) Best diff visualizer for JJ (human readability)

## Recommendation
1. **Difftastic (`difft`)** — best semantic readability and structural/code-aware diffs (tree-sitter parsing).
2. **delta** — best polished line-based patch readability in terminal.

### Why
- `difft` is strongest for refactors/moves/syntactic deltas.
- `delta` is strongest for fast patch scanning and day-to-day visual comfort.

### Local availability (verified)
- `difft` installed: `Difftastic 0.67.0`
- `delta` installed: `delta 0.18.2`

### JJ usage patterns
- Structural pass: `jj diff --tool difft`
- Patch pass: `jj diff --git | delta`

### Markdown rendering note
- `difft`/`delta` highlight markdown syntax and code blocks but do not provide full rendered markdown document views.
- For rendered markdown readability, pair with a markdown renderer such as `glow`/`mdcat` in review workflow.

## 4) Current Pi tool/MCP capability audit (workspace)

## Repo-local Pi extension tools
From `.pi/extensions/*.ts`:
1. `structural_edit`
   - File: `.pi/extensions/mentci-workspace.ts`
   - Backend used by extension: `target/release/mentci-mcp-edit`
   - Purpose: AST-aware edits + custom diff rendering in Pi UI.
2. `mentci_stt_transcribe`
   - File: `.pi/extensions/mentci-stt-ui.ts`
   - Purpose: STT transcription via `mentci-stt` binary.
   - Extra command: `/stt-latest`.

## mentci-mcp server tools (JSON-RPC tool list)
From `Components/mentci-mcp/src/main.rs`:
1. `structural_edit`
2. `capnp_sync_protocol`

## Installed extension packages (configured)
From `.pi/settings.json` / `.pi/extensions.edn`:
- `npm:@aliou/pi-linkup`
- `npm:@juanibiapina/pi-plan`
- `npm:@oh-my-pi/subagents`
- `npm:@oh-my-pi/lsp`
- `npm:mcporter`
- `npm:@oh-my-pi/omp-stats`

## Additional package-level capability notes
- `@aliou/pi-linkup`: `linkup_web_search`, `linkup_web_answer`, `linkup_web_fetch`, `/linkup:balance`.
- `@oh-my-pi/lsp`: single `lsp` tool with diagnostics/references/rename/actions/symbols (+ rust-analyzer extras when available).
- `@oh-my-pi/subagents`: `task` delegation tool for parallel specialized agent runs.
- `@juanibiapina/pi-plan`: `/plan` mode; constrains active tools to read-only set while planning.
- `mcporter`: MCP discovery/call/generation runtime/CLI (enabler for MCP workflows).

## 5) Practical standard search stack in current ecosystem
Recurring web-search backends in current MCP/server ecosystem:
- Tavily
- Brave Search
- Serper
- SearXNG
- DuckDuckGo

Terminal-native research companions:
- `gh search` for GitHub repos/code/issues
- Linkup for broad web retrieval and sourced answers

## Conclusion
- Web research is verified using both GitHub and Linkup.
- Linkup is functioning in the active runtime (auth + balance + search tested successfully).
- For JJ diff readability, use a two-pass model: `difft` (semantic) + `delta` (presentation), optionally paired with markdown renderer tools.
- Current local Pi + Mentci tool stack includes `structural_edit`, `capnp_sync_protocol`, STT tooling, LSP tooling, subagent delegation, and Linkup web-search capability via extension package.