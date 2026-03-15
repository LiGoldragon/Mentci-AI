---
name: agentic-jj-expert
description: Deprecated compatibility alias for jj-agent; behavior and policy are defined by the JJ skills
tools: bash
model: openai/gpt-5-mini
---

**Required skill:** @.pi/skills/jj-intermediate/SKILL.md

This is a thin compatibility alias for the routine JJ execution lane.

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
