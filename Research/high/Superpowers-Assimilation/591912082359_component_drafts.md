# Component Drafts from Superpowers Assimilation

## Draft 1: `mentci-policy-engine` (Rust lib)

### Role
Deterministic policy core that evaluates tool calls against workflow phase and returns:
- `allow`
- `warn`
- `block`

### Input Contract (target)
- workflow phase
- tool name
- target path
- optional artifact references

### Output Contract (target)
- decision kind
- reason code
- human explanation

---

## Draft 2: `mentci-mcp-gateway` (Rust bin)

### Role
Policy-aware **MCP Gateway** that sits between host and tool providers.

### Responsibilities
1. Receive tool-call envelopes.
2. Invoke `mentci-policy-engine` admission decision.
3. Route allowed calls to MCP server/client transport.
4. Emit structured events for audit/review.

---

## Draft 3 (next): `mentci-review-orchestrator` (Rust bin/lib)

### Role
Post-edit reviewer orchestration service.

### Responsibilities
1. Spawn review agents/subprocesses.
2. Evaluate diffs against policy/spec constraints.
3. Return verdict envelope to gateway.

---

## Schema Draft Added
- `Components/schema/mentci_mcp_gateway.capnp`
  - tool call envelope
  - policy decision
  - review request/response

## Note
These are intentionally draft-level scaffolds: stable naming, boundaries, and message contracts first; full runtime wiring follows.
