# Meta-Agent Orchestration Skill Upgrade Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Improve Mentci agent/skill guidance for meta-orchestration reliability, superpower adoption, and practical LSP usage.

**Architecture:** Keep existing skill topology; add focused policy increments to core skills instead of introducing many new files. Capture findings in Research artifacts and validate with real tool calls (task + lsp).

**Tech Stack:** Pi skills/agents markdown, JJ workflow, Linkup research, LSP tool.

---

### Task 1: External + internal research capture

**TDD scenario:** Trivial change — use judgment

**Files:**
- Create: `Research/high/Superpowers-Assimilation/<solar>_report_meta-agent-orchestration_patterns_overstory_and_superpowers.md`
- Create: `Research/medium/LSP-Pi/<solar>_report_lsp-skilled-usage_playbook.md`

**Step 1:** Collect sources for orchestration patterns and Overstory.

**Step 2:** Map findings to Mentci constraints (JJ, bounded revsets, runtime bookmark contract).

**Step 3:** Persist concise report artifacts with actionable recommendations.

**Step 4:** Commit.

### Task 2: Skill upgrades (minimal diffs)

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify: `.pi/skills/independent-developer/SKILL.md`
- Modify: `.pi/skills/subagent-driven-development/SKILL.md`

**Step 1:** Add meta-orchestration “superpowers” checklist (contracted handoffs, bounded retries, deterministic post-gates, fallback behavior).

**Step 2:** Add explicit LSP skilled-usage playbook references and limitations handling.

**Step 3:** Add non-empty subagent output mitigation guidance (sentinel + structured return contract).

**Step 4:** Commit.

### Task 3: Validation and review loop

**TDD scenario:** Trivial change — use judgment

**Files:**
- No additional file required; evidence in final session response and reports.

**Step 1:** Run concrete LSP actions (`symbols`, `definition`, `diagnostics`, `workspace-diagnostics`).

**Step 2:** Run at least one subagent orchestration probe using the new structured response contract.

**Step 3:** Record usefulness + limitations, and propose next changes.

**Step 4:** Commit and push target bookmark.
