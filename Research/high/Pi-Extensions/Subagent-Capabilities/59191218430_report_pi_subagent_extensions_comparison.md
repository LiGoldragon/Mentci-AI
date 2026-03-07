# Research Report: Pi Sub-Agent Extensions Comparison

**Solar Time:** 59191218430  
**Scope:** Compare major Pi sub-agent extensions by size, activity, high-level differences, and focus.

## 1) Compared extensions

1. `nicobailon/pi-subagents`
2. `@mjakl/pi-subagent`
3. `pasky/pi-side-agents`
4. `messense/pi-parallel-agents`
5. `@baochunli/pi-collaborating-agents`
6. `@oh-my-pi/subagents` (OMP package)
7. `cmf/pi-subagent` (SDK-style utility)

## 2) Size + activity snapshot

| Extension | Repo size (KB) | npm unpacked size | Activity snapshot |
|---|---:|---:|---|
| `pi-subagents` | 1532 | 485,168 B | 367★, 41 forks, 84 commits, 20 releases, ~810 weekly npm downloads |
| `@mjakl/pi-subagent` | 135 | 76,112 B | 14★, 2 forks, 42 commits, recent publish `1.2.0`, ~153 weekly npm downloads |
| `pi-side-agents` | 182 | 109,088 B | 88★, 3 forks, 83 commits, 2 releases, ~233 weekly npm downloads |
| `pi-parallel-agents` | 204 | 129,559 B | 2★, 3 forks, 21 commits, 3 tags, ~29 weekly npm downloads |
| `@baochunli/pi-collaborating-agents` | 222 | 311,819 B | 1★, 1 fork, 22 commits, release `0.3.1`, ~31 weekly npm downloads |
| `@oh-my-pi/subagents` | (inside OMP mono-repo) | 64,691 B | package `1.3.3710`, ~144 weekly npm downloads |
| `cmf/pi-subagent` | 36 | not observed as active npm package | 0★, 2 commits, low recent activity |

## 3) High-level differences and focus

### A. `pi-subagents` (nicobailon)
- **Focus:** Most feature-rich general-purpose subagent framework.
- **Strengths:** chain + parallel + async/background runs, TUI agent manager, chain files, skill injection, depth guard, strong testing/CI signals.
- **Tradeoff:** largest/most complex surface area.

### B. `@mjakl/pi-subagent`
- **Focus:** minimalist delegation.
- **Strengths:** simple operation, explicit `spawn` vs `fork` context mode, cycle/depth guards.
- **Tradeoff:** intentionally fewer orchestration features.

### C. `pi-side-agents` (pasky)
- **Focus:** async coding workflow with operational isolation.
- **Strengths:** tmux + git worktree lifecycle automation; `/agent` + `/agents`; optimized for many concurrent coding tasks with explicit review/merge loop.
- **Tradeoff:** opinionated workflow and external dependencies (tmux/worktrees).

### D. `pi-parallel-agents` (messense)
- **Focus:** model-oriented orchestration experiments.
- **Strengths:** multiple modes (single/parallel/chain/race/team DAG), per-task model choices, iterative reviewer loops.
- **Tradeoff:** lower adoption/activity so far.

### E. `@baochunli/pi-collaborating-agents`
- **Focus:** multi-agent coordination semantics.
- **Strengths:** inter-agent messaging, reservation-based edit coordination, agent feed/chat overlay, default typed subagent roles.
- **Tradeoff:** niche design; smaller ecosystem traction.

### F. `@oh-my-pi/subagents`
- **Focus:** OMP batteries-included task delegation.
- **Strengths:** built-in role agents (`task`, `planner`, `explore`, `reviewer`, `browser`) and command workflows.
- **Tradeoff:** best fit when already aligned with OMP fork/ecosystem, not plain minimal Pi.

### G. `cmf/pi-subagent`
- **Focus:** extension author toolkit (library) rather than end-user turnkey package.
- **Strengths:** composable recursive chain/parallel primitives with UI helpers.
- **Tradeoff:** low activity and thinner operational hardening signals.

## 4) Practical grouping

- **General-purpose production candidate:** `pi-subagents`
- **Minimal and predictable:** `@mjakl/pi-subagent`
- **Parallel coding operations (branch/worktree discipline):** `pi-side-agents`
- **Model orchestration experimentation:** `pi-parallel-agents`
- **Agent-to-agent communication patterns:** `@baochunli/pi-collaborating-agents`
- **OMP-native workflow package:** `@oh-my-pi/subagents`
- **Builder SDK:** `cmf/pi-subagent`

## 5) Data sources used

- GitHub repository metadata APIs (stars, forks, commits, releases, contributors, size, last push/update)
- npm registry metadata (latest version, unpacked size, modified time)
- npm weekly download counts (last-week endpoint)
- Project READMEs / package pages for feature focus and capabilities

## 6) Caveats

- Metrics are time-sensitive and may shift quickly.
- Stars/downloads are adoption signals, not direct quality guarantees.
- “Repo size” is GitHub repository metadata and not a direct proxy for runtime footprint.
