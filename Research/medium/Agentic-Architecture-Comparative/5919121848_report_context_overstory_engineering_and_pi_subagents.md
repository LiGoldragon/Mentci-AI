# Context Persistence Report: Overstory Engineering + Pi Sub-Agent Landscape

## Scope
This artifact persists the latest session context requested by the operator, with focus on:
1. Overstory engineering quality
2. Pi sub-agent capabilities and extension ecosystem

## A) Overstory engineering assessment (current confidence)

### Evidence basis
- Upstream repository metadata and docs (`README`, `CLAUDE.md`, `package.json`, `tsconfig.json`, `biome.json`, CI workflow)
- Sample source audits (`errors.ts`, `worktree/manager.ts`, `mail/store.ts`)
- Test surface inspection (`*.test.ts` volume and distribution)

### Findings
- **Strong engineering hygiene** for its domain:
  - strict TypeScript config (`strict`, `noUncheckedIndexedAccess`)
  - CI with lint + typecheck + tests
  - explicit error taxonomy with typed domain errors
  - modular architecture boundaries (runtime adapters, worktree manager, mail store, merge resolver, watchdog)
  - high test density
- **Operationally mature patterns**:
  - isolated git worktrees per agent
  - SQLite (WAL) for typed inter-agent coordination
  - tiered supervision/watchdog pattern
  - explicit observability commands

### Tradeoffs/risk notes
- Several large modules (1k+ LOC) indicate maintainability pressure if growth continues.
- Architectural complexity is meaningful; robust but not lightweight.
- Design is optimized for git/tmux workflows and needs adaptation for Mentci’s jj-first + Rust actor model.

## B) Pi sub-agent situation

### Core pi
- Core pi is intentionally minimal and **does not ship sub-agents as a mandatory built-in behavior**.
- Sub-agent behavior is commonly implemented via extensions/packages.

### Active extension ecosystem (validated)
- `pi-subagents` (`nicobailon/pi-subagents`): chain/parallel/background flows, agent manager UI, depth guards.
- `@oh-my-pi/subagents` (from `oh-my-pi`): role-oriented task delegation package.
- `pi-parallel-agents`: dynamic parallel orchestration with per-agent model choices.
- Additional variants exist (`pi-side-agents`, `pi-collaborating-agents`, `pi-subagent` forks).

### Synthesis
- Pi’s strategy: minimal core + extension composability.
- Sub-agent capability in practice: available and evolving, but quality varies by extension and operational discipline.

## Relevance to Mentci
- Reusable motifs:
  1. runtime adapter boundary
  2. typed async coordination protocol
  3. tiered supervision and diagnostics
- Recommended adaptation path:
  - implement as Rust actors + schema contracts (Cap’n Proto/EDN), not direct TS copy.
  - preserve isolation model while mapping orchestration to jj-native flow.

## Source pointers
- Overstory: `https://github.com/jayminwest/overstory`
- Pi core: `https://github.com/badlogic/pi-mono/tree/main/packages/coding-agent`
- Pi ecosystem references discovered in web scan:
  - `https://github.com/nicobailon/pi-subagents`
  - `https://github.com/can1357/oh-my-pi`
  - `https://github.com/messense/pi-parallel-agents`

## Confidence
- Overstory engineering quality: **medium-high confidence**.
- Pi sub-agent ecosystem mapping: **high confidence on existence and direction, medium confidence on per-extension implementation quality without deep code audits of each package**.
