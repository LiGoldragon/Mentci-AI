# Agent Report

- Chronography: `12.7.22.41 | 5919 AM`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Sema-Subagent-Flow`
- Title: `edn-aski-attractor-obsolescence`

## Prompt

research EDN as a basis for handling data which is good from LLM perspective, and programmatic and human-readability as a bonus - and therefore a better fit to work with graphs than dot graph notation: if attractor is used it's easy to translate the edn flow to a dot graph format to pass to attractor. Which brings attractor itself; do we even need it at this point? Exlore the code to get a grasp of its compatibility with mentci-ai and the author's intent - notably, does it expose the data-stream (messages from the agent doing work (what's the terminology for this?)) for users or agents to consume in real time? The subagent doesnt need to be owned if it's sequential, but it does if it's parallel (which is planned), it will need to go in a sub mentci-box which has a part of the tree writable from working on a different branch. Yes, the agents should converse in Aski (lojix is basically an aski dialect; aski is a principle on writing extremely concise code in ascii characters, essentially - inspired by shen and clojure (carp is also interesting mention)) - it is probably a good format for agents to use (the subject of the EDN research is linked, mention aski and that research together).

## Agent Answer

### 1. EDN vs. DOT: LLM Context and Graph Representation
**EDN (Extensible Data Notation)** provides an overwhelming advantage for LLM agents compared to **DOT graph notation**. 
*   **LLM Token Density:** DOT relies on verbose string definitions, redundant node declarations, and edge-linking syntax (`node_a -> node_b [label="..."]`). EDN uses native hierarchical data structures (maps `{}` and vectors `[]`) that perfectly represent DAGs (Directed Acyclic Graphs) with minimal syntax noise.
*   **Programmatic Introspection:** EDN can be ingested natively by Clojure and Rust (`edn-rs`) into strongly typed structs. DOT requires a dedicated lexer/parser and is inherently a *visual* representation language, not a programmatic state configuration language. 
*   **Translation:** Because EDN is highly structured, translating an EDN graph into a DOT graph for Attractor or Graphviz is a trivial, deterministic function. The "Truth" should live in EDN (or Lojix), and DOT should merely be an emitted artifact for visualizer tools.

### 2. Attractor Code Exploration and Compatibility
I explored the `Sources/attractor-docs/attractor/src/engine` codebase to analyze its compatibility with Mentci-AI's architecture:
*   **The Data-Stream (Terminology):** Attractor exposes its agent telemetry via `eventEmitter.emit()`, which broadcasts `PipelineEvent` objects. The terminology it uses includes `STAGE_STARTED`, `TOOL_HOOK_PRE`, `INTERVIEW_STARTED`, and `PIPELINE_COMPLETED`. In the broader agent observability space, this is generally referred to as an **Event Stream**, **Execution Trace**, or **Pipeline Telemetry**.
*   **Is Attractor Needed?** Architecturally, **No**. Attractor is built in Node.js/TypeScript and implements its own in-memory execution engine. However, the Mentci-AI mandate (`Core/ARCHITECTURAL_GUIDELINES.md`) requires **Rust Actor Orchestration**. More critically, Attractor operates within a single OS-level process and has zero native concept of `jj` (Jujutsu) branching or nested Nix `mentci-box` isolation. It is structurally incompatible with the "Russian Doll Model" and strict "State Separation" requirements of Mentci-AI. 

### 3. Subagent Ownership: Sequential vs. Parallel Branches
The principle of single-ownership dictates how subagents execute:
*   **Sequential Subagents:** If agents operate linearly (e.g., Planner -> Reviewer), they can share the same process owner and the same `jj` working-copy branch, as their state mutations are guaranteed not to race.
*   **Parallel Subagents:** If subagents branch out concurrently (e.g., multiple `@task` agents refactoring different modules), they **must be fully owned by a distinct sub-process**. In Mentci-AI, this translates to spawning a **"sub mentci-box"** (a nested, ephemeral Nix Jail) for each agent. Crucially, each sub-box must be assigned its own writable partition of the filesystem mapped to an isolated Jujutsu branch (`jj new -b subagent-task-A`). Only upon successful completion does the Master Process (Mentci-Aid) squash and merge these branches back into the main tree. This enforces true isolation and solves the double-ownership data race problem.

### 4. Aski & Lojix: Agent-to-Agent Conversation
The research validates that agents should not communicate via natural language. English (and other human languages) introduces hallucination vectors, ambiguity, and extreme token bloat. 
*   **The Aski Principle:** Aski is the principle of writing extremely concise, symbol-heavy code using minimal ASCII characters. Drawing deep inspiration from minimalist Lisp variants like **Shen**, **Clojure**, and the strongly-typed **Carp**, Aski enforces strict structural discipline.
*   **Lojix Dialect:** Lojix (EDN Next) acts as the specific data-serialization dialect of Aski. 
*   **Agent Communication:** When the `@planner` subagent hands off a task to the `@task` subagent, the payload should not be a Markdown checklist. It should be a concise, structured Lojix/EDN artifact. This ensures that the downstream agent receives a deterministic, strongly-typed programmatic state, maximizing context efficiency and strictly enforcing the Sema object boundaries.

### Conclusion & Recommendation
1.  **Obsolete Attractor:** We should formally deprecate `Goal 1` (Attractor DOT Job Handoff) in favor of a native Rust-based actor flow that consumes Lojix/EDN directly. If Attractor is maintained temporarily, it should solely be an output consumer (via a `lojix -> dot` compiler), not the orchestrator.
2.  **Lojix Internal Standard:** Fine-tune `pi` system prompts to force `@oh-my-pi/subagents` to communicate exclusively using Lojix/Aski S-expressions to save context window tokens and guarantee deterministic data routing.

## Reporting Protocol Notes

- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.