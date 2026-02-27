# Adaptive Skill Authority Architecture: The "Absolute Rules" Loop

## 1. Context and Motivation
As conversational sessions progress, the context window fills and older turns are compacted (`/compact`). This inherently causes a loss of instruction fidelity. Any heuristic learned, nuance agreed upon, or rule established in chat will inevitably decay, losing its authority over the agent's behavior. 

To counteract this and actually *increase* an AI skill's authority as it adapts to the environment, we must decouple the rules from the conversational context and re-inject them programmatically. We need a multi-tiered architecture where core tenets remain immutable, but learned context is compacted into an "Absolute Rules" digest that updates when underlying components change.

## 2. The Three-Tier Authority Model

We propose splitting AI skills (like `sema-programmer`) into three distinct layers, maintained entirely outside the chat history:

### Tier 1: Core Authority (The Constitution)
- **Format:** Static Markdown (`SKILL.md`) and Component specifications (`.capnp`).
- **Characteristics:** Immutable during a session. Version-controlled. Represents the highest law (e.g., "Logic-Data Separation", "Never edit generated code").
- **Injection:** Read from disk, but too large to inject every turn.

### Tier 2: Absolute Rules (The Compiled Law)
- **Format:** High-density, token-optimized String (compacted prompt).
- **Characteristics:** Re-engineered automatically whenever Tier 1 or the underlying codebase schemas change. This is a lossless compression of the Core Authority specifically tuned for the current state of the repo.
- **Injection:** Injected **every single turn** (via `before_agent_start` or `tool_call` hooks) so it never decays.

### Tier 3: Adaptive Heuristics (Case Law)
- **Format:** EDN (e.g., `.pi/skills/sema-programmer/runtime.edn`).
- **Characteristics:** Learned context. When the agent discovers a successful pattern (e.g., "rust analyzer fails if `setup.bin` is missing in `mentci-user`"), it writes this boundary condition to the EDN file using a dedicated tool. 
- **Promotion:** When Tier 2 is re-compiled, proven Tier 3 heuristics are folded into the new Absolute Rules, and the EDN is cleared/archived.

## 3. Implementation Abstraction Points

To achieve this, we can utilize several layers of the Mentci-AI infrastructure:

### A. Pi Workspace Extension (`mentci-workspace.ts`)
- **Role:** The **Injector**.
- **Action:** Intercepts `before_agent_start` (which runs on every user prompt) and reads the pre-compiled "Absolute Rules" digest from disk. It injects this digest as a hidden `<system-reminder>` with `customType: adaptive-authority`. This guarantees the agent *always* has the highest-priority rules freshly loaded, immune to `/compact`.

### B. Mentci Policy Engine / Background Watcher
- **Role:** The **Compiler**.
- **Action:** Monitors file hashes of `Components/**/*.capnp`, `Cargo.toml`, and `.pi/skills/*/SKILL.md`. When a change is detected, it triggers a lightweight LLM pipeline (or a deterministic rust script) to re-read the context and output a newly minted, highly condensed "Absolute Rules" prompt.

### C. MCP Tooling (`mentci-mcp`)
- **Role:** The **Learner**.
- **Action:** Exposes a new tool: `update_skill_heuristics(skill_name, condition, resolution)`. The agent calls this tool when it solves a complex bug or figures out a structural-edit workaround. This serializes the new knowledge into the Tier 3 EDN file, making it immediately available for the next compilation cycle.

## 4. Case Study: `sema-programmer` Re-engineering Loop

1. **Initial State:** The `sema-programmer` skill dictates "Use structural_edit over text replacement."
2. **The Encounter:** The agent tries to use `structural_edit` on a generated Rust wrapper module (`pub mod foo { include!(...) }`) and it fails to match the AST.
3. **The Adaptation:** The agent falls back to `edit` and successfully adds `#[allow(unused_parens)]`. 
4. **Knowledge Capture (Tier 3):** The agent calls `update_skill_heuristics("sema-programmer", "structural_edit fails on module include macros", "use text edit to insert #[allow(...)] before the macro")`. This is saved to `runtime.edn`.
5. **The Trigger:** A schema changes, or the user manually runs `mentci-policy-engine compile-rules`.
6. **Re-engineering (Tier 2):** The compiler reads `SKILL.md` and `runtime.edn`, synthesizing a new Absolute Rule: `"Sema Rule 4: Use structural_edit. EXCEPT for module include macros -> use standard edit for #[allow] attributes."`
7. **Perpetual Authority:** On the next prompt, `mentci-workspace.ts` injects this exact string. The agent immediately knows the workaround without needing to fail and search the chat history.

## 5. Next Steps / Execution Plan

1. **Scaffold Tier 3 Data Structure:** Create `Library/specs/AdaptiveHeuristics.md` defining the EDN schema for runtime knowledge capture.
2. **Build the Learner Tool:** Add an MCP tool to `mentci-mcp` that allows the agent to append to `.pi/skills/<skill>/runtime.edn`.
3. **Modify Extension Injection:** Update `.pi/extensions/mentci-workspace.ts` to switch from one-shot `session_start` injection to continuous `before_agent_start` injection of a compiled digest, bypassing session lifecycle decay.
4. **Implement the Compiler:** Create a rust bin/script in `Components/mentci-policy-engine` that watches for changes and generates the high-density Absolute Rules file.