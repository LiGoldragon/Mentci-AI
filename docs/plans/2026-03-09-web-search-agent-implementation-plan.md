# Web Search Agent Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Add a focused Linkup-based `web-search` subagent, and propagate the first-line `Status:` success contract to the other relevant agents.

**Architecture:** Add one new project agent file under `.pi/agents/` with explicit Linkup-only tools and explicit `google/gemini-3.1-flash-lite` model pinning. Make minimal prompt-only edits to `browser`, `explore`, and `reviewer` so they all guarantee a first-line `Status:` sentinel on successful completion.

**Tech Stack:** Pi project agents (`.pi/agents/*.md`), Linkup tools, subagent task probes.

---

### Task 1: Add the new web-search agent

**TDD scenario:** Trivial prompt/config addition — use judgment

**Files:**
- Create: `.pi/agents/web-search.md`

**Step 1: Create the agent file**

Add a new project agent with this frontmatter shape:
- `name: web-search`
- `description: Multi-source web research agent for external search, fetch, and synthesis`
- `tools: linkup_web_search, linkup_web_answer, linkup_web_fetch`
- `model: google/gemini-3.1-flash-lite`

Prompt requirements:
- Linkup-first web research
- multi-source synthesis and citation
- out of scope: repo search, URL-only extraction, mutation/install work
- redirects to `browser` for single-URL extraction and `explore` for repo search
- required first line: `Status: success - ...` or `Status: blocked - ...` or `Status: no-findings - ...`

**Step 2: Verify file shape manually**

Read: `.pi/agents/web-search.md`
Expected: flat frontmatter with the four fields above and explicit scope guardrails.

### Task 2: Standardize the success sentinel across adjacent agents

**TDD scenario:** Trivial prompt/config change — use judgment

**Files:**
- Modify: `.pi/agents/browser.md`
- Modify: `.pi/agents/explore.md`
- Modify: `.pi/agents/reviewer.md`

**Step 1: Add first-line success sentinel requirement**

For each file, add a short rule that successful completions must begin with `Status: ...`, matching the stricter existing style already used in `task` and `planner`.

**Step 2: Preserve role boundaries**

Do not change:
- `browser` = single URL extraction only
- `explore` = repo/codebase scouting only
- `reviewer` = code review only

**Step 3: Verify file text**

Read the edited files.
Expected: status-sentinel rule added without changing each role’s scope.

### Task 3: Live subagent verification

**TDD scenario:** Behavioral probe

**Files:**
- Test via subagent invocations only

**Step 1: Availability probe**

Run `web-search` with a simple current-info prompt.
Expected: non-empty output starting with `Status:` and referencing web sources.

**Step 2: Scope probe — single URL**

Ask `web-search` for URL-only extraction.
Expected: out-of-scope redirect toward `browser`.

**Step 3: Scope probe — repo search**

Ask `web-search` for local repo search.
Expected: out-of-scope redirect toward `explore`.

**Step 4: Regression probes**

Run minimal prompts against:
- `browser`
- `explore`
- `reviewer`

Expected: all now begin successful completions with `Status:`.

### Task 4: Commit and push

**TDD scenario:** Verification + history finalization

**Files:**
- Commit only the intended agent-file changes

**Step 1: Check tree**

Run: `jj status`
Expected: only the new/edited agent files and this plan if intentionally included in the commit.

**Step 2: Finalize commit**

Use an `intent:` message describing the web-search agent addition and shared status-rule hardening.

**Step 3: Push dev**

Run: `jj bookmark set dev -r @` then `jj git push --bookmark dev` (repeat if needed).
Expected: `dev@origin` matches local `dev`.
