# Intent Snapshot: lsp-pi Native LSP Lane for Pi

## Why this snapshot
Saved before context compaction to preserve current implementation direction.

## Current Intent
Adopt a **Pi-native LSP extension path** (bypassing MCP bridge) using `lsp-pi` as the baseline, then extend it for Nix/CriomOS.

## Findings Anchoring This Intent
- `lsp-pi` is a real published Pi package (`npm:lsp-pi`, version `1.0.3`).
- It implements direct LSP JSON-RPC in extension code (`vscode-languageserver-protocol`), not MCP.
- Existing actions cover definition/references/hover/signature/symbols/diagnostics/rename/codeAction.
- It has robust lifecycle controls (LRU open-files, idle cleanup, idle server shutdown, diagnostic fallback).
- It currently does **not** include `.nix` / `nixd` support.

## Planned Technical Direction
1. Fork/patch `lsp-pi` to add Nix support:
   - map `.nix` -> language id `nix`
   - add `nixd` server config with root detection (`flake.nix`, `default.nix`, `shell.nix`)
2. Keep existing hook + tool behavior unchanged; only extend language coverage.
3. Validate on CriomOS with:
   - workspace diagnostics on Nix files
   - definition/reference on known symbols (e.g., `mkCrioSphere`, `fallbackInputs`)
4. If stable, decide whether to:
   - run as project package in Mentci, or
   - upstream to `lsp-pi` and pin a revision/package release.

## Decision Heuristic
- Prefer Pi-native LSP extension lane for Nix semantic navigation quality.
- Keep current jcodemunch lane as fallback/indexing companion during transition.

## Open Questions
- Whether to maintain an internal fork or upstream quickly.
- Exact root detection precedence for mixed Nix layouts in CriomOS.
- Whether to expose additional Nix-specific actions/queries beyond generic LSP actions.
