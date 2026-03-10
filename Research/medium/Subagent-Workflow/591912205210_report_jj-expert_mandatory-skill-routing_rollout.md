# Report: JJ-Expert Mandatory Skill Routing Rollout

## Prompt
I want all the jj/git handling to go through an agent, so make it mandatory in the skills.

## Summary
- Updated high-authority and high-frequency skills so non-trivial JJ/git handling is routed through the `jj-expert` agent.
- Reworked VCS-heavy skills to define policy/orchestration while pushing execution responsibility to `jj-expert`.
- Converted review-range flow from SHA language to JJ revision language in the touched review skills/templates.
- Preserved the signed-release-tag requirement while removing direct multi-step main-session JJ/git workflow instructions from the touched skills.
- Repeatedly re-reviewed the rollout until policy contradictions, placeholder drift, broken paths, and Markdown formatting regressions were resolved.

## Files Updated
- `.pi/skills/independent-developer/SKILL.md`
- `.pi/skills/brainstorming/SKILL.md`
- `.pi/skills/writing-plans/SKILL.md`
- `.pi/skills/finishing-a-development-branch/SKILL.md`
- `.pi/skills/using-git-worktrees/SKILL.md`
- `.pi/skills/requesting-code-review/SKILL.md`
- `.pi/skills/requesting-code-review/code-reviewer.md`
- `.pi/skills/sema-programmer/SKILL.md`
- `.pi/skills/logical-context-persistence/SKILL.md`
- `.pi/skills/subagent-driven-development/SKILL.md`
- `.pi/skills/subagent-driven-development/code-quality-reviewer-prompt.md`
- `.pi/skills/verification-before-completion/SKILL.md`

## Design Outcome
The policy now distinguishes between:
- **non-trivial JJ/git handling** → mandatory `jj-expert` lane
- **bounded fallback when subagents are unavailable/failing** → direct local JJ evidence is allowed only for critical blocked-state recovery

This preserves the existing reliability-fallback doctrine while making agent-mediated VCS handling the normal path.

## Review/Repair Loop Outcome
The rollout required multiple review passes. Valid issues found and fixed included:
- contradictory direct JJ/git instructions left in `independent-developer`
- stale Git SHA placeholders in touched review flow docs/templates
- impossible template path in `subagent-driven-development/code-quality-reviewer-prompt.md`
- inconsistent example flow that skipped `jj-expert`
- Markdown fence regressions in `writing-plans`
- accidental weakening of signed-tag policy wording
- impossible fallback path that tried to use an agent after agent unavailability had already been established

Final reviewer outcome:
- `Status: no issues found in reviewed scope.`

## Notes
This was intentionally implemented as a skill-policy/documentation rollout rather than a code/runtime enforcement change. The next logical step, if desired, would be code-first enforcement for some of these behaviors so fewer instructions need to carry operational burden.
