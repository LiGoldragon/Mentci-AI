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

### B02: chronos Tool Accuracy and Notation Confusion
- **Description:** The `chronos` tool (zodiac-ordinal) is reported to be out-of-sync with industry standards like Solar Fire and astro-charts.com.
- **Root Cause:** 
    1. **Ordinal Shift:** The tool uses 1-based ordinals for degrees and minutes (e.g., `1°` means $0^\circ \le \lambda < 1^\circ$), which differs from standard astrological notation (where `0°` is the first degree). This causes a perceived $1^\circ$ and $1'$ offset.
    2. **Low-Precision Algorithm:** The current implementation uses a simplified solar longitude formula that excludes planetary perturbations, leading to small errors (approx. $1'$).
- **Resolution:** 
    1. Added `--notation standard` flag to use 0-based astrological notation (default remains `ordinal` for Mentci-internal logic).
    2. Improved the solar algorithm by including major planetary perturbation terms (Venus, Jupiter, Moon).
- **Status:** **Fixed.** (Resolution applied Feb 21, 2026).

---
*Mentci-AI Level 5 Bug Tracking*
