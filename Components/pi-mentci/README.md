# `pi-mentci` Extension

This directory contains the `@oh-my-pi/pi-mentci` extension (proposed).

## Architecture
As outlined in `Library/documentation/Pi_Subagent_Mechanics.md`, this extension aims to implement Mentci-AI's specific LLM lifecycle hooks:

1. **Pre-Hook (Context Injection):**
   - Automatically executes `chronos --format am --precision second` to gather the correct Anno Mundi / Solar Time coordinates.
   - Prepends solar telemetry to the system context window of every subagent or prompt call.

2. **Post-Hook (Auditability & Commit Formatting):**
   - Detects when an agent edits structural files via `mentci-mcp`.
   - Constructs a standardized Jujutsu commit message (e.g., `intent: ...`).
   - Executes `jj commit` with the exact Model Name, Timestamp, and Context provided by Pi.
