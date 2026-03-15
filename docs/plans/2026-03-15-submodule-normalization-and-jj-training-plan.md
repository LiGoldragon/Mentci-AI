# Submodule Normalization and JJ Training Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Normalize root submodule registration so tracked component gitlinks behave consistently like `Components/CriomOS`, and add one authoritative JJ usage/training document for agents, then wire it into the `independent-developer` skill.

**Architecture:** Keep the submodule authority in the root repo by making `.gitmodules` match the actual root-tracked gitlinks, rather than relying on ad-hoc local nested directories. Put JJ guidance in one stable Markdown authority file, synthesized from existing repo JJ rules and agent pitfalls, then link to it from the high-level skill entrypoint so future agents load the canonical explanation early.

**Tech Stack:** Git submodule metadata in `.gitmodules`, JJ workflow docs in Markdown, project skills under `.pi/skills/`, Rust verification via `Components/mentci-vcs`.

---

### Task 1: Normalize `.gitmodules` to match root-tracked component gitlinks

**TDD scenario:** Trivial config change — use judgment, then verify with submodule status and helper commands.

**Files:**
- Modify: `.gitmodules`
- Verify: root Git submodule metadata and `Components/mentci-vcs` helper output

**Step 1: Confirm exact mismatch set**

Run bounded checks to compare root gitlinks to `.gitmodules` paths.

**Step 2: Update `.gitmodules` minimally**

- Add `Components/mentci-execute` with the matching GitHub URL style used by sibling `mentci-*` components.
- Remove the duplicate `Components/chronos-lib` block.

**Step 3: Verify submodule metadata shape**

Run:
- `git config -f .gitmodules --get-regexp '^submodule\..*\.path$'`
- `git submodule status --recursive`

Expected:
- no duplicate path block
- no fatal missing mapping for `Components/mentci-execute`

**Step 4: Verify helper still reports clean sync state**

Run:
- `cargo run --manifest-path Components/mentci-vcs/Cargo.toml -- sync-required-submodules`

Expected:
- `0 need sync, 0 issues`

### Task 2: Agglomerate JJ usage into one training document

**TDD scenario:** Documentation change — use judgment, then verify link targets and content coverage.

**Files:**
- Create: `Library/documentation/JujutsuJjUsage.md`
- Read/reference: `Core/VersionControlProtocol.md`, `Core/AGENTS.md`, `.pi/prompts/jj-preflight.md`, `.pi/prompts/verification-packet.md`, `.pi/commands/jj-agent.md`, `.pi/commands/jj-expert.md`, `agent_update.txt`, `Library/RestartContext.md`, related JJ mentions in plans/docs

**Step 1: Synthesize canonical JJ guidance**

Document:
- JJ vs Git backend authority
- bookmarks vs branches
- change IDs vs commit IDs
- empty working-copy commit concept
- why trying to “remove” the empty working commit often creates another empty commit
- target bookmark movement and origin verification
- bounded revset safety
- nested JJ repos and submodules
- common agent mistakes and safe patterns

**Step 2: Keep the document agent-oriented**

Explain concepts for Git-trained agents, with concrete “do / don’t” examples.

**Step 3: Verify coverage against the surveyed files**

Make sure the synthesized doc covers the high-weight rules already present in repo authority files without contradicting them.

### Task 3: Wire JJ training into `independent-developer` and fix the sema link syntax

**TDD scenario:** Documentation/skill change — use judgment, then verify rendered text and link syntax.

**Files:**
- Modify: `.pi/skills/independent-developer/SKILL.md`

**Step 1: Add explicit JJ training reference**

Near the top of the skill, add a concise link to `@Library/documentation/JujutsuJjUsage.md` so agents load the canonical JJ explanation.

**Step 2: Fix sema-programmer loading syntax**

Replace the plain `/skill:sema-programmer` style inherited-authority mention with an `@...` file reference so the linked skill can be loaded directly by path.

**Step 3: Verify the final skill text**

Read back the changed section and ensure both references are present and unambiguous.

### Task 4: Final verification and finalize via `jj-agent`

**TDD scenario:** Verification/finalization cycle.

**Files:**
- Verify: `.gitmodules`, `Library/documentation/JujutsuJjUsage.md`, `.pi/skills/independent-developer/SKILL.md`

**Step 1: Run deterministic verification**

Run:
- `git submodule status --recursive`
- `cargo test -p mentci-vcs` or `cd Components/mentci-vcs && cargo test`
- `cargo run --manifest-path Components/mentci-vcs/Cargo.toml -- sync-required-submodules`
- targeted reads of the new doc and updated skill

**Step 2: Request review**

Get a bounded review on the root-repo diff.

**Step 3: Finalize via `jj-agent`**

Ask the `jj-agent` agent to:
- establish bounded JJ state,
- finalize the current intent into the correct described revision,
- push the runtime target bookmark,
- verify local/remote bookmark alignment.
Use `jj-expert` only if the `jj-agent` lane is unavailable or misbehaving.
