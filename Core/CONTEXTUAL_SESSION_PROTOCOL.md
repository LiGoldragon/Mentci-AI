# Contextualized Prompt Session Protocol

**Status:** Implementation (Manual/Script-Aided)
**Objective:** Capture high-fidelity intent and session-level evolution within the `jj` audit trail.

## 1. Overview
This protocol ensures that every atomic change records only local intent, while full prompt/context attribution is emitted during deterministic final session synthesis.

Final synthesis is mode-dependent:
- **Single-sub-commit mode:** Rewrite the single sub-commit description by prepending final `session:` context.
- **Multi-sub-commit mode:** Duplicate session sub-commits to a synthesis branch, squash that duplicated branch into one final `session:` commit, and record the duplicated change IDs in the final message.

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

*Note: `Components/scripts/session_metadata/main.clj` may still be used to track prompt/context state, but that state is reserved for final synthesis output.*

## 4. Phase 3: Session Synthesis (Single vs Multi)
When the session's work is complete and verified:
1.  **Identify the Session Range:** Find all intent commits belonging to the prompt session (including parallel branches).
2.  **Count Sub-Commits:** Determine whether synthesis is single-sub-commit or multi-sub-commit.
3.  **Generate Result Summary:** Write a concise overview of what was actually achieved.
4.  **Apply synthesis mode:**

### 4.1 Single-Sub-Commit Mode
- Rewrite that one sub-commit description.
- Prepend the final `session:` block before existing sub-commit description content.
- Do **not** create an extra final commit.

### 4.2 Multi-Sub-Commit Mode
- Duplicate the sub-commit branch to a synthesis branch.
- Squash duplicated sub-commits into one final `session:` commit.
- Append duplicated sub-branch change IDs under `## Squashed Change IDs` in the final message.
- Preservation rule: once a `session:` commit is retained, do not abandon commits referenced in its metadata (especially `## Squashed Change IDs`) unless that same operation sequence rewrites the `session:` commit to keep references valid.

### 4.3 Canonical Final Message Shape
Use this canonical description for the rewritten single commit (single mode) or final squashed commit (multi mode):

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

## Squashed Change IDs
- <change_id_1>
- <change_id_2>
...
```

`## Squashed Change IDs` is required in multi-sub-commit mode and omitted in single-sub-commit mode.

## 5. Tooling Support
Use `bb Components/scripts/session_metadata/main.clj` to manage prompt/context state during execution and to render final synthesis messages.

## 6. Phase 4: Prompt Report Emission
After session synthesis, emit a prompt answer report under `Reports/`:
1.  **Write Report Artifact:** Use `bb Components/scripts/answer_report/main.clj` with prompt and final answer content.
2.  **Classify Answer Kind:** Report kind must be one of `answer`, `draft`, or `question`.
3.  **Classify Change Scope:** Use `modified-files` when repository files changed for the prompt; use `no-files` for question-only or advisory responses.
4.  **Persist Even Without Edits:** Report emission is mandatory even when the prompt produced no filesystem changes.
5.  **Unify Subject Counterparts:** Ensure the report subject has a matching strategy subject and vice-versa via `bb Components/scripts/subject_unifier/main.clj --write`.

Canonical command shape:
```
bb Components/scripts/answer_report/main.clj \
  --prompt "<raw prompt>" \
  --answer "<final response>" \
  --kind <answer|draft|question> \
  --change-scope <modified-files|no-files> \
  --title "<short-summary>"
```

*The Great Work continues.*

## 7. Post-Synthesis Working Copy Hygiene
After final session synthesis is complete (single or multi mode), create a fresh child working commit so the finalized session commit is left immutable and the tree stays clean for the next prompt.

Canonical command:
```
jj new dev
```

## 8. Completion Invariants
Prompt completion is valid only when all of the following hold:
1. The prompt's active head is a `session:` commit (no trailing `intent:` head for that prompt).
2. A report artifact exists for the prompt in `Reports/<Subject>/` (new report file or update under existing subject).
3. Freshness-linked workflows that use prior intel must record and verify the referenced parent change ID before execution.
4. The finalized session head is pushed at end-of-session (default push target: `dev`; release flows default to `main`).
