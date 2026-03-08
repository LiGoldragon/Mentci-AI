# Subagents Extension in Nix Devshell Pi: Install + Function-Scoped Usage Guide

- **Solar:** `5919.12.18.51.47`
- **Programming:** `3wyybz4j`
- **Intent:** enable existing `@oh-my-pi/subagents` in the Nix-packaged `pi` wrapper and document practical usage for function-first orchestration.

## Why this matters

Current single-agent sessions accumulate noisy intermediate artifacts (FS scouting output, MCP traversal, long bash output). Subagents reduce this by isolating context and returning condensed results.

Given your preference, this guide frames orchestration by **function** (scout / plan / implement / review) rather than by fixed tool assumptions.

## What was installed

`@oh-my-pi/subagents@1.3.3710` is now packaged and wired into `pi-with-extensions`.

### Nix wiring summary
- Added package: `Components/nix/pi-subagents-extension.nix`
- Wired in `Components/nix/default.nix` as `pi_subagents_extension`
- Linked and loaded in `Components/nix/pi-with-extensions.nix`
  - symlink path: `node_modules/@oh-my-pi/subagents`
  - wrapper arg: `--extension "$PI_PACKAGE_DIR/node_modules/@oh-my-pi/subagents"`
- Added checks in `Components/nix/pi_with_extensions_check.nix`
- Exposed as package output in `flake.nix`: `piSubagentsExtension`

### Validation evidence
- `nix build .#checks.x86_64-linux.piWithExtensions` passed.
- `nix build .#packages.x86_64-linux.piWithExtensions` passed.
- Wrapper references and extension path anchors confirmed by scoped `rg` checks.

## What the extension already provides

From installed package metadata:
- Task tool runtime (`tools/index.ts`) spawns independent `pi` subprocesses.
- Bundled agents:
  - `task`, `planner`, `explore`, `reviewer`, `browser`
- Bundled workflow commands:
  - `implement.md`, `implement-with-critic.md`, `architect-plan.md`

Notable runtime behavior:
- each spawned agent has isolated context,
- results are persisted to task output files,
- supports parallel task arrays,
- can scope agent discovery (`user` / `project` / `both`).

## Function-scoped usage model (recommended)

Treat these as **functions** (roles), not tool identities:

1. **Scout function**
   - purpose: gather bounded evidence from FS/MCP/bash
   - likely agent: `explore`

2. **Plan function**
   - purpose: convert evidence into actionable sequence
   - likely agent: `planner`

3. **Implement function**
   - purpose: apply approved mutation
   - likely agent: `task`

4. **Verify/review function**
   - purpose: detect regressions, policy violations, missed edge cases
   - likely agent: `reviewer`

5. **Fetch function** (optional)
   - purpose: single URL extraction
   - likely agent: `browser`

This function-first framing allows each role to use whatever tools are appropriate now or later (LSP, structural_edit, MCP, bash), without overfitting to today’s toolset.

## Practical first workflow

Use the shipped chain command intent (`implement.md`) conceptually as:
1. `explore` on target scope
2. `planner` with prior output
3. `task` with prior output
4. `reviewer` final check

And for noise control:
- keep subagent outputs structured,
- pass only distilled payloads forward,
- avoid dumping raw logs into parent context unless failure debugging requires it.

## Note on existing sema-flow prompt

`.pi/prompts/sema-flow.md` exists but is old and should be reviewed before adoption. This install is independent of that legacy prompt and enables direct experimentation with the currently maintained `@oh-my-pi/subagents` package.

## Next improvement lane

Create project-local subagents in `.pi/agents/` with Cozo/structured payload contracts and explicit function ownership, then invoke Task tool with `agentScope: "both"` to use those custom agents alongside user-level defaults.
