# Report

- Subject: `Project-Hardening`
- Kind: `answer`
- Prompt: `I want a jailed shell where I can work on mentci-ai's full worktree (its own copy of it)`

## Result
Added a dedicated admin-shell workflow that creates/reuses a mutable Mentci workspace copy under `Outputs/` and opens a Bubblewrap-jail terminal from that workspace.

## Changes
1. Removed mandatory Cap'n Proto requirement for bootstrap CLI path:
- `Components/src/jail_bootstrap.rs` now accepts CLI-only bootstrap arguments when `--capnp` is omitted.
- Help text updated to mark `--capnp` optional.

2. Added interactive admin shell script:
- `execute`
- Validates args, bootstraps workspace with `mentci-ai job/jails bootstrap`, then launches `mentci-jail-run` with workspace CWD.
- Defaults:
  - `outputName=mentci-ai-admin`
  - `outputsDir=Outputs`
  - `workingBookmark=dev`
  - `targetBookmark=jailCommit`

3. Added script testing context:
- `executeadmin_shell/TestingContext.md`

4. Added top-level command in dev shell package set:
- `Components/nix/common_packages.nix` now exports `mentci-admin-shell`.

5. Updated documentation:
- `README.md` usage includes `mentci-admin-shell` plus options and examples.

6. Added bootstrap regression coverage:
- `Components/tests/mentci_bootstrap.rs` includes a test for CLI-only bootstrap without `--capnp`.

## Operational Notes
- Workspace copy path: `Outputs/<output-name>`
- Runtime metadata path: `Outputs/<output-name>/.mentci/runtime.json`
- This preserves jail isolation while giving a full editable working tree copy separate from repo root CWD.
