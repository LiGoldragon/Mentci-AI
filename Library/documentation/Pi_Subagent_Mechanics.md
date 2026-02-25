# Pi Subagent Mechanics

## How Subagents and Tools Work Under the Hood
To effectively utilize the `@oh-my-pi/subagents` extension and tool-calling flows in Mentci-AI, users and the master agent must understand the underlying token economy and LLM lifecycle.

### The Tool Calling Lifecycle
When the primary Pi agent decides to use a tool (like `read`, `bash`, `edit`, or an MCP tool):
1.  **Halt & Call:** The agent halts its token generation and outputs a JSON-formatted tool-call request.
2.  **Execution:** The local Pi runtime (TypeScript) executes the tool on the local machine (e.g., runs the bash command, reads the file).
3.  **Context Re-Injection:** The output of that tool is appended to the conversation history as a "Tool Response".
4.  **Re-Prompt:** The entire conversation history—including the new tool response—is sent *back* to the LLM API in a completely new request. The LLM does not "pause and wait" on the server; the connection is stateless. 

### Subagent Spawning (`@planner`, `@task`, etc.)
When the primary agent invokes a subagent via the `@` mention:
1.  **New Prompt Chain:** Pi intercepts the command and instantiates an entirely new context window for the requested subagent.
2.  **System Prompt Override:** The subagent is loaded with its own specific system prompt (e.g., the `@planner` subagent is instructed only to plan, not to write code).
3.  **Handoff:** The user's request (or the master agent's payload) is passed to this new subagent.
4.  **Return:** Once the subagent completes its goal, its final output is returned to the original primary agent's context window as a tool response.

### Caching and Token Costs
Because each tool call and subagent handoff requires a brand new HTTP request to the LLM API, the context window is constantly being re-uploaded. 
*   **Prompt Caching:** Providers like Google Gemini natively support Context Caching. If the system prompt and early conversation history remain static, the API caches those tokens, drastically reducing the cost and latency of subsequent tool-call roundtrips.
*   **Context Compaction:** Pi features a compaction setting (`.pi/extensions.edn`) that prunes older messages from the history once the context limit is approached, preserving the system prompt and the most recent interactions to keep the agent focused and costs bounded.
### Pi Pre-Hooks and Post-Hooks (Proposed Architecture)
To further automate context gathering and auditability, Mentci-AI requires specialized prompt hooks within the Pi environment:
1.  **Pre-Hook (Context Injection):** An executable hook that runs *before* the agent's prompt is sent to the LLM. 
    *   *Example:* Running `chronos` to fetch the current Solar Time (Chronography) and appending it to the system prompt. This gives the agent deep awareness of the astrological/chronological context (e.g., Anno Mundi 5919, Raddii, Zodiac degrees) which is essential for understanding human intent as per the Book of Sol.
2.  **Post-Hook (Audit & Synthesis):** An executable hook that runs *after* the agent finishes editing files.
    *   *Example:* Automatically staging changes via `jj` and pre-generating a structured commit message. The hook would include the model name and number that ran the session, the chronological timestamp, and a summary of the intent-driven changes.
    *   *Rationale:* This creates a durable trace of intent for every file modification without relying on the agent to manually format and run the `jj commit` command every time.
