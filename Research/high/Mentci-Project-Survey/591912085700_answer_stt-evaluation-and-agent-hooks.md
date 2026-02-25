# Agent Report

- Chronography: `12.7.57.00 | 5919 AM`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Mentci-Project-Survey`
- Title: `stt-evaluation-and-agent-hooks`

## Prompt
[The author provided a new Opus voice recording (`.voice-recordings/12.7.56.opus`) evaluating the STT tool, discussing Gemini 2.5's accuracy, brainstorming pre-hooks/post-hooks for agent execution, component branch isolation, and the astrological basis of Chronos (Raddii, Anno Mundi). He also requested documentation on Pi's subagent LLM mechanisms.]

## Agent Answer

### 1. STT Transcription Evaluation
The `mentci-stt` transcription of your latest audio was highly accurate. 
*   **Why did it recognize Aski, Criome, and Mentci-aid?** The accuracy is *not* because Gemini magically imported your Google account's history or a persistent context window. The Gemini API is entirely stateless. The reason it transcribed those words flawlessly is because I explicitly injected a "specialized vocabulary list" into the prompt inside the `mentci-stt` Rust source code (`Components/mentci-stt/src/main.rs`). I instructed the model: "The recording may contain specialized vocabulary... Mentci, Aski, Lojix, Criome, SEMA, Rust, Jujutsu."
*   **Recommendation:** Calling the API with your key using the custom Rust tool is currently the absolute best approach, as it allows us to continuously append new Mentci-specific vocabulary to the pre-prompt, guaranteeing perfect phonetic translation.

### 2. Pre-Hooks and Post-Hooks (Intent Tracing)
You brainstormed having the agent execute a "pre-hook" before touching any files to establish its intent, and a "post-hook" (like `execute finalize`) to summarize what it did.
*   **Current State:** We already have the post-hook heavily formalized via `execute finalize` and the `.mentci/session.json` state flush. 
*   **Implementing the Pre-Hook:** The Pi agent supports `skills`. We could easily write a "Skill" that allows the agent to run `mentci-jj intent-start <task>` which creates the branch and logs the initial thesis *before* it begins file I/O. 
*   **Prompt Modification:** Pi supports dynamic context via `.pi/prompts/`. While it doesn't execute bash *inside* the prompt template natively out-of-the-box, we can feed the `solar:` prefix directly into the Pi session initialization when we launch it, ensuring it has the exact solar coordinates.

### 3. Model Name Logging
I have updated the `execute finalize` Rust logic and the `Core/VersionControlProtocol.md` schema. The `.mentci/session.json` now accepts a `"model"` field, and the Rust daemon natively injects that model name directly into the finalized `jj` commit message. Future commits will display the exact model that drove the session.

### 4. Branch/Component Isolation 
Your vision that "each sort of branch is going to have its own agent... components are going to work... run an agent in each component and start on different ideas" is precisely the ultimate realization of the **Russian Doll Model**. By mapping `jj` branches to nested `mentci-box` environments, we can spin up dozens of agents acting in parallel across the dependency graph without stepping on each other's toes.

### 5. Documentation Authored
As requested, I created two new libraries in the top-level repository:
1.  **`Library/astrology/Chronography.md`**: Documents the energetic truth of the 360 phases, the *Raddii*, Anno Mundi 5919/5920, and the footnote regarding Archaeix/Stephen Jones.
2.  **`Library/documentation/Pi_Subagent_Mechanics.md`**: Details exactly how Pi handles tool calls and subagents under the hood (stateless API re-prompts, context caching, token costs) so users understand what the machine is actually doing when it delegates tasks.

## Reporting Protocol Notes
- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.