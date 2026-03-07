# Agentic Architecture Scan: jCodeMunch, Overstory, OpenClaw

## Scope
Research requested: compare architecture patterns in three external agentic-programming projects and identify what is reusable for Mentci-AI.

## Evidence Base
- `jgravelle/jcodemunch-mcp` (README + repo metadata)
- `jayminwest/overstory` (README + CLAUDE.md + repo metadata)
- `openclaw/openclaw` (README + repo metadata)

## Snapshot (as observed)
| Project | Primary purpose | Core stack | License | Signal of adoption |
|---|---|---|---|---|
| jCodeMunch MCP | Symbol-level code retrieval MCP server | Python + tree-sitter + local index | Dual-use (free non-commercial, paid commercial) | ~700 stars |
| Overstory | Multi-agent coding swarm orchestrator | TypeScript/Bun + tmux + git worktrees + SQLite | MIT | ~748 stars |
| OpenClaw | Personal AI assistant + gateway/control plane across channels | TypeScript/Node + WS gateway + Pi runtime + channel integrations | MIT | very high adoption (~273k stars) |

---

## 1) jCodeMunch architecture (what matters)
### Core shape
- Pre-index repository once, then retrieve exact symbols by stable IDs.
- Uses tree-sitter parse + byte offsets for O(1)-style source slicing.
- Exposes MCP tool surface (`search_symbols`, `get_symbol`, outlines, text fallback).

### Strong pattern
- **Token-economy architecture by design**: retrieval-first, not file-dump-first.
- Stable symbol IDs (`path::qualified_name#kind`) give deterministic references across sessions.

### Constraints
- Commercial usage requires paid license (adoption/legal gate for production).
- Implemented in Python; Mentci policy favors Rust for logic.

### Mentci reuse potential
- **High conceptual fit** for `mentci-search`/logical retrieval.
- Reimplement pattern in Rust as schema-defined symbol index + typed query API.

---

## 2) Overstory architecture (what matters)
### Core shape
- Multi-agent orchestration centered on:
  1) isolated git worktrees per agent,
  2) tmux-backed worker processes,
  3) SQLite mailbox for typed inter-agent messaging,
  4) merge queue with conflict tiers,
  5) tiered watchdog (mechanical + AI triage + monitor agent).
- Runtime abstraction via `AgentRuntime` adapters (Claude, Pi, Codex, Gemini, etc.).

### Strong patterns
- **Runtime adapter boundary** separates orchestration logic from vendor-specific launch/guards.
- **Typed mailbox** enables asynchronous, many-agent coordination without prompt coupling.
- **Tiered supervision** gives operational resilience in swarm mode.

### Constraints
- Built around TypeScript/Bun + tmux process model, not actor-supervised Rust.
- Git-worktree strategy differs from Mentci’s jj-first worldview.

### Mentci reuse potential
- **High structural fit**:
  - adapter interface concept -> Rust trait + component-local contracts,
  - typed mail protocol -> Cap’n Proto messages over actor channels,
  - tiered supervision -> ractor supervision tree.
- **Medium fit** for worktree mechanics (Mentci should map this into jj-native flow).

---

## 3) OpenClaw architecture (what matters)
### Core shape
- Gateway-centered platform: one control plane coordinating channels, sessions, tools, and clients.
- Multi-channel ingress (chat apps), with per-session and optional per-agent routing/sandboxing.
- Pi runtime integration and broad tool surface (browser/canvas/nodes/cron/skills).

### Strong patterns
- **Unified gateway control plane** with explicit session model and observability surfaces.
- **Security posture surfaced as config defaults** (pairing/allowlists, policy toggles).
- **Productized operator flow** (onboarding, doctor, update channels, docs depth).

### Constraints
- Much broader product scope than Mentci’s core (assistant platform vs architecture kernel).
- Heavy integration surface could distract from Mentci’s component/contract purity if copied wholesale.

### Mentci reuse potential
- **Medium-high fit** on:
  - control-plane thinking,
  - session isolation boundaries,
  - operational tooling discipline (doctor/onboard/update).
- **Lower fit** on omnichannel product breadth.

---

## Cross-project synthesis for Mentci
## Reusable architecture motifs (ranked)
1. **Symbol-first retrieval layer** (from jCodeMunch) — highest immediate ROI for agent efficiency.
2. **Runtime adapter contract** (from Overstory) — clean pluggability across Pi/Codex/Gemini/OpenCode.
3. **Typed async coordination bus** (Overstory mail concept, but Cap’n Proto + actors).
4. **Tiered supervision/health loops** (Overstory watchdog + OpenClaw ops mindset).
5. **Operator-grade lifecycle commands** (OpenClaw onboarding/doctor-style reliability UX).

## Likely anti-patterns for Mentci
- Adopting Python/TS runtime logic in core orchestration.
- Using untyped ad-hoc message payloads instead of schema-first contracts.
- Porting large product surfaces before core symbolic substrate is stable.

## Proposed Mentci experiment sequence
1. **Experiment A: Rust Symbol Index Actor**
   - Build local index actor (tree-sitter + stable symbol IDs + byte spans).
   - Expose typed query contract (Cap’n Proto).
2. **Experiment B: Runtime Adapter Trait**
   - Define `AgentRuntime` trait in Rust with strict single-object I/O messages.
   - Implement one adapter first (Pi), then second (Codex/Gemini).
3. **Experiment C: Coordination Bus + Supervision**
   - Model typed coordination messages (`dispatch`, `done`, `escalate`, `merge_ready`, etc.).
   - Implement supervised actor tree for orchestration health.
4. **Experiment D: Operator Reliability Surface**
   - Minimal `execute doctor` / `execute onboard` parity for Mentci workflows.

---

## Sources
- jCodeMunch
  - https://github.com/jgravelle/jcodemunch-mcp
  - https://raw.githubusercontent.com/jgravelle/jcodemunch-mcp/main/README.md
  - https://api.github.com/repos/jgravelle/jcodemunch-mcp
- Overstory
  - https://github.com/jayminwest/overstory
  - https://raw.githubusercontent.com/jayminwest/overstory/main/README.md
  - https://raw.githubusercontent.com/jayminwest/overstory/main/CLAUDE.md
  - https://api.github.com/repos/jayminwest/overstory
- OpenClaw
  - https://github.com/openclaw/openclaw
  - https://raw.githubusercontent.com/openclaw/openclaw/main/README.md
  - https://api.github.com/repos/openclaw/openclaw

## Notes on confidence
- Confidence is high on macro-architecture patterns (well-documented in upstream READMEs/docs).
- Confidence is medium on internal implementation quality tradeoffs (no full source audit performed in this pass).
