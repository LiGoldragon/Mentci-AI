# Meta-Agent Orchestration Research: Overstory + Superpowers + Mentci Fit

## Why this research
User requested deeper meta-agent orchestration research using subagents and "overstory"-style systems, then skill upgrades and practical testing.

## Evidence gathered
- External research via Linkup:
  - Overstory repo and docs (`https://github.com/jayminwest/overstory`, `.../STEELMAN.md`, `.../SECURITY.md`)
  - Microsoft/Azure orchestration patterns (`https://learn.microsoft.com/en-us/azure/architecture/ai-ml/guide/ai-agent-design-patterns`)
  - OpenAI Agents orchestration patterns (`https://openai.github.io/openai-agents-python/multi_agent/`)
  - Reliability/failure papers and guides (`https://arxiv.org/pdf/2503.13657`, Skywork best-practices article)
- Internal source:
  - `Sources/superpowers/README.md`
  - `Sources/superpowers/skills/subagent-driven-development/SKILL.md`

## High-value superpowers to adopt
1. **Contracted handoff payloads** (Goal/Scope/Out-of-scope/Output contract/Verification commands).
2. **Two-layer gate model**: agentic review + deterministic post-gates (tests/diagnostics/status).
3. **Bounded retry + fail-closed** on agent failure (no infinite retry loops).
4. **Concurrency discipline**: parallel only for independent scopes; explicit merge checkpoints for overlap.
5. **Operational observability**: preserve concise evidence packets and limitation logs.

## Overstory-specific lessons worth adapting
- Strong isolation + guardrails are useful, but complexity/cost/error amplification are real (as Overstory itself warns in STEELMAN).
- Tool-call guards and explicit orchestration policies are higher ROI than growing swarm depth.
- Depth caps and explicit escalation conditions reduce runaway multi-agent cascades.

## Tests run in this session
### Subagent behavior test (meta-orchestration contract)
- Task tool invoked with explicit non-empty sentinel contract (`Status: success|blocked|no-op`).
- Result still returned `(no output)` despite successful task execution.
- Conclusion: prompt-level mitigation alone is insufficient; adapter-level capture fallback remains needed.

## Implemented repo changes tied to this research
- Updated `.pi/skills/independent-developer/SKILL.md` with a new **Meta-Orchestration Superpowers** section.
- Updated `.pi/skills/subagent-driven-development/SKILL.md` with:
  - structured handoff contract,
  - bounded retry rule,
  - non-empty output mitigation guidance.
- Tightened non-empty first-line status requirement in `.pi/agents/task.md` and `.pi/agents/planner.md`.

## Practical next moves
1. Add adapter fallback: if parsed final assistant text is empty, return minimal status from tool metadata.
2. Add an orchestrator command template for contracted handoffs.
3. Track subagent-empty-output frequency as a measurable reliability KPI.
