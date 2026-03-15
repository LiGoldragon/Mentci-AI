# JJ Skills Aggregation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using `/skill:subagent-driven-development` or `/skill:executing-plans`.

**Goal:** Move JJ consumption guidance into three `.pi/skills` levels and retarget JJ-relevant skills and agents to those skill files via `@path` references.

**Architecture:** Keep `Core/VersionControlProtocol.md` as protocol authority for now, but make `.pi/skills` the primary consumption/training surface. Preserve existing detailed instructions in consumer files for now; only add/retarget canonical JJ skill references in this pass.

**Tech Stack:** Markdown skill/agent files under `.pi/`, bounded JJ finalization via `jj-agent`.

---

### Task 1: Create the three JJ skill hubs

**Files:**
- Create: `.pi/skills/jj-basic/SKILL.md`
- Create: `.pi/skills/jj-intermediate/SKILL.md`
- Create: `.pi/skills/jj-expert/SKILL.md`

**Steps:**
1. Write a basic JJ skill covering mental model, Git prohibition, bounded inspection, nested JJ repos, empty working-copy node, and escalation.
2. Write an intermediate JJ skill covering routine execution, no-editor automation, preflight, intent/finalization discipline, bookmark move + push verification, and routine review/finalization ranges.
3. Write an expert JJ skill covering rescue, rewrite/rebase/recovery, duplicate change IDs, side-bookmark classification, cleanup judgment, and fail-closed behavior.
4. Cross-link the three skills with `@.pi/skills/.../SKILL.md` references.

### Task 2: Retarget core JJ consumers

**Files:**
- Modify: `.pi/agents/jj-agent.md`
- Modify: `.pi/agents/jj-expert.md`
- Modify: `.pi/agents/agentic-jj-expert.md`
- Modify: `.pi/skills/independent-developer/SKILL.md`

**Steps:**
1. Point `jj-agent` directly to the intermediate JJ skill.
2. Point `jj-expert` directly to the expert JJ skill.
3. Point `agentic-jj-expert` to the same routine JJ skill used by `jj-agent`.
4. Replace remaining Library JJ references in `independent-developer` with direct skill references.

### Task 3: Add JJ skill references to other JJ-relevant skills and agents

**Files:**
- Modify: `.pi/skills/brainstorming/SKILL.md`
- Modify: `.pi/skills/executing-plans/SKILL.md`
- Modify: `.pi/skills/finishing-a-development-branch/SKILL.md`
- Modify: `.pi/skills/logical-context-persistence/SKILL.md`
- Modify: `.pi/skills/requesting-code-review/SKILL.md`
- Modify: `.pi/skills/sema-programmer/SKILL.md`
- Modify: `.pi/skills/subagent-driven-development/SKILL.md`
- Modify: `.pi/skills/using-git-worktrees/SKILL.md`
- Modify: `.pi/skills/verification-before-completion/SKILL.md`
- Modify: `.pi/skills/writing-plans/SKILL.md`
- Modify: `.pi/agents/browser.md`
- Modify: `.pi/agents/web-search.md`

**Steps:**
1. Add a compact `JJ skills` block with direct `@.pi/skills/jj-.../SKILL.md` references.
2. Use basic/intermediate/expert emphasis appropriate to the surface.
3. Do not gut existing instructions in this pass.

### Task 4: Verify references and finalize

**Steps:**
1. Search `.pi/skills` and `.pi/agents` for old Library JJ references.
2. Search for new `@.pi/skills/jj-.../SKILL.md` references to confirm coverage.
3. Request bounded review of the JJ-skill aggregation change.
4. Use `jj-agent` to commit and push to `dev`.
