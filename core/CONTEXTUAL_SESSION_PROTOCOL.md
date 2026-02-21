# Contextualized Prompt Session Protocol

**Status:** Implementation (Manual/Script-Aided)
**Objective:** Capture high-fidelity intent and session-level evolution within the `jj` audit trail.

## 1. Overview
This protocol ensures that every atomic change is attributed to its original user prompt and agent context, and that the entire session is eventually synthesized into a single, high-density "Contextualized Prompt" commit. This facilitates Level 6 "Instinctive Symbolic Interaction" by providing future agents with a complete cognitive map of past sessions.

## 2. Phase 1: Session Initiation (Capture)
Upon receiving a new user prompt (Directive), the agent must:
1.  **Store the Raw Prompt:** Copy the user's input exactly as provided.
2.  **Formulate Initial Context:** Summarize the interpretation of the prompt, the intended strategy, and any identified constraints.

## 3. Phase 2: Atomic Attribution (The Incremental Loop)
For **every** atomic filesystem change (Level 5 requirement), use the following `jj` commit format:

```
intent: <Short Description of Logical Change>
[Prompt: <Raw User Prompt (Truncated if > 500 chars)>]
[Context: <Specific sub-task context for this commit>]
```

*Note: Use `scripts/session_metadata.clj` to generate this string.*

## 4. Phase 3: Session Synthesis (Synthesis)
When the session's work is complete and verified:
1.  **Identify the Session Range:** Find the revisions from the first atomic commit to the current `HEAD`.
2.  **Generate Result Summary:** Write a concise overview of what was actually achieved.
3.  **Synthesize the Contextualized Commit:** Squash the session range into a single commit with the following canonical description:

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
Use `bb scripts/session_metadata.clj` to help manage the prompt/context state and generate commit messages during the session.

*The Great Work continues.*
