# LargeAI Rename and jj-agent Cleanup Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Rename the Rust symbol from `LargeAi` to `LargeAI` while preserving the serialized `largeAI` wire format, create and validate a new primary `jj-agent` lane (with `agentic-jj-expert` retained only as a compatibility alias), and then use bounded JJ expertise to abandon genuinely dangling commits from the last three days.

**Architecture:** Split the work into four stages: (1) stabilize the working copy by either finalizing or intentionally carrying the two pending Nix fixes, (2) apply the Rust naming correction with existing-test verification, (3) create and test a `jj-agent` prompt that combines agentic-jujutsu command surfaces with current Mentci JJ guardrails while keeping `agentic-jj-expert` as a compatibility alias, and (4) only after the agent proves reliable, use it under post-gates to help clean dangling last-three-days history.

**Tech Stack:** Rust (`criome-core`), JJ workflows, project-local agent prompt files, `agentic-jujutsu` CLI in Nix dev shell, bounded JJ inspection.

---

### Task 1: Stabilize the current working copy

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify/finalize current pending files if needed:
  - `Components/nix/agentic-jujutsu.nix`
  - `Components/nix/default.nix`

**Step 1: Verify current pending edits**
Run bounded checks to confirm the only local edits are the warning fix for `agentic-jujutsu.nix` and its wiring in `default.nix`.

**Step 2: Carry them forward intentionally**
Treat these edits as part of the current prompt’s logical change set so later JJ operations do not accidentally discard them.

**Step 3: Do not reshape history yet**
Leave finalization for the combined content pass after the rename and new agent files are in place.

### Task 2: Rename Rust `LargeAi` to `LargeAI`

**TDD scenario:** Modifying tested code — run existing tests first

**Files:**
- Modify: `Components/criome-core/src/contracts/species.rs`
- Modify: `Components/criome-core/tests/mvp_flow.rs`
- Modify any directly-related docs if needed for clarity only

**Step 1: Run current targeted tests before editing**
Run the existing `criome-core` tests covering the species/horizon path.

**Step 2: Apply the rename**
- Rename the Rust enum variant to `LargeAI`.
- Update all Rust references/tests to `NodeSpecies::LargeAI`.
- Preserve the wire format exactly:
  - `as_str()` still returns `"largeAI"`
  - serde rename remains `"largeAI"`
  - alias `"large_ai"` remains accepted

**Step 3: Re-run targeted tests**
Prove the rename compiles and the behavior remains intact.

### Task 3: Create `jj-agent`

**TDD scenario:** Trivial change — use judgment

**Files:**
- Create: `.pi/agents/jj-agent.md`
- Modify: `.pi/agents/agentic-jj-expert.md`
- Optionally update a high-level index/context file only if needed to make the lane discoverable

**Step 1: Draft the new agent prompt**
The prompt must combine:
- current Mentci JJ policy and `jj-expert` safety rules,
- runtime bookmark discipline,
- explicit change-id/commit-id rigor,
- clean-tree/finalization rules,
- side-bookmark classification,
- agentic-jujutsu CLI/MCP command surface for bounded testing.

**Step 2: Keep the lane narrow**
It should be JJ-only, read-only except for explicit JJ operations, and fail closed on ambiguous history or content-preservation risk.

### Task 4: Validate `jj-agent`

**TDD scenario:** Verification phase

**Files:**
- No product-code files required beyond the new agent prompt

**Step 1: Trivial test**
Use the new agent on a small no-op JJ task (status/bookmark/preflight question) and verify:
- non-empty output,
- correct bounded evidence,
- no unsafe recommendations.

**Step 2: Harder bounded test**
Use the same agent on a harder but still bounded task such as classifying recent side bookmarks / duplicate change IDs / detached commits without mutating anything. Verify:
- correct diagnosis,
- runtime bookmark discipline,
- no hallucinated cleanup.

**Step 3: Review**
Review the prompt and test outputs before trusting the agent for destructive cleanup.

### Task 5: Abandon dangling last-3-days commits if the new lane proves trustworthy

**TDD scenario:** JJ history repair / verification phase

**Files:**
- No content edits expected; JJ history operations only

**Step 1: Build the bounded candidate set**
Identify which last-three-days commits are truly dangling (not on the desired visible target lineage, not protected bookmarks, not preserved audit milestones, not required content-preservation anchors).

**Step 2: Preserve content before mutation**
For each candidate cluster, prove whether its content already survives elsewhere.

**Step 3: Execute bounded abandon/forget cleanup**
Prefer the smallest safe operations. Do not touch protected milestones unless explicitly intended.

**Step 4: Verify after every cleanup phase**
- `jj status`
- bounded `jj log`
- bounded bookmark list
- explicit proof that desired content still survives

### Task 6: Finalize and push

**TDD scenario:** Verification phase

**Files:**
- All touched files from tasks above only

**Step 1: Fresh verification packet**
Confirm:
- Rust rename present and tested
- new agent prompt present
- agentic-jujutsu still runnable in dev shell
- history cleanup result verified

**Step 2: Finalize via `jj-agent`**
Push the runtime target bookmark and leave exactly one empty working copy above the final revision. Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.
