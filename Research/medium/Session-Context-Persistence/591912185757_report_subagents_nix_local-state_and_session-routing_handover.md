# Session Handover: Subagents Integration, Loader Fix, and Repo-Local Session State

- **Solar:** `5919.12.18.57.57`
- **Programming:** `3wyybz4j`
- **Scope:** Mentci-AI Pi runtime ergonomics, subagent workflow enablement, and session-state locality policy.

## Current Durable State

1. **Subagents extension is Nix-integrated in `pi-with-extensions`**
   - Added package for `@oh-my-pi/subagents@1.3.3710`.
   - Wired in Nix namespace and wrapper launch args.
   - `piWithExtensions` build/check path is green.

2. **Runtime loader path defect was fixed**
   - Initial root package load path failed (`.../@oh-my-pi/subagents`).
   - Correct extension entrypoint in this composition is:
     - `.../@oh-my-pi/subagents/tools/index.ts`.
   - Wrapper/check updated accordingly.

3. **Subagent assets were imported into project scope**
   - `.pi/agents/`: `task`, `planner`, `explore`, `reviewer`, `browser`.
   - `.pi/commands/`: `implement`, `implement-with-critic`, `architect-plan`.

4. **Slash-command compatibility bridge was added**
   - Prompt templates mirrored in `.pi/prompts/` for immediate slash discovery:
     - `/implement`, `/implement-with-critic`, `/architect-plan`.

5. **State-locality policy updated to repo-local**
   - Dev shell now exports:
     - `PI_CODING_AGENT_DIR=$(pwd)/.pi/agent`
     - `PI_PACKAGE_DIR=$(pwd)/.pi/pi-source` (stable link)
   - This prevents new mutable Pi state writes into global `~/.pi/agent` during dev-shell use.

6. **Session visibility side-effect explained**
   - Existing historical sessions remained in global `~/.pi/agent/sessions`.
   - Switching to repo-local agent dir changes active session index location, so historical sessions appear missing until migrated or bridged.

## Recent Commit Chain (context continuity)

- `f5fba9eb9b21` — install subagents extension in nix wrapper
- `c5fd891a27bf` — loader path fix to TS entrypoint
- `b907248d2657` — import agents/commands into project scope
- `abc581e184c2` — expose workflow prompts for slash autocomplete
- `d49cb6091979` — keep Pi runtime state repo-local
- `2628216f2d96` — share oauth auth across repos while keeping sessions repo-local

## Operational Guidance (next steps)

1. **Prefer standards lane over ad-hoc duplication**
   - Long-term: align with OMP-managed loaders/resources model.
   - Short-term: current project prompt bridge is functional for immediate workflows.

2. **Session migration policy**
   - Migrate per repo via `cwd` from each session file header.
   - Do not bulk-copy all sessions to every repo state.

3. **Function-first subagent orchestration**
   - Continue using role functions (`scout`, `plan`, `implement`, `review`) rather than tool-locked assumptions.
   - Keep handoffs compressed and structured to reduce context spill.

## Why this artifact exists

User requested current context persistence into repository files, favoring Pi extension standards and reusable agents/commands workflows.
