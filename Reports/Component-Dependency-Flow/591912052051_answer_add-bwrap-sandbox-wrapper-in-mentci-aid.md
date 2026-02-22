# Session Report

- Kind: `answer`
- Subject: `Component-Dependency-Flow`
- Title: `add-bwrap-sandbox-wrapper-in-mentci-aid`

## Prompt
implement a bwrap wrapper in execute (sandbox)

## Outcome
Added a Bubblewrap-backed sandbox command path to `mentci-aid` and exposed it under both:
- `mentci-ai sandbox ...`
- `mentci-ai execute sandbox ...`

## Implemented Changes
1. Added `Components/mentci-aid/src/sandbox.rs`:
- CLI parsing for sandbox flags (`--workdir`, `--home`, `--bind`, `--ro-bind`, `--setenv`, `--share-net`).
- Deterministic Bubblewrap argument assembly.
- Command execution with inherited stdio and explicit non-zero exit propagation.
- Basic argument parser tests.

2. Updated `Components/mentci-aid/src/main.rs`:
- Registered new `sandbox` module.
- Routed both `sandbox` and `execute sandbox` command forms.
- Expanded usage text for sandbox invocation.

3. Updated `Components/nix/common_packages.nix`:
- Added `pkgs.bubblewrap` so `bwrap` is available in the dev shell.

4. Updated `README.md`:
- Added sandbox usage example under the main usage list.

## Validation
- `nix develop -c cargo test --manifest-path Components/mentci-aid/Cargo.toml` passed.
- New sandbox unit tests passed:
  - `sandbox::tests::parse_sandbox_args_reads_explicit_values`
  - `sandbox::tests::parse_env_requires_key_value_shape`
  - `sandbox::tests::parse_bind_requires_source_and_target`
- `nix develop -c cargo run --quiet --manifest-path Components/mentci-aid/Cargo.toml --bin mentci-ai -- sandbox --help` printed expected usage.
