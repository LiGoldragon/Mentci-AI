# CLAUDE.md

Guidance for Claude Code when operating in this repository.

## Primary authority

Follow these files first:
- `Core/AGENTS.md`
- `Core/ARCHITECTURAL_GUIDELINES.md`
- `Core/VersionControlProtocol.md`
- `Library/RestartContext.md`
- relevant language guides such as `Core/SEMA_RUST_GUIDELINES.md` and `Core/SEMA_NIX_GUIDELINES.md`

If this file conflicts with those sources, the `Core/` documents win.

## What this repository is

Mentci-AI is a Nix-and-Rust AI daemon workspace built around Sema Object Style, JJ-first version control, and repo-local Nix evaluation.

Key top-level areas:
- `Core/` — mandatory policy and protocol
- `Components/` — implementation crates, Nix expressions, schemas, workflows
- `Library/` — architecture docs, specs, restart context
- `Research/` — reports, investigations, external validation
- `docs/plans/` — execution-oriented implementation plans

## Development entrypoint

Use the dev shell:

```bash
nix develop
```

Useful commands:

```bash
cargo build
cargo test --manifest-path Components/mentci-aid/Cargo.toml
nix build .#execute
nix flake check
```

## JJ workflow

Jujutsu is the workflow authority. Git is backend transport only.

- Use `$MENTCI_TARGET_BOOKMARK`
- Do not leave a dirty working copy at the end of a task
- A change is not complete until the target bookmark is moved, pushed, and verified on `origin`
- Prefer `execute finalize` for session synthesis when appropriate

## Repo-specific cautions

- Nix must stay repo-local and self-contained
- Do not patch files with ad-hoc Python or regex scripts
- Keep research in `Research/` and plans in `docs/plans/`
- Treat nested component repositories as separate JJ contexts
