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

### 0. Data Weighting Mandate (Inquiry over Action)
- **Prioritize Evidence:** In a weight-driven system like an LLM, the probability of a "correct" answer increases with the density of relevant context. You MUST favor gathering more data (via `logical_run_query`, `linkup_web_search`, or `jj log`) before initiating a mutation.
- **The "Right Answer" Bias:** Treat implementation as a side effect of high-fidelity research. If the solution is not immediately obvious from the current context, execute 2-3 additional discovery tool calls to "weight" your internal reasoning toward the architectural truth.
- **Post-Report Inquiry:** When delivering a research report or analysis, you MUST conclude by explicitly asking questions. This invites the human operator to efficiently guide your next steps and ensures strict alignment with their original intent. If you refer to specific strategies or implementation plans (e.g., "Strategy 1" vs "Strategy 2"), you MUST provide a concise summary of each strategy within the inquiry to ensure the context remains immediate and readable.

### 1. Mandatory External Validation (Linkup)
Before asserting anything about external ecosystems, benchmarks, or library maturity, you **MUST** use Linkup tools. 
- Use `linkup_web_search` for broad discovery.
- Use `linkup_web_answer` for synthesizing specific facts.
- Use `linkup_web_fetch` to read technical documentation from known URLs.
- **Research Persistence Mandate (Hierarchical Discovery):** 
  - All findings, synthesized reports, and external validation evidence MUST be saved as Markdown artifacts in the `Research/` directory. 
  - **Consolidation Rule:** Store research artifacts (strategies, reports, external validation) in `Research/`. Store execution-oriented implementation plans in `docs/plans/` by default. If a workflow explicitly uses `Development/<priority>/<Subject>/` (per RestartContext mirrored topology), keep `Development/` and `Research/` counterparts synchronized by subject.
  - Use a descriptive filename prefixed with the **Solar Time** (no-separator format): `[SolarTime]_report_name.md`.
  - **Solar Time Generation:** Use the `chronos` tool with the following command to get the prefix: `chronos --format am | tr -d '.'`. (Example output: `591912122531`).
  - **Structural Order:** The agent MUST actively observe and mirror existing directory hierarchy patterns. Research artifacts MUST be placed in subdirectories based on architectural importance: `Research/high/` (Core ontology/Samskara), `Research/medium/` (Feature implementations), or `Research/low/` (Transient experiments/tooling).
- **Pattern Recognition (Structural Adherence):** The agent MUST take note of established order patterns within the repository. Before creating new files or folders, perform a recursive directory listing (`ls -R`) to ensure the new artifacts align with the established organizational logic. Note that `index.edn` files are legacy artifacts representing an incomplete Datalog implementation; the goal is to transition this knowledge into the `mentci-datalog` substrate.
- **Protocol:** Never claim a tool or architecture is "superior" without providing verified evidence from at least 2 external sources retrieved via Linkup and documented in the appropriate hierarchical level of `Research/`.

### 1.1 Subagent Orchestration Mandate (Use Often, Use Efficiently)
- **Subagent-First for Non-Trivial Work:** You MUST favor subagents for non-trivial implementation, refactoring, debugging, broad discovery, and multi-step validation. Keep the main session focused on orchestration, policy, and final synthesis.
- **Efficiency Threshold:** If work crosses more than one file, needs iterative test-fix loops, or requires tracing logic across multiple components, dispatch subagents.
- **Parallelism Requirement:** For 2+ independent tasks, dispatch parallel subagents instead of serializing in one context.
- **Triviality Exception (No Subagent Required):** Direct local execution is preferred for tiny operations, such as reading one known file, checking one symbol/location, listing a directory, or running a single bounded status command.
- **Heavy-Context Trigger:** If you are about to run 2-3 additional discovery commands for one task branch, stop and consider dispatching an `explore`/`planner` subagent first.
- **Reliability Fallback:** If subagents are unavailable or failing, report blocked state with raw evidence and continue with bounded direct tooling only for critical progress.

### 1.2 Meta-Orchestration Superpowers (Adopt + Test)
- **Contracted Handoff Payloads:** Every non-trivial subagent delegation should include explicit `Goal`, `Scope`, `Out-of-Scope`, `Output Contract`, and `Evidence Requirements` fields. Avoid free-form handoffs when correctness matters.
- **Sentinel Non-Empty Contract:** Require the first response line to be a sentinel status (`Status: success|blocked|no-op`) to reduce empty-output ambiguity in adapters.
- **Bounded Retry Ladder:** For subagent failure, retry at most 1 time with simplified scope; on second failure, fail closed and continue with direct bounded tooling.
- **Deterministic Post-Gates:** Before accepting subagent completion, run deterministic checks (targeted tests/diagnostics/status) in main session.
- **Conflict-First Parallelism:** Use parallel subagents only for independent scopes; if path overlap is likely, force serialized execution or explicit merge checkpoints.
- **Trace Packet Requirement:** Preserve delegation packet + outcome packet in Research notes when orchestration is experimental or unstable.

### 2. Logical Mastery
- Prioritize `logical_run_query` and `logical_get_ast` for understanding code.
- **Structured Query Fallback Rule:** If `logical_*` tools are unavailable in the active harness, use bounded structured queries via repository tools (`lsp`, MCP search/outline tools, or tightly-scoped `rg`) instead of ad-hoc broad scans.
- **Shortcoming Documentation Rule:** If a requested structured-query path is unavailable or returns partial coverage (for example, language or file-type gaps), document the limitation in a Research tooling log artifact, including command/scope/outcome.
- **Mirror Verification:** After any UI or code change, read `.mentci/ui_mirror.txt` to confirm the machine's output matches the intended design.

### 2.1 LSP Skilled-Usage Playbook
- **Use `symbols` first** to map local topology in a file before deep references.
- **Use `definition`/`references`** for semantic navigation instead of regex when symbol identity matters.
- **Use `diagnostics` before and after edits** on touched files to verify local semantic correctness quickly.
- **Use `workspace-diagnostics` sparingly** for a bounded set of edited files (not broad repo sweeps).
- **Use `codeAction` only after diagnostics** to avoid blind auto-fix behavior.
- **Known Limitation Handling:** If LSP reports unsupported language/file type (for example `No LSP for .ts` in some lanes), document the gap and fall back to bounded `read`/`rg`/tests.

### 3. DVCS Rigor (Jujutsu)
- Treat every component as a future independent `jj` repository.
- **State Authority Rule (JJ over Git):** Repository state authority is `jj` (`jj status`, `jj log`, `jj bookmark list`). `git` is only a storage/transport backend for Jujutsu synchronization; do not use `git status`/`git log` as the source of truth for workflow decisions.
- **Runtime Bookmark Contract:** Determine target bookmark from runtime context (`MENTCI_TARGET_BOOKMARK`) before any commit/bookmark move/push. If unset, resolve target explicitly and report it. Treat hardcoded `dev` examples as placeholders, not authority.
- **Commit Protocol (Standard Intent Header):** Every commit message MUST follow this template. The agent MUST use the exact original prompt from the interaction; if the prompt is lost, it must be synthesized from session intent.
  ```markdown
  intent: <Short, one-line summary of the change>

  ## Original Prompt
  <The exact user prompt that initiated this logical change>

  ## Context
  <High-level architectural/session status, e.g., "Salvaging history" or "Evolving Datalog substrate">

  ## Summary
  <Bullet points of specific logical and physical changes>

  ## Validation
  <Evidence of correctness: test output, cargo check, or linkup verification>
  ```
- **Clean Tree Mandate (Implementation/Intent Separation):** 
  - **The Working Copy (@) MUST remain anonymous and empty** while work is in progress. 
  - Never describe the active working copy (`jj describe`) before the mutation is complete.
  - **Workflow:** 
    1. Perform physical work in the anonymous working copy (`@`).
    2. Once complete, finalize the intent by describing the commit: `jj describe -m "..."`.
    3. If starting a new independent task, create a NEW empty commit: `jj new`.
  - **Directive Commits:** If a commit exists only with a message (pre-setting intention), it is a "Directive Commit." If a description exists on a clean worktree, treat it as critical "context"—it represents a quantifiable intent left by a predecessor. In such cases, you should still create a NEW child commit (`jj new`) to perform the actual work, preserving the directive as a distinct node in the history.
  - **Atomic Finalization:** Only describe the commit once the physical changes are staged in that commit. This ensures that every described node in the history is a non-empty, atomic logical unit.
- **Bookmark Movement Protocol:** 
  - Never move the target bookmark (`$MENTCI_TARGET_BOOKMARK`) to an "actively edited" or undescribed commit. 
  - Always finalize the work into a described commit, then move the bookmark to that immutable state: `jj bookmark set <name> -r <finalized_revision>`.
  - **Worktree Alignment:** The primary development bookmark of the current repository is considered the authoritative head. You MUST ensure your working copy is always based on this active bookmark. If the active bookmark is moved (by you or an external process, such as a rebase by a master agent), your working copy MUST follow it to maintain alignment. In most cases, this will involve rebasing your working copy onto the new location of the bookmark. If in doubt on how to align, ask for clarification.
- **Mandatory Pushing:** Always push the bookmark you are currently working on (`jj git push --bookmark <name>`). If in doubt about which bookmark to push, ask for clarification. Verify local/remote bookmark alignment.
  - **Two-Step Interaction:** Note that `jj git push` may first stage the movement (showing "Changes to push to origin") and then require a subsequent identical command to actually perform the network push. Always verify with `jj log -r <name>@origin` or a repeated push command.
- **Commit-Then-Validate Rule (Nix-heavy flows):** For rebuild-sensitive Nix work, create a small logical commit *before* running expensive `nix build`/`nix develop` validation. If validation fails, apply the fix as a new follow-up commit (do not rewrite the previous logical step). Continue in small commits until green.
- **Chain-of-Intent Preference:** Favor a sequence of small atomic intent commits over one large mutation. End the sequence with a final `session` commit summarizing outcomes and evidence.
- **Tagged Release Mode (Main-Only):** When creating a release, the release commit MUST be on `main` and MUST be tagged using the original zodiac-ordinal style.
  - **Required tag style:** `v0.12.x.x.x` (current-era shorthand of `v<cycle>.<sign>.<degree>.<minute>.<second>`)
  - **Required release flow:**
    1. Create/verify release commit on `main`.
    2. Create signed or annotated git tag with the same version.
    3. Push `main` bookmark and push the release tag.
    4. Verify `main == main@origin` and verify tag presence/signature.
- **Phantom Intent Avoidance:** Never create "Phantom Commits" (descriptions without diffs). If a squash or rebase results in an empty described commit, it must be squashed into its neighbor or deleted.
- **Session Handover:** Always end the interaction by creating a new empty commit (`jj new`). This action itself creates the clean handoff state for `@` (empty working copy on a fresh commit); do not add extra no-op graph operations (for example, rebasing that empty commit) unless explicitly required.
- **Generalization Rule:** Keep specific implementation details or transient commit hashes out of formal documentation/skills unless they are being used as a demonstrable example of a low-level technical property.

- **Basic Rebase and Push Workflow:**
    1.  **Fetch latest from remote:** `jj git fetch` (updates local view of remote bookmarks like `main@origin`).
    2.  **Rebase your bookmark onto `main`:** `jj rebase -b <your-bookmark-name> -d main@origin` (moves your commits on top of the latest `main` from remote).
    3.  **Push your rebased bookmark:** `jj git push --bookmark <your-bookmark-name>` (publishes your updated bookmark to the remote).

### 4. Resolving Version Bugs & Tooling Issues
- **Version Bumps Allowed:** Always look for a newer trusted release when hitting a version-related bug. Bumping the version is allowed and encouraged to resolve issues.
- **Forking as Fallback:** If the version bump doesn't work, fork the dependency into `Sources/` and use our fork (use `gh` or `hub` for forking).
- **$EDITOR and Saṃskāra:** The `$EDITOR` problem is effectively fixed by Saṃskāra before it even exists. Backburner any manual/legacy `$EDITOR` fixes.

### 5. Implementation Flow
1. **Research & Verify:** Use Linkup to validate assumptions.
2. **Logic & Data:** Apply `sema-programmer` rules (Logic/Data separation, Cap'n Proto contracts).
3. **Draft & Plan:** Use `/skill:brainstorming` followed by `/skill:writing-plans`.
4. **Confident Mutation:** An Independent Developer makes changes when confident about their usefulness and logical integrity. If a change is logically sound and cannot break existing functionality, proceed with implementation.
5. **Implement & Verify:** Follow the Subagent Orchestration Mandate (Section 1.1): delegate non-trivial implementation/verification to subagents, then self-verify via the Mirror Hook.

## Recency-Weighted Policy Resolution

When resolving conflicting instructions or policy interpretations, apply the following precedence stack:
1. User instruction (immediate context)
2. System/developer harness rules
3. Core authority docs (`Core/*`)
4. Skill/agent role docs
5. Legacy/older guidance

If a conflict persists within the same layer, use bounded `jj` evidence (e.g., specific commits or limited revsets) to determine which instruction is more recent or better aligned with the current state. Avoid unbounded scans; perform targeted recency checks only.

## Ad-Hoc Script Disclosure

When ad-hoc scripts (one-off scripts executed outside standard tools) are used, you MUST disclose them in your final report, including:
- **Purpose:** Why was this needed?
- **Command/Path used:** Exact script or command executed.
- **Why native tool was insufficient:** Explicit rationale.
- **Follow-up:** Recommendation for a native replacement or path to a research artifact documenting this transient need.

### 5.1 Nix Basics (High-Level Guardrails)
- Treat Nix as infrastructure/repro surface; keep domain logic in Rust + schema lanes.
- If you add or patch a runtime-integrated tool (LSP server, MCP server, CLI extension), ensure its runtime binary is present in dev shell packages (not just referenced in config).
- For Nix/runtime changes, verify with concrete shell evidence before claiming done:
  - `nix develop . --command bash -lc 'which <binary>'`
  - one targeted behavior check (`nix build .#checks.<system>.<check>` or equivalent).
- If behavior is wired but runtime is missing (PATH/tool not found), treat as incomplete implementation, not a user-environment issue.

### 6. History as the Primary Debugging Surface
- **Never give up before auditing history.** The answer to a failing tool or a logic error is most often lying in the commit log (`jj log -p`) or the operation log (`jj op log`).
- **Research the past:** If a tool was working yesterday but fails today, use `jj diff -r @--` to isolate what changed in the environment or configuration.
- **Toxic/Massive Commits:** When examining unknown or old dangling commits, do not blindly run `jj diff -r <hash>`. If the commit contains thousands of vendored files (e.g. accidentally tracking `node_modules`), printing the diff can crash the tooling or poison your context window. Always run `jj log -r <hash> --no-graph -T 'commit_id ++ "\n"' | xargs -I {} jj diff --stat -r {}` first to see the blast radius before looking at file contents. If a commit is overwhelmingly toxic, abandon it entirely rather than attempting to filter it.
- **Revset Resource Safety (CRITICAL):** Never run unbounded revsets on large repositories (e.g. `all()`, global `heads(all())`, or deep ancestry without narrowing predicates). Start with bounded/scope-limited revsets such as `@`, `@-`, `bookmarks()`, or `heads(visible())`, and expand only when required.
- **Temporary Hard Prohibition (OOM Guard):** Until explicitly lifted by the user, do NOT run broad JJ history scans in this repository. Forbidden examples: `jj log -r 'all()'`, `jj log -r 'heads(all())'`, unbounded ancestry traversals, or any history command without narrow revset/time/commit limits.
- **Safe Preview Rule:** Before running any potentially expensive `jj log`/`jj diff` operation, run a lightweight preview first (`jj status`, targeted `jj bookmark list`, or a narrowed `jj log -r '<small revset>' --no-graph`). If command scope is uncertain, ask the user before executing.
- **Failure Containment:** If a JJ command causes heavy resource pressure, stop issuing further heavy history/graph queries immediately, acknowledge the impact, and continue only with minimal bounded commands.
- **Record failures:** If you encounter an extension-loading error, don't just retry; document the exact state of `.pi/extensions.edn` and the process environment in a Research artifact.

## JJ Anti-Churn Guardrails
- Never move the target bookmark (`$MENTCI_TARGET_BOOKMARK`) to empty commit.
- Never leave multiple empty commits stacked above the target bookmark.
- After `jj new`, do not rebase/reshape empty @ unless explicitly required.
- Before bookmark moves, run `jj log -r "$MENTCI_TARGET_BOOKMARK|@|@-" --no-graph`.
- If repairing history, print raw before/after evidence.

## Subagent Reliability & Raw Evidence Contract
- **Reliability:** If a task tool returns "Unknown agent ... Available: none", stop chain execution and report blocked state. Run minimal JJ preflight evidence (`jj status`, bounded `jj log`) before retrying. Do not fabricate success from partial/empty agent outputs.
- **Evidence:** For claims about push/build/test/model availability, include raw command output snippets. Summary-only reports are not acceptable for final verification.

## Scope & Discipline
- Prefer bounded commands and semantic lookup (`lsp`).
- Avoid oversized scans and irrelevant output dumps.
- Do not reintroduce `.pi/settings.json` deny-all extension policy (`"extensions": ["!**"]`). Preserve targeted exclusion patterns.

### 7. The World Database
- **Specifying the World:** Our main task is specifying the world using CozoDB, which is then emitted in a Cap'n Proto specification.
- **Database Initialization:** We maintain a database to give an importance rating to every major component of the repo, and to store agent skills and protocols. Ensure that as new components or skills are developed, they are tracked and rated in this Datalog/CozoDB substrate.

## Completion Checklist
- [ ] Linkup validation performed and documented in Research.
- [ ] Sema-grade Logic/Data separation achieved.
- [ ] Changes verified via Mirror.
- [ ] History audited for clues before declaring an impasse.
- [ ] Atomic `intent:` commits pushed to the active integration bookmark (`main` for release mode).
- [ ] For Nix-heavy work: commit-before-validate cadence followed (small fix chain, then green).
- [ ] Final `session` commit recorded with validation evidence.
- [ ] Session ended with `jj new` to leave a clean worktree.

### 8. Crypto-Content-Addressed Rebasing
- **Independent Clones over Worktrees:** Due to shared operation-log staleness in standard `jj` worktrees, prefer working in entirely independent `jj` clones when executing distinct flows.
- **Root Authority Claim:** When opening an independent clone, the root `MentciCommit` Cap'n Proto message must define the `ownedSpacename` (the branch or subset of the DVCS variable space this clone is permitted to mutate).
- **Pruned Context:** When a redesign completes, the system should emit a new root `MentciCommit` that links to a compressed, cryptographic archive of its ancestor history, cleanly resetting the LLM context window to just the active surface area.

### 9. Persistent Awareness & Reminders
- **Active Context Management:** You MUST use the system's reminder capabilities (or internal persistent memory) to actively bring back questions, suggestions, or architectural considerations that have not been acknowledged or resolved by the human operator. Do not let critical design questions drop out of context simply because the conversation moved on.