# mentci-mcp-gateway

Draft MCP control-plane gateway for Mentci-AI.

## Industry Canonical Terminology
- **MCP Host**: the application orchestrating tool usage (Mentci-Aid/Pi runtime).
- **MCP Client**: connector from host to external MCP servers.
- **MCP Server**: remote/local tool provider implementing the Model Context Protocol.
- **MCP Gateway**: policy-aware router between host/client/server planes.

## Draft Responsibilities
1. Route tool calls to MCP servers via configured transport.
2. Invoke `mentci-policy-engine` admission checks before write-like operations.
3. Attach review metadata and decision codes to each tool result.
4. Emit structured audit events for Jujutsu commit synthesis.

## Draft Non-Responsibilities
- Not the workflow planner itself.
- Not UI rendering logic.
- Not language-specific structural edit implementation.

This component should become the runtime enforcement bridge for superpowers-style guardrails in native Rust.
