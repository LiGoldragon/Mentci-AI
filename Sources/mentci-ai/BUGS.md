# Mentci-AI: Bug Documentation

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `docs/architecture/ASKI_POSITIONING.md`.

## Core Issues

### B01: Context Overflow / Session Exhaustion (Gemini CLI)
- **Description:** During long-running agentic sessions, the Gemini model may become unresponsive or unable to execute valid shell commands (e.g., `ls`, `jj status`). This typically manifests as middleware parsing errors or general tool-call failure.
- **Root Cause:** Excessive context accumulation leading to "attention drift" or hitting token limits, which disrupts the parsing of structured tool outputs or the generation of precise shell commands.
- **Resolution:** A manual **Context Restart** is required.
    - **Procedure:**
        1. Capture current progress in `RESTART_CONTEXT.md`.
        2. Terminate the active session.
        3. Start a new session and "pickup context from @RESTART_CONTEXT.md".
- **Status:** **Mitigated via Process.** (Ongoing monitoring of model behavior for performance degradation).

---
*Mentci-AI Level 5 Bug Tracking*
