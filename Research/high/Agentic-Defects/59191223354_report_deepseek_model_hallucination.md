# Report: Model Hallucination Defect in DeepSeek-70B via LiteLLM Gateway

## 1. Summary
The investigation into the defective behavior of the `ouranos-lite-gateway` concludes that the issue is **not a bug in the `pi` harness or the gateway's configuration**. The root cause is a **model-level hallucination** from the backend Large Language Model, which failed to correctly adhere to the provided tool-calling schema.

## 2. Evidence
A series of subagent explorations confirmed the following state:
1.  **Backend Model:** The `prometheus` node is configured to serve `DeepSeek-R1-Distill-Llama-70B-Q8_0.gguf` via a `llama.cpp` server, not `ollama`.
2.  **Gateway Configuration:** The user's local `~/.pi/agent/models.json` correctly configures the `ouranos-lite-gateway` provider with `"api": "openai-completions"`. Source code analysis confirms this maps to the `client.chat.completions.create` endpoint, which is the modern, tool-capable chat completions API.
3.  **Hallucinated Schema:** A thorough search of the entire repository confirms that the JSON keys `agent_skills_used` and `subagent_tasks` **do not exist** in any prompt template, skill, or source file. The JSON response provided by the model was a complete fabrication.

## 3. Root Cause Analysis
The failure occurred due to a confluence of factors related to model capability and the nature of the prompt:

1.  **Meta-Instruction Prompt:** The user provided a high-level, meta-instructional prompt: `"Do not edit anything - use subagents to do a shallow survey..."`.
2.  **Tool Schema Presentation:** The `pi` harness correctly received this prompt and formulated a request to the model, including the XML-formatted schema for the available `task` tool, which is used to invoke subagents.
3.  **Model Response Failure:** The request was correctly routed through the LiteLLM gateway to the `DeepSeek-70B` model on the `prometheus` node. However, this model, while a powerful instruction-follower, is not as robustly fine-tuned for the specific XML tool-calling syntax used by `pi` as other models (e.g., OpenAI's GPT series or Google's Gemini).
4.  **Intent vs. Execution:** The model correctly parsed the *intent* of the prompt (the user wants a survey performed by subagents). But instead of generating a valid XML tool call to the `task` tool, it "thought aloud" by generating a JSON object that *described its plan*. It hallucinated a schema that it thought represented the subagent tasks it was being asked to perform, but it never actually called the tool.

This is a classic failure mode for LLMs that have strong general reasoning but lack the specific, rigid fine-tuning required for reliable tool adhesion.

## 4. Proposed Next Steps
To mitigate this and confirm the hypothesis, the following strategies are recommended:

*   **Strategy 1 (Validate with Simple Prompt):** Test the `ouranos-lite-gateway` again with a simple, non-meta prompt that requires a tool, such as `"List the files in the .pi/skills/ directory."` This will determine if the model is capable of performing *any* tool call, or if it fails on all of them.
*   **Strategy 2 (Inspect `llama.cpp` Logs):** If access is available, inspect the `llama.cpp` server logs on the `prometheus` node. This would reveal the exact prompt payload it received from LiteLLM and show how the tool schema was presented, which could be invaluable for debugging.
*   **Strategy 3 (Prompt Template Adjustment):** A common solution for this issue is to create a provider-specific prompt template for the `ouranos-lite-gateway`. This template could be modified to include explicit, few-shot examples of `pi`'s XML tool-call format within the system prompt. This often provides enough guidance to steer a less-capable model towards the correct output format.
