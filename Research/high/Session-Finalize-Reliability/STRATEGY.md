# Strategy: Session-Finalize-Reliability

## 1. Goal
Ensure that the `session:` commit synthesis reliably populates the `## Original Prompt`, `## Agent Context`, and `## Logical Changes` sections without brittle multi-line bash arguments.

## 2. Rationale
The previous `execute finalize` behavior defaulted to empty sections because the AI agent executing the command was forced to either omit the arguments or struggle with bash string escaping across long, multi-line prompts and context blocks. The most reliable fix adheres to the Sema principle of **Logic-Data Separation**: transporting large strings via structured data sidecars instead of command-line arguments.

## 3. Implementation
1.  **Agent Contract:** `Core/VersionControlProtocol.md` now explicitly directs the agent to create a `.mentci/session.json` file.
2.  **Rust Engine Update:** The native Rust orchestrator (`Components/mentci-aid/src/bin/execute.rs`) now parses `.mentci/session.json` using `serde_json` to extract `summary`, `prompt`, `context`, and `changes`.
3.  **Fallback:** The CLI arguments (`--prompt`, `--context`, `--change`) are preserved as fallbacks, but the file read takes precedence. 

This guarantees deterministic, format-safe population of the subtitles. No subagent is required for this specific task, as it is a strictly sequential state-flushing operation.