---
name: requesting-code-review
description: Use when completing tasks, implementing major features, or before merging to verify work meets requirements
---

> **Related skills:** Before requesting review, verify with `/skill:verification-before-completion` that tests pass.

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

**1. Get the review range via `jj-expert`:**
Ask the `jj-expert` agent for the bounded review range or comparable revisions for the code review request. Do not compute Git SHAs directly in the main session.

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
- `{BASE_REV}` - Starting revision prepared by `jj-expert`
- `{HEAD_REV}` - Ending revision prepared by `jj-expert`
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

[Ask `jj-expert` for bounded review revisions]
  BASE_REV: <jj-expert-provided-base-revision>
  HEAD_REV: <jj-expert-provided-head-revision>

[Dispatch code-reviewer subagent]
  WHAT_WAS_IMPLEMENTED: Verification and repair functions for conversation index
  PLAN_OR_REQUIREMENTS: Task 2 from docs/plans/deployment-plan.md
  BASE_REV: <jj-expert-provided-base-revision>
  HEAD_REV: <jj-expert-provided-head-revision>
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
