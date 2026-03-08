# Supplement: JSON vs Markdown Output Reliability for LLM Command Lanes

- **Solar:** `5919.12.18.50.45`
- **Programming:** `3wyybz4j`
- **Purpose:** supplement prior subagent command-runner research with evidence on why JSON/schema-constrained output generally performs better than markdown/free-form output for machine pipelines.

## Bottom line
For automation lanes (like command execution summarization), **schema-constrained JSON reliably outperforms markdown** on parseability and integration reliability.

This does **not** mean JSON is always better for human readability or nuanced prose quality. It means JSON is better when downstream systems need deterministic fields and validation.

## Evidence (authoritative)

1. **OpenAI Structured Outputs announcement**
   - Reports perfect schema adherence on their eval for `gpt-4o-2024-08-06` with structured outputs, versus much lower adherence on older baseline models for complex schema following.
   - Explicitly states JSON mode alone does not ensure schema compliance.
   - Source: <https://openai.com/index/introducing-structured-outputs-in-the-api/>

2. **OpenAI Structured Outputs docs**
   - Recommends schema-constrained outputs for reliable application integration.
   - Frames JSON schema output as protection against missing required keys / invalid enum values.
   - Source: <https://platform.openai.com/docs/guides/structured-outputs>

3. **Anthropic Structured Outputs docs**
   - States JSON outputs can enforce valid JSON matching schema and strict tool use ensures tool parameters match input schema.
   - Supports same operational conclusion: structured output is preferred for deterministic agent workflows.
   - Source: <https://platform.claude.com/docs/en/build-with-claude/structured-outputs>

4. **JSONSchemaBench (research benchmark)**
   - Positions constrained decoding + JSON schema as the dominant approach for reliable structured generation and provides large-scale comparative evaluation framework across engines.
   - Source: <https://arxiv.org/abs/2501.10868>

## Practical implication for Mentci command subagent

For `bash_signal` / command summarization lanes, prefer:
- **Primary channel:** strict JSON schema object
- **Optional secondary channel:** brief markdown render for humans

Recommended pattern:
1. Model emits schema-valid JSON only.
2. UI renderer converts JSON into concise markdown view.
3. Raw logs remain file-backed (reference path/hash), not injected into main context.

## Suggested schema delta (from prior report)
Add explicit confidence + truncation metadata:

```json
{
  "command": "...",
  "exit_code": 0,
  "signal": {
    "errors": [],
    "warnings": [],
    "key_findings": [],
    "changed_files": []
  },
  "stdout_tail": "...",
  "stderr_tail": "...",
  "truncated": true,
  "raw_log_path": "Research/low/Command-Logs/...",
  "confidence": 0.92,
  "next_action": "..."
}
```

## Caveat
Use markdown/free text for:
- narrative explanation,
- user-facing prose,
- exploratory reasoning.

Use strict JSON for:
- tool chaining,
- state transfer between agents,
- contract-sensitive automation.
