# Mentci-AI: Release Milestones and Goal Logs

This file maintains the durable record of session milestones and goal outcomes since the last major release tag.

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
