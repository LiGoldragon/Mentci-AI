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
- Treat every component as a future independent `jj` repository.
- Use atomic `intent:` commits for every logical change.
- End every session with a finalized `session:` commit pushed to `dev`.

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
