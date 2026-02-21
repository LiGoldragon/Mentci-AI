# Contextualized Prompt Session Protocol

**Status:** Implementation (Manual/Script-Aided)
**Objective:** Capture high-fidelity intent and session-level evolution within the `jj` audit trail.

## 1. Overview
This protocol ensures that every atomic change records only its local intent, while full prompt/context attribution is emitted once in a final session-level merge commit. This supports parallel intent branches that merge back to the main trunk with one canonical narrative record.

## 2. Phase 1: Session Initiation (Capture)
Upon receiving a new user prompt (Directive), the agent must:
1.  **Store the Raw Prompt:** Copy the user's input exactly as provided.
2.  **Formulate Initial Context:** Summarize the interpretation of the prompt, the intended strategy, and any identified constraints.
3.  **Enforce Pre-Edit Cleanliness:** If the working tree is dirty at prompt start, commit pre-existing intent groups before making new edits for the prompt.

## 3. Phase 2: Atomic Attribution (The Incremental Loop)
For **every** atomic filesystem change (Level 5 requirement), use the following minimal `jj` commit format:

```
intent: <Short Description of Logical Change>
```

Constraints:
- Do not include raw prompt text in atomic commits.
- Do not include session context blocks in atomic commits.
- Keep each atomic commit scoped to one logical change so parallel work remains legible.

*Note: `scripts/session_metadata.clj` may still be used to track prompt/context state, but that state is reserved for the final session merge commit.*

## 4. Phase 3: Session Synthesis (Final Merge Commit)
When the session's work is complete and verified:
1.  **Identify the Session Range:** Find all intent commits belonging to the prompt session (including parallel branches).
2.  **Merge to Trunk:** Merge the session's parallel intent branches back to the active trunk bookmark (`dev` by default).
3.  **Generate Result Summary:** Write a concise overview of what was actually achieved.
4.  **Write the Final Contextualized Commit Message:** Use the following canonical description on the merge/aggregating commit:

```
session: <Result Summary>
♓︎ <Chronos Date>

## Original Prompt
<Full Raw User Prompt>

## Agent Context
<Full session-level strategy and interpretation summary>

## Logical Changes
- <Intent 1>
- <Intent 2>
...
```

## 5. Tooling Support
Use `bb scripts/session_metadata.clj` to manage prompt/context state during execution and to render the final session merge message.

*The Great Work continues.*
