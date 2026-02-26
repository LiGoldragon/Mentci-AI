# Superpowers Assimilation Research (Mentci)

## Source Mount
- Mounted external source as:
  - `Sources/superpowers -> /home/li/git/superpowers`

## Upstream Snapshot
- Repo: `obra/superpowers`
- Primary author: Jesse Vincent (`obra`)
- Public stats at time of analysis: ~61.8k stars, ~4.7k forks, ~286 commits.

## What to Assimilate vs. Keep External

### Assimilate into Mentci Rust Core
1. **Workflow State Machine**
   - Phase progression logic (`brainstorm`, `plan`, `execute`, `verify`, `review`, `finish`).
   - Deterministic transition model.
2. **Admission Policy Engine**
   - Pre-execution guardrails (`allow/warn/block`) for write-like calls.
3. **Completion Gate Logic**
   - Verify/review gating before finish actions.
4. **Review Trigger Contracts**
   - Structured request/response messages for post-edit review.

### Keep External / Reference-Only
1. Narrative skill prose (`SKILL.md` text style and pedagogy).
2. Platform-specific plugin wrappers (Claude/Cursor/Codex/OpenCode glue).

## Canonical Industry Terms (for Mentci)
- **MCP Host**: Mentci-Aid / Pi runtime orchestrator.
- **MCP Client**: connector initiating calls to MCP servers.
- **MCP Server**: tool provider endpoint.
- **MCP Gateway**: policy-aware routing bridge between host/client/server.
- **Policy Engine / Admission Controller**: deterministic permit/deny layer.

## Assimilation Principle
- Move runtime policy and state to Rust + Cap'n Proto messages.
- Keep UI adapters thin and replaceable.
