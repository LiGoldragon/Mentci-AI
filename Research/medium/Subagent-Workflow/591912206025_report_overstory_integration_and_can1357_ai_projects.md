# Overstory Integration in Mentci-AI and Related AI Projects by can1357

## Why this research
The user requested subagent-based research on two independent topics:
1. whether Overstory is integrated into Mentci-AI, and
2. what other AI/devtool projects are maintained by the author behind the `oh-my-pi` / subagents ecosystem.

## Method
- Local Mentci-AI repository research was delegated to an `explore` subagent.
- External ecosystem/author research was delegated to a `web-search` subagent.
- This report preserves the returned evidence in one place.

## Findings

### 1. Overstory integration in Mentci-AI is currently research-and-plan level, not runtime/code level
Direct repository evidence shows Overstory is present as an influence in Mentci-AI's orchestration research and planning, but not as an implemented runtime integration.

#### Direct evidence
- `Research/high/Superpowers-Assimilation/5919122023_report_meta-agent-orchestration_patterns_overstory_and_superpowers.md`
  - documents Overstory sources, lessons, and adoption ideas for Mentci meta-agent orchestration.
- `docs/plans/2026-03-09-meta-agent-orchestration-skill-upgrade-plan.md`
  - explicitly instructs collecting Overstory sources and folding them into Mentci skill/orchestration discipline.

#### What that means
Current “integration” appears to be:
- conceptual/policy integration,
- research capture,
- planning for skill/orchestration improvements.

It does **not** yet appear to be:
- a direct runtime dependency,
- a code-level adapter/plugin,
- an explicit Overstory-powered execution surface inside Mentci-AI.

### 2. The relevant author is can1357, and they maintain several AI/devtool projects beyond oh-my-pi
The external research lane confirmed that `can1357` is the visible author/maintainer behind the `oh-my-pi` repository and identified several other AI-related or agent-adjacent tools/projects.

#### Verified projects
- **oh-my-pi**
  - terminal AI coding agent ecosystem with subagents, MCP, browser, Python, and related tooling.
- **llm-git**
  - Rust-based AI commit/changelog/rewrite assistant using Claude/OpenAI-compatible APIs.
- **C2Switcher**
  - Claude Code account manager with usage/load balancing and monitoring features.
- **AgentX**
  - AI-native issue tracker with TUI dashboard, analytics, and MCP server integration.

## Evidence

### Local repository evidence
1. `Research/high/Superpowers-Assimilation/5919122023_report_meta-agent-orchestration_patterns_overstory_and_superpowers.md`
   - proves Overstory has already been researched as an input into Mentci’s meta-agent orchestration design.
2. `docs/plans/2026-03-09-meta-agent-orchestration-skill-upgrade-plan.md`
   - proves Overstory was included in the implementation-planning lane for skill/process upgrades.

### External evidence
1. https://github.com/can1357/oh-my-pi
   - proves can1357 owns/maintains the `oh-my-pi` monorepo.
2. https://github.com/can1357/llm-git
   - proves `llm-git` is an AI commit/changelog/history tool by the same author.
3. https://github.com/can1357/c2switcher
   - proves `C2Switcher` is a Claude Code account/usage switching tool by the same author.
4. https://github.com/can1357/agentx
   - proves `AgentX` is an AI-native issue tracker with MCP/TUI features by the same author.

## Interpretation
There are two plausible readings of “Overstory integration” in Mentci-AI:

### Strategy 1: conceptual/process integration
Overstory serves as a source of orchestration ideas and guardrails that Mentci translates into:
- skill rules,
- orchestration contracts,
- bounded retry behavior,
- subagent discipline.

This strategy is **already evidenced** in the repository.

### Strategy 2: direct runtime integration
Overstory would be integrated as a code/runtime component, adapter, dependency, or execution surface.

This strategy is **not evidenced** by the current repository findings.

## Current best answer
Mentci-AI has **conceptual integration of Overstory ideas**, but I do not currently have evidence of a **direct code/runtime Overstory integration**.

## Open questions
- Do you want me to continue with a second pass specifically on whether Overstory-inspired ideas have already been translated into concrete skill/agent wording beyond the two files above?
- Do you want a separate follow-up report comparing can1357’s other tools (`llm-git`, `AgentX`, `C2Switcher`) against Mentci-AI’s architecture and identifying which design ideas are worth borrowing?