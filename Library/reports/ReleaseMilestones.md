# Mentci-AI: Release Milestones and Goal Logs

This file maintains the durable record of session milestones and goal outcomes since the last major release tag.

## release: v0.12.10.48.58 - logical editing plane, adaptive authority, and search intelligence integration
♓︎10°48'58" | 5919 AM

- **Logical Editing Plane:** Integrated `wrale/mcp-server-tree-sitter` (renamed to `mentci-tree-sitter`) as the primary structural analysis layer. Agents now utilize AST-aware queries (`logical_run_query`) and hierarchical syntax inspection (`logical_get_ast`) for deep codebase visibility.
- **Adaptive Skill Authority:** Designed and implemented a three-tier authority model (Core, Absolute Rules, Adaptive Heuristics). Skills like `sema-programmer` now adapt at runtime by capturing boundary conditions in `runtime.edn` and compiling them into token-efficient `absolute_rules.md` digests.
- **Logical File Search (LFS):** Designed a semantic repository indexer utilizing an architecture-aware SQLite shadow database (`.mentci/logical_fs.db`) to enable instant, categorized file discovery (logic, schema, data, research, library).
- **Search Intelligence:** Integrated the `linkup` search stack and codified a **Web Validation Mandate** in `Core/AGENTS.md`, requiring agents to perform routine external validation before asserting ecosystem status or maturity.
- **Mirror Hook Protocol:** Implemented a terminal-synchronized mirror protocol (`ui_mirror.txt`) that allows agents to "see" their own rendered TUI state, eliminating agent-user display hallucinations.
- **Rust Warning Hygiene:** Conducted a workspace-wide audit and resolved all Rust warnings, establishing a zero-warning baseline for future development.
- **Environment Hardening:** Fixed `mentci-user` setup binary decoding for packed Cap'n Proto messages and ensured secret injection diagnostics are visible in the `nix develop` shellHook.

## release: v0.12.9.59.28 - aski projector, native capnp sync protocol, and edn spec-source migration
♓︎9°59'28" | 5919 AM

- **Aski Projector:** Implemented native Rust EDN-to-Cap'n-Proto-text projection in `Components/aski-lib`.
- **MCP Protocol Tooling:** Added `capnp_sync_protocol` to `Components/mentci-mcp` for deterministic hash-synced binary generation.
- **Spec Authority Migration:** Converted `mentci-stt`, `mentci-user`, and `mentci-intel` data authorities from ad-hoc text to EDN source sidecars.
- **Operational Cleanup:** Removed obsolete sync shell surfaces and aligned symlinked `.bin` message pointers.

## release: v0.12.9.4.12 - capnp component locality, hash-synced messages, and pure nix devshell integration
♓︎9°4'12" | 5919 AM

- **Cap'n Proto Component Locality:** Implemented a core architectural shift where all Cap'n Proto schemas (`*.capnp`) now reside within their respective component's logic directory (e.g., `Components/mentci-user/schema/`).
- **Hash-Synced Binary Messages:** Established a protocol for passing structured data to components using prebuilt Cap'n Proto binary messages synced to text-versions via SHA-256 hashes.
- **Pure Nix devShell Secret Injection:** Refactored `mentci-user` into a generic Rust tool that bootstraps environment secrets via the new Cap'n Proto spec.

## release: v0.12.8.57.3 - vtcode integration, ui abstraction, and sema lineage mining
♓︎8°57'3" | 5919 AM

- **VTCode Integration:** Built `vinhnx/vtcode` via Nix, configured its `.vtcode/vtcode.toml` for safe local policies.
- **UI Abstraction Protocol:** Formally documented that all agent UIs must consume API keys structurally, strictly forbidding local `.env` usage.
- **Sema Lineage Crystallization:** Mined ChatGPT exports to extract high-weight architectural guidance.

## release: v0.12.8.5.55 - codify intent, deprecate legacy patching, and establish mcp/ast programmatic refactoring
♓︎8°5'55" | 5919 AM

- **Philosophical Alignment:** Authored `Core/PHILOSOPHY_OF_INTENT.md` and updated `ARCHITECTURAL_GUIDELINES.md` to establish the "Local Machine of Fit".
- **Rust-Only Mandate:** Eliminated Python and legacy Clojure scripting allowances.
- **AST/CST Refactoring:** Scaffolded `Components/mentci-mcp`, a native Rust Model Context Protocol server exposing `ast-grep` and `tree-sitter`.

## [Previous Historical Logs Elided for Brevity - see git history]
♓︎.6.8.53 5919AM | v0.12.6.8.53 | Major structural shift: Rust Actor Orchestration, Logic-Data Separation, and Full Repository Sweep complete. Deleted legacy Babashka scripts.
