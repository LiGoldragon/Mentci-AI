# JJ Cleanup, Training Expansion, and sane-jj Research Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Clean up the bounded last day of JJ history, expand JJ training across all agents/prompts with expert escalation for VCS lanes, and persist research plus planning for a safer Rust JJ workflow binary under `mentci-vcs`.

**Architecture:** Treat the work as four layers: (1) bounded history cleanup of genuinely empty/noise side state, (2) propagation of basic JJ mental models to all agents/prompts, (3) a primary `jj-agent` operational lane with `jj-expert` retained as fallback/deep-debug rescue, and (4) durable research + plan artifacts for a `mentci-vcs`-hosted sane-JJ binary that wraps the CLI with typed, guarded workflows.

**Tech Stack:** Jujutsu, repository skill/agent markdown, bounded JJ inspection, Rust CLI architecture research, `mentci-vcs` component surface.

---

### Task 1: Persist sane-jj research and plan artifacts

**TDD scenario:** Trivial change — use judgment

**Files:**
- Create: `Research/medium/Vcs-Wrapper-Strategy/<solar>_report_sane-jj_wrapper_strategy_and_workflow_command_set.md`
- Create/Modify: `Research/medium/Vcs-Wrapper-Strategy/index.edn` (if absent, create it)
- Create: `docs/plans/2026-03-12-sane-jj-integration-plan.md`

**Step 1: Write the research artifact**
- Summarize external evidence for `jj-lib` vs CLI-wrapper strategy.
- Recommend extending `Components/mentci-vcs` rather than creating a new component.
- Define the safe command set: preflight, finalize, push-verify, bookmark-safe-move, side-bookmark classification, cleanup guard, fetch+rebase preview, etc.

**Step 2: Write the implementation plan**
- Map concrete files and phased work to extend `mentci-vcs` with a sane-JJ command surface.

**Step 3: Verify artifact placement**
Run bounded checks confirming the new files exist and index references are correct.

**Step 4: Finalize via `jj-agent`**
Ask `jj-agent` to finalize the non-empty revision, push the runtime target bookmark, and verify remote alignment. Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.

### Task 2: Expand JJ training to all remaining agents/prompts

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify: `.pi/agents/browser.md`
- Modify: `.pi/agents/web-search.md`
- Modify: `.pi/prompts/jj-preflight.md`
- Modify: `.pi/prompts/verification-packet.md`
- Modify: `.pi/commands/verification-packet.md`

**Step 1: Add basic JJ training to all remaining agents**
- Teach `browser` and `web-search` the repository’s JJ posture:
  - `jj` is authoritative,
  - remain read-only,
  - do not give bookmark-move advice casually,
  - escalate repo-history questions to `jj-agent` first, using `jj-expert` only as fallback/rescue.

**Step 2: Fix prompt hard-coding and add runtime-bookmark awareness**
- Replace `dev` hard-codes with `$MENTCI_TARGET_BOOKMARK` logic.
- Require bounded preflight/verification packets and explicit unresolved-bookmark handling.

**Step 3: Verify text-level consistency**
Run bounded searches to ensure the touched prompt files no longer hardcode `dev` where runtime bookmark logic is required.

**Step 4: Finalize via `jj-agent`**
Ask `jj-agent` to finalize/push with bounded evidence. Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.

### Task 3: Deepen expert JJ mastery and VCS-lane escalation

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify: `.pi/agents/jj-expert.md`
- Modify: `.pi/commands/jj-expert.md`
- Modify any additional VCS-facing training files if needed from the audit

**Step 1: Establish `jj-agent` as primary and deepen `jj-expert` as fallback**
- Add richer diagnostics for:
  - duplicate visible change IDs,
  - hidden vs visible revisions,
  - empty working-copy vs described empty commits,
  - side bookmark classification,
  - safe cleanup order,
  - safe rebase/push evidence,
  - fail-closed rules on content preservation.

**Step 2: Strengthen VCS-lane expert escalation**
- Ensure prompts/commands explicitly state that VCS-touching agents must acquire the `jj-agent` mental model first, with `jj-expert` reserved for fallback/deep-debug rescue before making repo-history recommendations.

**Step 3: Review**
Run reviewer pass on the JJ-expert/VCS-lane wording.

**Step 4: Finalize via `jj-agent`**
Ask `jj-agent` to finalize/push with bounded evidence. Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.

### Task 4: Clean up bounded last-day JJ history

**TDD scenario:** Verification/history repair phase

**Files:**
- No repo-content edits required; JJ history/bookmark cleanup only

**Step 1: Reconfirm bounded cleanup candidates**
- Re-run bounded JJ preflight.
- Confirm `roomkzpy` remains an empty/noise bookmark and that `pzsskzpy` remains a preserved audit milestone.

**Step 2: Execute cleanup via `jj-agent`**
- Remove or hide only the confirmed-noise side bookmark/history. Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.
- Preserve runtime bookmark, preserved milestones, and one empty `@` above target.

**Step 3: Verify**
- `jj status`
- bounded `jj bookmark list`
- bounded `jj log`
- ensure no accidental content loss

### Task 5: Final verification, push, then separate rebase onto latest main

**TDD scenario:** Verification phase

**Files:**
- Modify as needed from prior tasks only

**Step 1: Run bounded verification**
- Confirm touched docs/research exist and contain the expected runtime-bookmark/JJ training changes.
- Confirm history cleanup result.

**Step 2: Finalize the content work via `jj-agent`**
- Push runtime target bookmark with bounded alignment proof. Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.

**Step 3: Separate rebase operation**
- Ask `jj-agent` to fetch bounded remote state, then rebase the current runtime target bookmark from `$MENTCI_TARGET_BOOKMARK` onto latest `main`, and push. Use `jj-expert` only as fallback/rescue when the `jj-agent` lane is unavailable or misbehaving.
- Treat this as a distinct JJ operation after the content/history cleanup work is safely finalized.

**Step 4: Leave clean handoff**
- Exactly one empty working copy above the final pushed revision.
