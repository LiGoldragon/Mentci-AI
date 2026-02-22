# Session Report

- Kind: `answer`
- Subject: `Top-Level-FS-Spec`
- Title: `implement-top-level-fs`

## Prompt
implement top level fs

## Outcome
Implemented top-level FS minimization and enforcement.

### Structural moves
- `ARCHITECTURE.md` -> `Library/architecture/ARCHITECTURE.md`
- `BUGS.md` -> `Library/research/BUGS.md`
- `Work.md` -> `Library/research/WORK_CONTEXT.md`
- `agent-sources.edn` -> `Core/agent-sources.edn`
- `jj-project-config.toml` -> `Core/jj-project-config.example.toml`

### Top-level enforcement
- Added root contract checker:
  - `Components/scripts/root_guard/main.clj`
  - `Components/scripts/root_guard/TESTING_CONTEXT.md`
- Added local runtime config ignore:
  - `/.mentci/` in `.gitignore`
- Updated protocol to require root guard run:
  - `Core/VERSION_CONTROL.md`
- Updated FS spec with root-file allowlist and guard reference:
  - `Library/specs/ASKI_FS_SPEC.md`

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
