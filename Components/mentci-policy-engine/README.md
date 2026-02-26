# mentci-policy-engine

Rust policy core for workflow guardrails inspired by superpowers.

## Industry Terminology
- **Policy Engine**: deterministic rule evaluation core.
- **Admission Control**: pre-execution decision on whether tool calls are allowed.
- **Decision Output**: `allow` / `warn` / `block` with a machine-readable reason code.

## Scope
- Evaluate tool-call intent against workflow phase.
- Enforce write constraints during `brainstorm` and `plan` phases.
- Provide stable decision outputs for UI/TUI adapters.

## Out of Scope
- No UI rendering.
- No subprocess spawning.
- No persistence backend.

This component is meant to be consumed by `mentci-mcp-gateway` and any local extension adapters.
