# Code Quality Reviewer Prompt Template

Use this template when dispatching a code quality reviewer subagent.

**Purpose:** Verify implementation is well-built (clean, tested, maintainable)

**Only dispatch after spec compliance review passes.**

```
Dispatch a subagent with the code-reviewer template:
  Use the template at ../requesting-code-review/code-reviewer.md

  WHAT_WAS_IMPLEMENTED: [from implementer's report]
  PLAN_OR_REQUIREMENTS: Task N from [plan-file]
  BASE_REV: [bounded base revision from jj-agent per @.pi/skills/jj-intermediate/SKILL.md]
  HEAD_REV: [bounded head revision from jj-agent per @.pi/skills/jj-intermediate/SKILL.md]
  DESCRIPTION: [task summary]
```

**Code reviewer returns:** Strengths, Issues (Critical/Important/Minor), Assessment
