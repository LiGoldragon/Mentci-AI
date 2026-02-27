# Research: Voice Input/Output Interface for Pi and MCP

- **Solar:** 5919.12.9.49.50
- **Subject:** `Voice-Interface`
- **Status:** `design-draft`
- **Weight:** `High`

## 1. Intent
To design a fully conversational, hands-free interface for the `pi` agent harness, leveraging Pi extensions for lifecycle hooking, Contextualized STT for high-fidelity input, and Semantic TTS (with AST-parsing) for non-intrusive voice feedback.

## 2. Architecture Overview

A complete voice loop requires three major components:
1.  **Agent TTS (Text-to-Speech):** The agent speaks its questions or summaries.
2.  **The Handshake (Chime):** A non-verbal audio cue indicating the system has transitioned from speaking to listening.
3.  **Contextualized STT (Speech-to-Text):** The microphone activates, recording the user's response, heavily biased by the context of the agent's last question.

### 2.1 Pi Extension Hooks
The `pi` extension API provides the exact hooks needed to implement this without modifying the core harness:
*   `pi.on("agent_end", ...)`: Triggered when the agent finishes its turn. If the final message ends with a question mark (or is a specific `request_user_input` tool call), the extension intercepts it.
*   `pi.registerCommand("voice", ...)`: A manual slash command `/voice` or a custom keyboard shortcut (e.g., `Ctrl+Space`) to trigger recording.
*   `ctx.ui.inject()`: To push the transcribed text directly into the user's input buffer or submit it immediately.

## 3. The Conversational Loop (The "Chime" Flow)

1.  **Agent Questions:** The agent outputs text: *"I see the tests are failing. Should I revert the change or attempt a fix?"*
2.  **TTS Generation:** The Pi extension routes this text to a TTS engine (e.g., local Piper, macOS `say`, or a cloud API).
3.  **The Handshake:** The extension plays a short `chime.wav`.
4.  **Contextual STT:** The extension invokes `mentci-stt` (our Rust component). 
    *   **Crucial Step:** The extension passes the agent's question as the `basePrompt` or `vocabularyPreambleTemplate` in the Cap'n Proto `SttRequest`.
    *   Gemini STT now knows the user is answering a choice between "revert" and "fix", drastically reducing hallucinated transcriptions (e.g., avoiding hearing "river" instead of "revert").
5.  **Submission:** The STT result is injected into the Pi session and submitted, triggering the next agent turn.

## 4. MCP Voice Interface & Semantic Summarization

Blindly reading terminal output via TTS is a terrible UX (e.g., reading 100 lines of a Rust compiler error). We need **Semantic TTS**.

### 4.1 AST-Parsed Shell Summaries
Drawing inspiration from `vtcode`'s `tree-sitter-bash` parser:
1.  The agent calls the `bash` tool (e.g., `cargo build`).
2.  The command fails.
3.  The Pi extension hooks `pi.on("tool_result")`.
4.  Instead of reading the `stderr`, the extension uses tree-sitter (via `mentci-mcp` or a lightweight Rust WASM module) to parse the *input command*.
5.  It synthesizes a highly condensed vocal summary: *"Cargo build failed"* or *"Git commit successful"*.

### 4.2 Explicit MCP Voice Tools
Alternatively, the agent can be given an explicit `speak_to_user` MCP tool.
*   **Schema:** `speak(message: String, require_response: Bool)`
*   When the agent knows it is operating in a hands-free mode, it uses `speak()` to broadcast summaries and set `require_response = true` to automatically trigger the chime and microphone loop.

## 5. Implementation Roadmap

### Phase 1: Pi Extension Scaffold
- Create `extensions/voice-interface.ts`.
- Wire a hotkey to start/stop recording via `sox` or `rec`.
- Pass the audio file to `mentci-stt` and inject the resulting text into the Pi editor.

### Phase 2: Agent TTS & The Chime
- Add a TTS binary to the Nix `dev_shell` (e.g., `espeak-ng`, `say`, or `piper`).
- Hook `agent_end` to play the last message if it's classified as a question.
- Play the chime audio asset, then auto-trigger Phase 1.

### Phase 3: Contextual Injection
- Update the Pi extension to pull the `last_assistant_message` and pack it into the `SttRequest` Cap'n Proto envelope before calling `mentci-stt`.

## 6. Relevance to "Local Machine of Fit"
This system perfectly aligns with the project philosophy. The machine adapts to the human's physical state (hands-free, pacing, thinking out loud) rather than forcing the human to bend to the keyboard. By contextualizing the STT with the agent's own state, we create a closed-loop "psyche extension."
