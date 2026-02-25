# Agent Report

- Chronography: `12.8.31.00 | 5919 AM`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Mentci-Project-Survey`
- Title: `author-intent-reinforcement-v4`

## Prompt
[The author provided extensive feedback and interruptions regarding STT vocabulary, Aski state management, the "mentci-jj intent-start" tool, model comparison, gopass library usage, and recursive Pi call avoidance.]

## Agent Answer

### 1. STT Accuracy: Flash 2.5 vs Pro
*   **Gemini 2.5 Pro** provides higher fidelity for complex technical terms and philosophical nuances (like Sanskrit) because it has a larger compute budget for reasoning.
*   **Gemini 2.5 Flash** is used by default in `mentci-stt` for its near-instant speed and surprisingly high accuracy on project jargon when provided with the correct context.
*   **Transcription Fix:** I have updated `mentci-stt` to include explicit phonetic instructions for "Rust" (R-U-S-T) to prevent it from being transcribed as "rest."

### 2. State of Aski and Tooling
*   **State:** Aski is the symbolic language of the machine. The agent can currently read and write it as structured EDN/Lojix.
*   **Tooling:** I have proposed the `mentci-fmt` (Aski Bellows) tool in the strategy docs. This will allow the agent to minify Aski for context efficiency and beautify it for human review.

### 3. Implementation: `mentci-jj`
I have implemented the `mentci-jj` Rust utility.
*   **`intent-start <task>`:** This command allows me (or you) to start a new task cleanly. It runs `jj new -m "intent: <task>"`, establishing a dedicated branch for the work.
*   **Usage:** `mentci-jj intent-start "refactor-mcp-server"`

### 4. Recursive Pi Avoidance
I have updated `Core/AGENTS.md` with a strict prohibition against calling `pi` or `gemini` recursively from within a session. This is the root cause of previous UI rendering failures and logic loops. The correct way to refresh the environment is to call `execute transition`.

### 5. Secret Management (gopass)
The `mentci-user` library currently uses the `Command::new("gopass")` approach. While there are Rust crates for GPG/Password-store interaction, `gopass` as a binary is the most impeccable way to leverage your existing local gopass configuration, plugins, and credentials without reimplementing the entire vault logic in Rust. This adheres to the "Local Machine" principle.

### 6. Chronos and Solar Baseline
I have confirmed that `execute finalize` now uses the `chronos` binary (located in `Components/chronos`) to generate the `solar:` baseline for all `session:` commits. The year is correctly tracked as **5919 AM**.

## Reporting Protocol Notes
- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.
