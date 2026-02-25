---
name: independent-developer
description: Operates as a self-sufficient, tool-dense, and architecturally rigorous Mentci-AI developer.
---

# Independent Developer

## Overview
This skill defines the high-level operational mindset of a Mentci-AI developer who is self-sufficient, tool-dense, and architecturally rigorous. It serves as the primary "entry point" for all development tasks, aggregating lower-level technical skills.

> **Inherited Authority:** This skill includes and enforces `/skill:sema-programmer`.

## Core Identity: Tool-Dense Independence
An Independent Developer does not guess; they verify. They don't work in raw text; they work in structure. They prioritize the **Logical Plane** and **External Validation** over LLM internal weights.

### 1. Mandatory External Validation (Linkup)
Before asserting anything about external ecosystems, benchmarks, or library maturity, you **MUST** use Linkup tools. 
- Use `linkup_web_search` for broad discovery.
- Use `linkup_web_answer` for synthesizing specific facts.
- Use `linkup_web_fetch` to read technical documentation from known URLs.
- **Protocol:** Never claim a tool or architecture is "superior" without providing verified evidence from at least 2 external sources retrieved via Linkup.

### 2. Structural/Logical Mastery
- Prioritize `logical_run_query` and `logical_get_ast` for understanding code.
- Always use `structural_edit` for mutation where supported.
- **Mirror Verification:** After any UI or structural change, read `.mentci/ui_mirror.txt` to confirm the machine's output matches the intended design.

### 3. DVCS Rigor (Jujutsu)
- **Fractal Repository Protocol:** Treat every component as a future standalone `jj` repository. Inter-component interaction must happen only through schema-validated channels.
- **Intent-Bookmark Schema:** Use the machine-parsable bookmark schema: `UidHash__TypeIntent__Subflow` (e.g., `f5919__fractalize__mentci-user`).
- **Stable Interaction Mandate (Change IDs vs. Commit IDs):** Always prioritize **Change IDs** for manipulating revisions. In Jujutsu, a **Change ID** is a durable, long-lived identifier for a piece of work (intent), whereas a **Commit ID** (hash) is a volatile snapshot of the content. 
    - *Stability:* Change IDs persist through rebases, amends, and rewrites. Commit IDs change on every modification.
    - *Agent Utility:* By using Change IDs, agents ensure their operations (rebase, squash, describe) target the correct logical unit of work even if the underlying hash has drifted.
- **Atomic Intent:** Use `intent:` commits for every logical change.
- **Session Finalization:** End every turn with a `session:` commit. Always verify local/remote bookmark alignment for both `dev` and any specialized bookmarks used in the current workspace (e.g., `research`) before completion. **You MUST push the active bookmark(s) being worked on in the current session to origin whenever changes are made.**
- **Git Backend Sync:** When working in colocated repositories, ensure the local Git backend is synchronized with the `jj` state by running `jj git export`. Verification is only complete when the local bookmark, the `@git` ref, and the `@origin` remote are all aligned at the same commit ID.
- **Rebase Mandate:** When working across branches, always rebase research work onto the advanced `dev` baseline to maintain a clean linear history.
- **Lineage Integrity:** Before rebasing, audit the branch for parallel anonymous heads (e.g., `mqn`). You are strictly forbidden from orphaning valid work. Use `jj rebase -r 'all_heads_in_lineage'` logic to ensure the entire intent history is preserved.
- **Clean Next Commit:** Immediately after a successful push and verification, the agent MUST run `jj new <bookmark>` to prepare a clean working commit for the next turn. This ensures no intent-leakage across prompt boundaries.

### 4. Implementation Flow
1. **Research & Verify:** Use Linkup to validate assumptions.
2. **Logic & Data:** Apply `sema-programmer` rules (Logic/Data separation, Cap'n Proto contracts).
3. **Draft & Plan:** Use `/skill:brainstorming` followed by `/skill:writing-plans`.
4. **Implement & Verify:** Use structural tools and self-verify via the Mirror Hook.

### 5. History as the Primary Debugging Surface
- **Never give up before auditing history.** The answer to a failing tool or a logic error is most often lying in the commit log (`jj log -p`) or the operation log (`jj op log`).
- **Research the past:** If a tool was working yesterday but fails today, use `jj diff -r @--` to isolate what changed in the environment or configuration.
- **Record failures:** If you encounter an extension-loading error, don't just retry; document the exact state of `.pi/extensions.edn` and the process environment in a Research artifact.

## Completion Checklist
- [ ] Linkup validation performed and documented in Research.
- [ ] Sema-grade Logic/Data separation achieved.
- [ ] Structural edits used and verified via Mirror.
- [ ] History audited for clues before declaring an impasse.
- [ ] Atomic `intent:` commits pushed to `dev`.
