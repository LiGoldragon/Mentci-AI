# Agent Report

- Chronography: `12.7.23.30 | 5919 AM`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Sema-Subagent-Flow`
- Title: `sema-subagent-architecture-flow`

## Prompt

research and implement a subagent flow based on the intent of the author and the sema style architecture, also documented in the book of goldragon. get large context from files. specifically, the pi subagent extension that was just documented. document any suggestions and questions for the author

## Agent Answer

### 1. Research Synthesis: The Author's Intent & Sema Architecture

By analyzing the context from *The Book of Goldragon* (`Sources/TheBookOfGoldragon/Raw_Notes/59191205-Transcription-Sema_Lojix_Aski.md`), `Core/ARCHITECTURAL_GUIDELINES.md`, and `Core/HIGH_LEVEL_GOALS.md`, the following core architectural directives define the "intent of the author" (Li Goldragon):

1.  **Actor-Based Programming / Data Flow (Single Ownership):** The author emphatically states that "agentic programming is essentially already declared king" and that "each object has a single owner concept. Even agents are owned, to some degree, by a process." This prevents data races and double ownership.
2.  **Recursive Sub-Flow Execution (The Russian Doll Model):** The `mentci-aid` master process orchestrates complex DAGs (Directed Acyclic Graphs). Sub-processes (subagents) must be spawned as isolated sub-flows within strict Nix Jails.
3.  **State Separation via Jujutsu (`jj`):** Sub-flows must operate on "unique, internal Jujutsu bookmarks" and do not commit directly to the parent's bookmark until finalization.
4.  **Sidereal Purity & Initialization Envelopes:** SEMA is fully specified binary data (like Cap'n Proto). Code must be free of "toxicity" (OS state leakage), meaning domain configuration must be passed via `capnp` init-envelopes, not scattered environment variables.
5.  **Logics (`lojix`) & Context Efficiency:** Text is a stepping stone to binary efficiency. Logics is an extended EDN (EDN Next) designed for maximum LLM context efficiency, saving space by omitting unnecessary whitespace/newlines.

### 2. Implementation: The Sema Subagent Flow (`@oh-my-pi/subagents`)

I have implemented a `pi` prompt template at `.pi/prompts/sema-flow.md` that wires the `@oh-my-pi/subagents` extension directly into the Sema architectural rules. 

The flow maps the specialized subagents to the author's philosophical and technical requirements:

*   **Phase 1: The Architect (`@planner`)**
    *   *Role:* Reads `Core/SEMA_RUST_GUIDELINES.md` and generates a Sema-compliant strategy (or DOT graph artifact, fulfilling Goal 1 of `HIGH_LEVEL_GOALS.md`).
    *   *Sema Mapping:* Ensures Cap'n Proto boundaries are defined upfront, establishing the "single object in/out" rule.
*   **Phase 2: The Sidereal Eye (`@explore`)**
    *   *Role:* Scans the `Sources/` and `Components/` directories for existing Cap'n Proto schemas and Rust actors.
    *   *Sema Mapping:* Prevents logic duplication and strictly respects the read-only mandate of `Sources/`.
*   **Phase 3: The Warrior (`@task`)**
    *   *Role:* Executes the code modifications.
    *   *Sema Mapping:* Enforces **State Separation**. The task subagent must create a unique, internal Jujutsu bookmark (e.g., `jj new -b subagent-task-XYZ`) for its isolated sub-flow, adhering to the single ownership concept.
*   **Phase 4: The Pure Truth (`@reviewer`)**
    *   *Role:* Evaluates the implementation before merging back to the main track.
    *   *Sema Mapping:* Checks for "Init Envelope Purity" (no env variables for domain config) and strict capitalisation ontology before squashing the sub-branch into the parent flow.

### 3. Suggestions and Questions for the Author (Li Goldragon)

As I mapped the subagent capabilities to your specified architecture, several critical questions and suggestions arose for optimizing the autonomous pipeline:

**Suggestions:**
1.  **Lojix Artifact Handoffs:** Instead of having the `planner` subagent output standard Markdown for the checklist, we should instruct it to output a `lojix` (EDN Next) data structure. The `task` subagent can ingest this structured data directly, perfectly aligning with the "Logics and Context Efficiency" mandate.
2.  **Jujutsu Subagent Workspace Bindings:** We should extend the `mentci-jj` Babashka wrapper with specific commands (e.g., `mentci-jj subagent-start <task-id>` and `mentci-jj subagent-merge <task-id>`). This would give the `@task` and `@reviewer` subagents a hardened, foolproof CLI interface to guarantee State Separation without relying on their raw bash logic.
3.  **Cap'n Proto Tooling for Agents:** We should provide a read-only schema parser tool (`mentci-schema-read`) to the `@explore` subagent. Raw `.capnp` files can consume a lot of token context; a tool that summarizes the binary structures would save immense LLM context space.

**Questions:**
1.  **DOT Graph vs. Lojix for Planning:** Goal 1 mentions passing a job to Attractor as a DOT graph artifact. Should the `@planner` subagent focus on generating strict DOT syntax, or should it generate `lojix` syntax which a Rust actor then transpiles to DOT for Attractor?
2.  **Subagent Process Ownership:** You mentioned "Each object has a single owner concept. Even agents are owned, to some degree, by a process." When `@task` executes a sub-flow, should it spawn entirely inside a nested, ephemeral Nix shell (using a generated `mkShell` derivation), or does simply running it on a unique Jujutsu bookmark inside the current Jail satisfy the isolation requirement?
3.  **The Role of Aski in Subagents:** Is the ultimate goal for the subagents to converse natively in Aski/Lojix rather than English? If so, should we begin fine-tuning the `pi` templates to demand Aski/Lojix responses for internal subagent-to-subagent communication (e.g., `@planner` handing off to `@explore`)?

## Reporting Protocol Notes

- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.