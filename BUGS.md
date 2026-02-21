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
- **Description:** The `chronos` tool (zodiac-ordinal) was found to be out-of-sync with industry standards like Solar Fire and astro-charts.com.
- **Root Cause:** 
    1. **Low-Precision Algorithm:** The implementation used a simplified solar longitude formula that excluded major planetary perturbations, leading to real inaccuracies (approx. $1'$) even after accounting for notation differences.
- **Expertise Note:** **Ordinal Shift** (1-based vs 0-based) was initially suspected but confirmed **not** to be the cause of the reported discrepancy; Li Goldragon is an expert at automatic mental ordinal-shift when consulting ephemeris. The observed error was purely algorithmic.
- **Resolution:** 
    1. Improved the solar algorithm by including major planetary perturbation terms (Venus, Jupiter, Moon).
    2. Added `--notation standard` flag to use 0-based astrological notation for easier external verification (default remains `ordinal` for Mentci-internal logic).
- **Status:** **Fixed.** (Resolution applied Feb 21, 2026).

---
*Mentci-AI Level 5 Bug Tracking*
