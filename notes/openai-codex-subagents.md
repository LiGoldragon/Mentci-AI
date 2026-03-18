# Research: openai-codex provider for subagents in pi

## Key findings
1. **Codex subagents support per-agent model overrides.** Each custom agent file under `~/.codex/agents/*.toml` can declare `model`, `model_reasoning_effort`, `sandbox_mode`, `skills.config`, etc., and inherits unspecified fields from the parent session. Subagent workflows behave like regular sessions (tools, skills, approvals) and consume their own tokens; you must request them explicitly (`spawn agents-on-CSV` or natural-language instructions). Source: https://developers.openai.com/codex/subagents

2. **OpenAI-codex provider uses ChatGPT OAuth for Codex subscriptions.** Tools such as OpenClaw (and by extension codex-inspired integrators like pi) offer an `openai-codex` provider entrypoint; the CLI flow is `openclaw models auth login --provider openai-codex` or `openclaw onboard --auth-choice openai-codex`, which mirrors the pi `/login` command pattern. The provider maps to `openai-codex/gpt-5.4` (and, if entitled, `openai-codex/gpt-5.3-codex-spark`) so subagents can run under the user’s ChatGPT/Codex subscription instead of needing a separate API key. Source: https://docs.openclaw.ai/providers/openai

3. **Mapping to pi.** Pi already ships with the `openai-codex` provider row and supports `/login openai-codex` (the underlying `pi` CLI is a superset of Codex’s CLI). When you instruct pi to spawn subagents (via `/skill` heuristics or the `subagent` tool) you can target that provider/model by specifying `--models openai-codex/gpt-5.4` or `model: "openai-codex/gpt-5.4"` in the agent configuration text, ensuring each subagent inherits the OAuth session tokens. Because pi uses the same provider/model synonyms as Codex (per docs above), no additional configuration file is required beyond the usual skills/prompts and a `/login` command.

## Next steps
- Confirm that the running pi session has an `openai-codex` login in `~/.pi/settings.json` or via `/login openai-codex` before dispatching subagents.
- When spawning or automating subagents, explicitly call `subagent({agent: "worker", task: "...", model: "openai-codex/gpt-5.4"})` or similar to ensure they use the subscription-managed models.
- Keep token/token expiry in mind: the Codex docs mention approvals/approvals overlay; refresh with `/login openai-codex` if you see authentication failures.

## Sources
- OpenAI Codex subagent documentation: https://developers.openai.com/codex/subagents
- OpenClaw OpenAI provider page (details `openai-codex` entry): https://docs.openclaw.ai/providers/openai
