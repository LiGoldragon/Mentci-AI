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
- `BUGS.md` -> `Library/reports/BUGS.md`
- `Work.md` -> `Library/reports/WORK_CONTEXT.md`
- `agent-inputs.edn` -> `Core/agent-inputs.edn`
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
  - `Core/ASKI_FS_SPEC.md`

### Runtime/config rewiring
- Nix wiring updated to current component paths and local JJ config location:
  - `flake.nix` uses `./Components/nix` and `./Components/scripts`
  - `Components/nix/dev_shell.nix` sets `JJ_CONFIG="$(pwd)/.mentci/jj-project-config.toml"`
  - `Components/nix/default.nix` / `Components/nix/jail_inputs.nix` use `inputs` consistently
- Launcher/remount now use canonical whitelist path:
  - `Core/agent-inputs.edn`

### Validation
- `bb Components/scripts/root_guard/main.clj` -> pass
- `bb Components/scripts/inputs_remount/main.clj --help` -> pass
- `bb Components/scripts/validate_scripts/main.clj` -> fails on pre-existing issue (`Components/scripts/program_version/main.clj` missing `defn*`)
