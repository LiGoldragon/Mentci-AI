# Session Report

- Kind: `answer`
- Subject: `Top-Level-FS-Spec`
- Title: `implement-top-level-fs`

## Prompt
implement top level fs

## Outcome
Implemented top-level FS minimization and enforcement.

### Structural moves
- `ArchitectureMap.md` -> `Library/architecture/ArchitectureMap.md`
- `Bugs.md` -> `Library/research/Bugs.md`
- `Work.md` -> `Library/research/WorkContext.md`
- `agent-sources.edn` -> `Core/agent-sources.edn`
- `jj-project-config.toml` -> `Core/jj-project-config.example.toml`

### Top-level enforcement
- Added root contract checker:
  - `Components/scripts/root_guard/main.clj`
  - `Components/scripts/root_guard/TestingContext.md`
- Added local runtime config ignore:
  - `/.mentci/` in `.gitignore`
- Updated protocol to require root guard run:
  - `Core/VersionControlProtocol.md`
- Updated FS spec with root-file allowlist and guard reference:
  - `Library/specs/AskiFsSpec.md`

### Runtime/config rewiring
- Nix wiring updated to current component paths and local JJ config location:
  - `flake.nix` uses `./Components/nix` and `./Components/scripts`
  - `Components/nix/dev_shell.nix` sets `JJ_CONFIG="$(pwd)/.mentci/jj-project-config.toml"`
  - `Components/nix/default.nix` / `Components/nix/jail_sources.nix` use `inputs` consistently
- Launcher/remount now use canonical whitelist path:
  - `Core/agent-sources.edn`

### Validation
- `bb Components/scripts/root_guard/main.clj` -> pass
- `bb Components/scripts/sources_remount/main.clj --help` -> pass
- `bb Components/scripts/validate_scripts/main.clj` -> fails on pre-existing issue (`Components/scripts/program_version/main.clj` missing `defn*`)
