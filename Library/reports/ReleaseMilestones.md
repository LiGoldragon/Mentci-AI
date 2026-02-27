## release: v0.12.9.4.12 - capnp component locality, hash-synced messages, and pure nix devshell integration
♓︎9°4'12" | 5919 AM

- **Cap'n Proto Component Locality:** Implemented a core architectural shift where all Cap'n Proto schemas (`*.capnp`) now reside within their respective component's logic directory (e.g., `Components/mentci-user/schema/`).
- **Hash-Synced Binary Messages:** Established a protocol for passing structured data to components using prebuilt Cap'n Proto binary messages synced to text-versions via SHA-256 hashes (e.g., `setup_4311f0704ec238e2.bin`).
- **Pure Nix devShell Secret Injection:** Refactored `mentci-user` into a generic Rust tool that bootstraps environment secrets via the new Cap'n Proto spec, enabling the Nix `dev_shell.nix` to inject keys purely without shell script wrappers.
- **Architectural Guidance Updates:** Formalized component-local schemas and synced binary message requirements in `Core/ARCHITECTURAL_GUIDELINES.md`.
- **Logic Cleanup:** Decoupled `mentci-stt` from internal secret resolution, delegating environment hydration entirely to the primary `mentci-user` devShell integration.

## release: v0.12.8.57.3 - vtcode integration, ui abstraction, and sema lineage mining
♓︎8°57'3" | 5919 AM

- **VTCode Integration:** Built `vinhnx/vtcode` via Nix, configured its `.vtcode/vtcode.toml` for safe local policies, and established `mentci-vtcode` to inject API keys via `mentci-user export-env`.
- **UI Abstraction Protocol:** Formally documented in `Library/documentation/UserInterfaceDocumentation.md` that all agent UIs must consume API keys structurally, strictly forbidding local `.env` usage.
- **Workflow Interruption Fix:** Globally disabled the `pi-superpowers-plus` extension to prevent unwanted interruptions and TUI conflicts during high-focus sessions.
- **Sema Lineage Crystallization:** Mined ChatGPT exports to extract high-weight architectural guidance, clarifying the evolution of Sajban into Sema Object Style, Aski, Lojix, and Criome state domains.
- **Ad-Hoc Script Deprecation:** Bootstrapped the `mentci-dig` Rust component to structurally extract AST/JSON/EDN data, replacing disposable Python data-mining scripts.
- **Commit History Hygiene:** Enforced atomic intent-scoping by dropping empty/stray commits and rewriting malformed release descriptions in the `dev` branch.

# Mentci-AI: Release Milestones and Goal Logs

This file maintains the durable record of session milestones and goal outcomes since the last major release tag.

## release: v0.12.8.5.55 - codify intent, deprecate legacy patching, and establish mcp/ast programmatic refactoring
♓︎8°5'55" | 5919 AM

- **Philosophical Alignment:** Authored `Core/PHILOSOPHY_OF_INTENT.md` and updated `ARCHITECTURAL_GUIDELINES.md` to establish the "Local Machine of Fit", rejecting manipulative AI governance and enforcing local, patient execution.
- **Rust-Only Mandate:** Eliminated Python and legacy Clojure scripting allowances. All agentic execution, utilities, and integrations must be built in Rust + Cap'n Proto.
- **AST/CST Refactoring:** Forbidden LLM regex/string patching. Scaffolded `Components/mentci-mcp`, a native Rust Model Context Protocol server exposing `ast-grep` and `tree-sitter` for flawless, lossless concrete syntax tree manipulations.
- **Agent Workflow Safety:** Forbidden recursive `pi` shell calls that break TUI rendering. Implemented `execute transition` which safely spawns a new `foot` terminal via `systemd --user` for environment refreshes.
- **Logic-Data Purity:** Elevated strict logic-data separation rules. Implemented `mentci-user` to dynamically resolve `gopass` secrets (like Linkup API keys) via `.mentci/user.json` sidecars instead of hardcoded shell exports.
- **High-Fidelity Transcription:** Built `mentci-stt` in Rust to transcribe Opus voice notes using Gemini, explicitly prompting for Mentci jargon (Aski, Lojix, SEMA) and automatically dating transcripts via `chronos`.
- **Environment Updates:** Upgraded `pi` agent to 0.55.0, mitigated the Nix Tokenization Problem via path-shortening skills, and formalized the multi-socket Mentci-Box daemon topology in the high-level goals.

## session: extend devShell with pi-sync-extensions and Level 5 agent UI defaults
♓︎7°42'12" | 5919 AM

- **Extension Sync Script:** Developed `Components/tools/pi-sync-extensions.bb` (Babashka) to automate the installation and configuration of curated Level 5 extensions (`pi-linkup`, `pi-plan`, `pi-task-tool`, `pi-lsp`, `mcporter`, `omp-stats`).
- **Declarative Configuration:** Established `.pi/extensions.edn` as the project-root authority for agent interface defaults, including `gemini-2.5-flash` model and token compaction settings.
- **Environment Automation:** Integrated the sync script into the `devShell` `shellHook` and added `pkgs.nodejs` to `common_packages.nix` to support `npm`-based extension fetching.

## session: automate pi path stabilization and nix tokenization fix in devShell
♓︎7°11'14" | 5919 AM

- **Automated Symlink:** Implemented a `shellHook` in `Components/nix/dev_shell.nix` that establishes a stable `~/.pi/pi-source` symlink to the current `pi` package in the Nix store.
- **Tokenization Fix:** Exported `PI_PACKAGE_DIR` in the `devShell` to point to the stable link, preventing context-wasting Nix hashes from leaking into agent prompts.
- **Environment Parity:** Synced `PI_AI_ANTIGRAVITY_VERSION` and path stabilization logic between the `pi` package wrapper and the development shell.

## session: rename pi nix definitions and align operator interface identity
♓︎7°4'31" | 5919 AM

- **Nix Renaming:** Renamed `coding_agent.nix` to `pi.nix` and `pi_agent_rust.nix` to `pi-rust.nix` to match utility names.
- **Namespace Convergence:** Updated `Components/nix/default.nix` and `flake.nix` to use `pi` and `pi_rust` identifiers, establishing the TypeScript implementation as the primary `apps.pi` entrypoint.
- **Dependency Alignment:** Propagated renaming to `attractor`, `common_packages`, and check logic, ensuring consistent package propagation.
- **Documentation Sync:** Updated `RestartContext.md` to reflect the refined operator interface identity (`pi` vs `pi-rust`).

## session: implement mentci-launch component and validate launch planning tests
♓︎7°1'7" | 5919 AM

- **Component Scaffolding:** Initialized `Components/mentci-launch` Rust crate with core logic for systemd-controlled terminal spawning.
- **Contract Fulfillment:** Implemented `MentciLaunchRequest` Cap'n Proto schema integration and verified deterministic data delivery to `mentci-box`.
- **Validation:** Added smoke tests for launch orchestration and verified environmental clean-room isolation within the jail environment.

## [Previous Historical Logs Elided for Brevity - see git history]
♓︎.6.8.53 5919AM | v0.12.6.8.53 | Major structural shift: Rust Actor Orchestration, Logic-Data Separation, and Full Repository Sweep complete. Deleted legacy Babashka scripts.

## release: v0.12.9.59.28 - aski projector, native capnp sync protocol, and edn spec-source migration
♓︎9°59'28" | 5919 AM

- **Core Hash:** `f251679`
- **Aski Projector:** Implemented native Rust EDN-to-Cap'n-Proto-text projection in `Components/aski-lib`.
- **MCP Protocol Tooling:** Added `capnp_sync_protocol` to `Components/mentci-mcp` for deterministic hash-synced binary generation.
- **Spec Authority Migration:** Converted `mentci-stt`, `mentci-user`, and `mentci-intel` data authorities from ad-hoc text to EDN source sidecars.
- **Operational Cleanup:** Removed obsolete sync shell surfaces and aligned symlinked `.bin` message pointers.

### transition from previous tagged release (`v0.12.8.5.55`)
- Added VTCode integration lane and hardened Gemini schema compatibility for tool calls.
- Enforced stronger component-local Cap'n Proto schema/data contracts.
- Shifted structured message authoring toward EDN-first authoring with Rust-native projection.
- Continued Rust-only replacement of script surfaces and policy codification in core guidance.
