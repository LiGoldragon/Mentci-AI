# Nix Symbol Bottleneck in jCodeMunch + LSP Migration Insights

## Original Prompt Context
User requested practical validation of Nix support in the active Pi + devshell environment, then asked whether LSP would provide better results and requested all research/insights to be persisted.

## Executive Summary
- `jcodemunch` in production now accepts `language: "nix"` in `search_symbols` after patching server schema enum and pinning to fork.
- Nix parsing is present, but extraction quality is limited by a narrow custom symbol walker.
- Many CriomOS Nix files are correctly recognized as `.nix` but still appear in `no_symbols_files` due to conservative extraction heuristics.
- For repository-scale Nix navigation, an MCP↔LSP lane (e.g., `nixd` via LSP bridge MCP server) is likely a higher-leverage strategy than extending ad-hoc symbol scraping alone.

## What Was Verified (Local + MCP)
1. `jcodemunch_index_folder` against `/home/li/git/CriomOS--dev` succeeds and classifies indexed language as Nix.
2. `jcodemunch_search_symbols` now accepts `language: "nix"` (previously rejected by schema enum).
3. `search_symbols` returns some Nix symbols (e.g., `fallbackInputs` in `default.nix`).
4. A large set of CriomOS Nix files remains in `no_symbols_files` despite being valid Nix source.

## Root Cause Analysis
The bottleneck is in custom Nix extraction logic (`_parse_nix_symbols` / `_extract_nix_binding`), not parser initialization itself:
- Traversal depth capped (`MAX_DEPTH = 4`).
- Only `binding` nodes in select containers are walked.
- Attrpaths restricted to exactly one identifier (dotted/compound paths skipped).
- Symbol kinds reduced to `function` when RHS is `function_expression`, otherwise `constant`.
- Limited support for common Nix idioms (`inherit`, richer attrset/module patterns, nested forms).

Net effect: parse succeeds, but symbol emission is sparse for real-world Nix module trees.

## Upstream/Fork Work Completed
- Forked: `LiGoldragon/jcodemunch-mcp`.
- Applied patch to expose `nix` (and newer supported languages) in `search_symbols.language` enum.
- Fork commit used for production pin: `991bd5848583e53c1d8531d105b8e9f2bedd8d21`.
- Production derivation switched from PyPI tarball to forked GitHub source in `Components/nix/jcodemunch-mcp.nix`.

## External Research Findings (LSP Direction)
Candidate MCP/LSP bridges and servers identified:
- `Tritlo/lsp-mcp`
- `isaacphi/mcp-language-server` (+ multi-LSP forks)
- `bug-ops/mcpls`
- `rockerBOO/mcp-lsp-bridge`
- Native MCP mode example: `gopls` (Go)

Nix language intelligence references:
- `nix-community/nixd` (feature-rich Nix language server, Nix-aware semantics)
- `oxalica/nil` (incremental Nix language server)
- `nix-community/rnix-parser`, `astro/deadnix`, `oppiliappan/statix` for AST/scoping inspiration.

## Strategic Recommendation
Use a two-lane approach:
1. **Short-term reliability:** keep jCodeMunch Nix support for lightweight symbol/text retrieval.
2. **Primary navigation quality:** integrate `nixd` through an MCP↔LSP bridge for defs/refs/diagnostics/workspace symbols.

This balances immediate utility with stronger semantic retrieval needed for CriomOS-scale refactors.

## Suggested Next Implementation Slice
- Add an MCP LSP bridge component to devshell/Pi extension lane.
- Configure it for `nixd` on CriomOS workspace root.
- Validate end-to-end with tasks:
  - definition lookup for key symbols
  - workspace symbol search across `nix/mkCrio*`
  - reference search for selected bindings
  - diagnostics retrieval for targeted files.
