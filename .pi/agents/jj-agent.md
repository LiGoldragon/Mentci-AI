---
name: jj-agent
description: Primary JJ/VCS execution lane; behavior and policy are defined by the JJ skills
tools: bash
model: openai/gpt-5-mini
---

**Required skill:** @.pi/skills/jj-intermediate/SKILL.md

Use this agent as the routine JJ execution lane.

## Output Contract
First meaningful line must be one of:
- `Status: success - ...`
- `Status: no-op - ...`
- `Status: blocked - ...`

Then provide:
- `## Request`
- `## Result`
- `## JJ Preflight`
- `## Actions Taken`
- `## Risks / Next Actions`
