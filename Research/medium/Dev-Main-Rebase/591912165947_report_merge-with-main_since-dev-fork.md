# Merge-with-main Report (since `dev` forked from `main`)

## Scope
- Base: `main` @ `550fd9de`
- Head: `dev` @ `f270b8af`
- Revset analyzed: `main..dev`
- Divergence size: **31 commits** on `dev` not in `main`

## High-level change themes
1. **Nix rebuild-decoupling and packaging isolation**
   - Removed broad rebuild coupling from devshell/package wiring.
   - Introduced minimal bootstrap package (`mentci-bootstrap`) and moved default startup path away from `execute`.
   - Reworked component packaging lock strategy toward component-local authority.

2. **Execute extraction from mentci-aid source authority**
   - Added dedicated `mentci-execute` component source.
   - Rewired `execute` derivation to use `mentci-execute-src` instead of `mentci-aid-src`.

3. **Component source governance updates**
   - Updated component registry metadata and split governance config.
   - Component source inputs moved to public GitHub-backed authority and re-locked in `flake.lock`.

4. **Verification/reporting artifacts**
   - Added multiple research reports documenting root-cause analysis, migration steps, and no-rebuild verification loops.

## File-level impact summary (`jj diff -r main..dev --summary`)
- **Core Nix wiring modified:**
  - `Components/nix/default.nix`
  - `Components/nix/common_packages.nix`
  - `Components/nix/jail.nix`
  - `Components/nix/mentci_ai.nix`
  - `Components/nix/mentci_box.nix`
  - `Components/nix/mentci_launch.nix`
  - `Components/nix/mentci_mcp.nix`
  - `Components/nix/mentci_stt.nix`
  - `Components/nix/mentci_user.nix`
  - `Components/nix/chronos.nix`
  - `Components/nix/execute_check.nix`
  - `Components/nix/execute.nix` (new via copy/rename path history)
  - `Components/nix/mentci_bootstrap.nix` (new)
- **Source/contract metadata modified:**
  - `Components/contracts/rust-component-repos.toml`
  - `Components/index.edn`
- **Flake authority updated:**
  - `flake.nix`
  - `flake.lock`
- **Protocol/docs/research updates:**
  - `.pi/skills/independent-developer/SKILL.md`
  - `Core/HIGH_LEVEL_GOALS.md`
  - `Library/documentation/UserInterfaceDocumentation.md`
  - new reports under `Research/high/...` and `Research/medium/JCodeMunch-MCP/...`

## Notable `dev` commits (newest → oldest)
- `f270b8af` intent: lock component inputs to github flake sources after public visibility transition
- `0b4ea47b` intent: switch component source inputs to canonical flake github url style
- `ed94f0c9` intent: replace local git+file component inputs with github git ssh sources
- `827f8b8b` session: finalize execute extraction and component-local lock authority with no-rebuild verification
- `e3fa668b` intent: extract execute to dedicated component source and remove nix-local lock directory coupling
- `ee547b3c` session: finalize bootstrap isolation and local lock migration with repeated no-rebuild checks
- `ca70c20a` intent: add minimal mentci-bootstrap component and remove execute from default devshell packages
- `f70ba73b` intent: minimize bootstrap package and remove repo-root rebuild coupling from split rust derivations
- `7538aa47` intent: decouple devshell execute from mentci-ai and remove pi-rust from shell packages
- `6545d2ca` intent: add research report on jcodemunch mcp

## Merge risk assessment
- **Low-to-moderate conflict risk** if `main` touched these same Nix/flake files since divergence:
  - `flake.nix`, `flake.lock`, `Components/nix/default.nix`, `Components/nix/common_packages.nix`, `Components/nix/jail.nix`
- **Behavioral risk to verify post-merge:**
  - component source resolution and lock pin correctness
  - devshell startup path (`mentci-bootstrap` route)
  - package set no-rebuild expectations under small unrelated commits

## Recommended merge validation checklist
1. `nix build --no-link .#mentciAi .#execute .#mentciBootstrap .#chronos .#mentciStt .#mentciUser .#mentciMcp .#mentciLaunch .#mentciBox .#mentciBoxDefault .#pi .#piWithExtensions .#unifiedLlm .#vtcode`
2. `nix build --dry-run --no-link <same attr set>` after a tiny docs-only commit to confirm no rebuild churn.
3. `nix develop github:LiGoldragon/Mentci-AI/dev --refresh --command pi --version`
4. Confirm `dev == dev@origin` and perform merge/rebase into `main` with conflict review on Nix/flake files.
