---
name: requesting-code-review
description: Use when completing tasks, implementing major features, or before merging to verify work meets requirements
---

> **Related skills:** Before requesting review, verify with `/skill:verification-before-completion` that tests pass.
>
> **JJ skills:**
> - Basic: @.pi/skills/jj-basic/SKILL.md
> - Intermediate: @.pi/skills/jj-intermediate/SKILL.md
> - Expert: @.pi/skills/jj-expert/SKILL.md

## Repo-Local Nix Purity Rule
- Treat every repository as a self-contained world during Nix evaluation.
- Never reference files from a parent repo, sibling checkout, ad-hoc absolute path, or undeclared local path escape inside Nix code.
- If reusable Nix code is needed, it must live inside the active repository or arrive through a declared flake input; if we create that code, it belongs in a repository and our repository workflow remains JJ-first, with Git only as backend transport.
- Deep modules must not `../`-escape repo boundaries to find package code. Root-wire shared derivations from the active repo root and pass them down through module arguments / `specialArgs`.


# Requesting Code Review

Dispatch a subagent with the code-reviewer prompt template to catch issues before they cascade.

**Core principle:** Review early, review often.

## When to Request Review

**Mandatory:**
- After each task in subagent-driven development
- After completing major feature
- Before merge to main

**Optional but valuable:**
- When stuck (fresh perspective)
- Before refactoring (baseline check)
- After fixing complex bug

## How to Request

**1. Get the review range via `jj-agent`:**
Ask the `jj-agent` agent for the bounded review range or comparable revisions for the code review request. Use `jj-expert` only if the `jj-agent` lane is unavailable or misbehaving. Do not compute Git SHAs directly in the main session.

**2. Dispatch code-reviewer subagent:**

Fill the template at `code-reviewer.md` in this skill directory, then dispatch a subagent with it.

**How to dispatch:**

Use the `subagent` tool with the code-reviewer template filled in:

```ts
subagent({ agent: "code-reviewer", task: "... filled template ..." })
```

**Placeholders:**
- `{WHAT_WAS_IMPLEMENTED}` - What you just built
- `{PLAN_OR_REQUIREMENTS}` - What it should do
- `{BASE_REV}` - Starting revision prepared by `jj-agent`
- `{HEAD_REV}` - Ending revision prepared by `jj-agent`
- `{DESCRIPTION}` - Brief summary

**3. Act on feedback:**
- Fix Critical issues immediately
- Fix Important issues before proceeding
- Note Minor issues for later
- Push back if reviewer is wrong (with reasoning)

## Example

```
[Just completed Task 2: Add verification function]

You: Let me request code review before proceeding.

[Ask `jj-agent` for bounded review revisions; use `jj-expert` only as fallback/rescue]
  BASE_REV: <jj-agent-provided-base-revision>
  HEAD_REV: <jj-agent-provided-head-revision>

[Dispatch code-reviewer subagent]
  WHAT_WAS_IMPLEMENTED: Verification and repair functions for conversation index
  PLAN_OR_REQUIREMENTS: Task 2 from docs/plans/deployment-plan.md
  BASE_REV: <jj-agent-provided-base-revision>
  HEAD_REV: <jj-agent-provided-head-revision>
  DESCRIPTION: Added verifyIndex() and repairIndex() with 4 issue types

[Subagent returns]:
  Strengths: Clean architecture, real tests
  Issues:
    Important: Missing progress indicators
    Minor: Magic number (100) for reporting interval
  Assessment: Ready to proceed

You: [Fix progress indicators]
[Continue to Task 3]
```

## Integration with Workflows

**Subagent-Driven Development:**
- Review after EACH task
- Catch issues before they compound
- Fix before moving to next task

**Executing Plans:**
- Uses human review between batches — dispatched code review is optional
- Useful before merge if no review happened during execution

**Ad-Hoc Development:**
- Review before merge
- Review when stuck

## Red Flags

**Never:**
- Skip review because "it's simple"
- Ignore Critical issues
- Proceed with unfixed Important issues
- Argue with valid technical feedback

**If reviewer wrong:**
- Push back with technical reasoning
- Show code/tests that prove it works
- Request clarification

See template at: `code-reviewer.md` in this skill directory
