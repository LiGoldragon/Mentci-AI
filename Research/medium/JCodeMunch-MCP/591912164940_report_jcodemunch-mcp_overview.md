# jCodeMunch MCP — Research Report (Mentci Context)

## Why this report
User asked for a concise report on the two referenced videos and what jCodeMunch is.

## Executive summary
**jCodeMunch MCP** is an MCP server that indexes source code and lets an LLM retrieve **symbol-level slices** (functions/classes/methods/constants) instead of opening full files. Its core claim is large token-cost reduction for code navigation tasks.

The two videos are both by **J. Gravelle** and discuss this tool:
- Intro/demo video (Mar 1, 2026): explains concept and token-savings examples.
- Follow-up update (Mar 4, 2026): shows global savings counter, status line integration, compatibility updates, and rapid patching.

## What it does (from upstream docs)
jCodeMunch’s README positions the tool as:
1. Index once via tree-sitter parsing.
2. Store symbol metadata + byte offsets.
3. Retrieve exact symbol source by stable symbol IDs.
4. Return metadata including estimated token savings and cost-avoidance counters.

Representative tools exposed over MCP:
- `index_repo`, `index_folder`
- `search_symbols`, `get_symbol`, `get_symbols`
- `get_repo_outline`, `get_file_outline`, `search_text`

The README states 11 tools total.

## Claimed benefits
- Large reduction in token usage when compared to brute-force full-file reads.
- Faster code exploration for agent workflows.
- Works with MCP-compatible clients.
- Local-first indexing/cache under `~/.code-index`.

## Architectural model
The advertised pipeline is:
- **Discovery** (GitHub/local)
- **Filtering/hardening** (path traversal and secret/binary exclusions)
- **Parsing** (tree-sitter)
- **Storage** (index + cached source)
- **Retrieval** (symbol-level by byte offsets)

This matches the stated goal: convert “file reading” into “structured retrieval”.

## Privacy/telemetry notes
The project includes an optional community counter feature. README states shared data is limited to:
- token delta
- anonymous install/session ID

Opt-out env var is documented:
- `JCODEMUNCH_SHARE_SAVINGS=0`

## Important caveats for adoption
1. **License model is dual-use**
   - README explicitly states free non-commercial use and paid commercial license requirement.
   - This is an adoption constraint for production/commercial deployments.

2. **Performance claims are largely project-authored**
   - Current claims (e.g., major token savings percentages) are from project docs/videos.
   - Independent third-party benchmarks were not validated in this pass.

3. **Scope boundaries**
   - README says it is not intended for LSP diagnostics/completions, editing workflows, or full semantic program analysis.

## Relevance to Mentci
High conceptual overlap with Mentci direction:
- structured symbol retrieval
- token minimization
- agent-first code navigation
- explicit logic/data boundaries

Potential use for Mentci would be as an external retrieval tool in coding-agent contexts, with caution around:
- license compatibility
- telemetry policy defaults
- benchmark validation against our own workloads

## Videos: what they are saying in plain terms
- **Video 1 (intro):** “Install this MCP server to avoid expensive full-file reads; symbol-level retrieval drastically lowers token costs.”
- **Video 2 (update):** “Feature updates + community usage growth; token/cost counters added; compatibility and stability improved.”

## Sources
1. YouTube metadata/descriptions/transcripts:
   - https://www.youtube.com/watch?v=vzCy44o3JwA
   - https://www.youtube.com/watch?v=1eb4mn0M9ao
2. Project README:
   - https://github.com/jgravelle/jcodemunch-mcp
   - https://raw.githubusercontent.com/jgravelle/jcodemunch-mcp/main/README.md
3. MCP baseline context:
   - https://github.com/modelcontextprotocol/specification

## Bottom line
Yes — both videos are about **jCodeMunch MCP**. It is a symbol-retrieval MCP server aimed at reducing token/cost overhead in LLM coding workflows. The concept is strong and aligned with Mentci’s structured retrieval philosophy, but operational adoption should be gated by license review and our own benchmark validation.
