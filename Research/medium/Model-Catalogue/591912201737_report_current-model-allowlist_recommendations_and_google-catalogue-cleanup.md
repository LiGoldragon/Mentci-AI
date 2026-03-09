# Report: Current Model Allowlist Recommendations and Google Catalogue Cleanup

## Intent
Provide very current, evidence-backed recommendations for which models should be used per major provider, and document the minimal repo cleanup applied to reduce stale model-catalogue noise.

## Scope
This report distinguishes between:
- **recommendation policy** for major providers, and
- **actual repo-controlled catalogue cleanup** performed in this repository.

The code changes in this session only directly constrain the **Google model injection path** in `Components/nix/pi-dev.nix`, because that is the concrete local source of catalogue bloat identified in repo code.

## Recommended Allowlist by Provider (March 2026)

### Google
| Use-case | Recommended model | Notes |
|---|---|---|
| Best thinker | `gemini-3.1-pro-preview` | Current highest-capability reasoning lane; still preview. |
| Best all-round well-priced | `gemini-3-flash-preview` | Best fit for fast, reliable agent work in this repo right now because it is already the validated good lane for subagents. |
| Best-priced for well-specified tasks | `gemini-3.1-flash-lite` | Cheapest currently capable Google lane for well-bounded tasks. |

**Do not prefer:** `gemini-1.5-*`, broad fuzzy `flash` matches, or legacy `2.5` defaults when a current 3.x equivalent exists.

### Anthropic
| Use-case | Recommended model | Notes |
|---|---|---|
| Best thinker | `claude-opus-4-6` | Highest-capability Claude lane. |
| Best all-round well-priced | `claude-sonnet-4-6` | Anthropic's own positioning strongly supports this as the practical default. |
| Best-priced for well-specified tasks | `claude-haiku-4-5` | Current lower-cost Claude tier. |

### OpenAI
| Use-case | Recommended model | Notes |
|---|---|---|
| Best thinker | `gpt-5.4` | Current flagship reasoning/agent model per recent release notes and model docs. |
| Best all-round well-priced | `gpt-5-mini` | Recommended for general balanced usage in current pricing/model guidance. |
| Best-priced for well-specified tasks | `gpt-5-nano` | Lowest-cost current lane for simple bounded workloads. |

**Specialized coding lane:** `gpt-5.3-codex` remains important for agentic coding, but it is specialized rather than the general default.

### xAI
| Use-case | Recommended model | Notes |
|---|---|---|
| Best thinker | `grok-4-fast-non-reasoning` | Local Pi defaults and available public evidence remain thinner here than Google/OpenAI/Anthropic. |
| Best all-round well-priced | _insufficient strong evidence_ | Do not set a repo policy default yet. |
| Best-priced for well-specified tasks | _insufficient strong evidence_ | Do not set a repo policy default yet. |

### Mistral
| Use-case | Recommended model | Notes |
|---|---|---|
| Best thinker | `devstral-medium-latest` | Strongest currently visible coding-oriented Mistral lane in the active local catalog. |
| Best all-round well-priced | _insufficient strong evidence_ | Further direct-source confirmation needed before allowlisting. |
| Best-priced for well-specified tasks | _insufficient strong evidence_ | Further direct-source confirmation needed before allowlisting. |

### Groq
| Use-case | Recommended model | Notes |
|---|---|---|
| Best thinker | `openai/gpt-oss-120b` | This is effectively a hosting/access lane rather than a unique frontier family. |
| Best all-round well-priced | _insufficient strong evidence_ | Avoid repo policy commitment until stronger direct-source evidence exists. |
| Best-priced for well-specified tasks | _insufficient strong evidence_ | Same caveat. |

### DeepSeek
| Use-case | Recommended model | Notes |
|---|---|---|
| Best thinker | `deepseek-r1` | Widely positioned as the reasoning lane. |
| Best all-round well-priced | `deepseek-v3.2` | Commonly positioned as the practical balanced lane. |
| Best-priced for well-specified tasks | `deepseek-v3.2` | Also the pragmatic cheaper choice absent a stronger separate low-cost lane. |

## Local Implementation Performed

### 1. Reduced Google catalogue injection noise
Updated:
- `Components/nix/pi-dev.nix`

The injected Google model list was tightened to the current explicitly approved set used in this repo's Google lane:
- `gemini-3.1-pro-preview`
- `gemini-3.1-flash`
- `gemini-3.1-flash-lite`
- `gemini-3-flash-preview`

Removed from the injected allowlist:
- `gemini-2.5-pro`
- `gemini-2.5-flash`

This is the most concrete repo-controlled catalogue cleanup point identified during exploration.

### 2. Preserved working explicit subagent model lane
Previously fixed subagent frontmatter remains explicitly pinned to:
- `google/gemini-3-flash-preview`

This matters because fuzzy fallback terms like `flash` and `mini` were empirically correlated with the empty-output failure lane.

## Why only Google was code-cleaned here
The repo exploration showed that the most direct, local source of catalogue bloat was the Google model injection patch inside `Components/nix/pi-dev.nix`. Other provider recommendations are documented here for policy/reporting purposes, but they are not yet all hard-coded in the same repo-controlled injection path.

## Evidence Quality Notes

### Stronger evidence
- Google official Gemini API models docs and Gemini 3.1 model card.
- Anthropic official pricing/models/release docs.
- OpenAI official API models and release notes.

### Weaker / secondary evidence
- Pricing/comparison aggregators and industry comparison articles.
- These were used only as supporting context, not as sole authority.

## Source Highlights

### Google
- Google Gemini API models docs: model availability and deprecation guidance.
- Google DeepMind Gemini 3.1 Pro model card: confirms 3.1 Pro as the current advanced reasoning lane.
- Google Cloud blog on Gemini 3 Flash: confirms Flash as the practical cost/performance workhorse.
- Google blog on Gemini 3.1 Flash-Lite: confirms Flash-Lite as the cheapest current capable lane.

### Anthropic
- Anthropic pricing docs and models overview.
- Anthropic release notes recommending migration to newer Haiku/Sonnet lanes.
- Anthropic Sonnet 4.6 product page positioning Sonnet 4.6 as the recommended balanced model.

### OpenAI
- OpenAI models docs.
- OpenAI release notes for GPT-5.4.
- Recent model comparison guidance indicating `mini` and `nano` lanes for cheaper bounded tasks, with `gpt-5.3-codex` as a specialized coding lane.

## Follow-up Recommendations
1. Add resolver hardening so fuzzy patterns like `flash`, `mini`, and `gpt` cannot silently resolve to stale or undesired models.
2. Move from fragile inline `substituteInPlace` catalogue injection toward a clearer Mentci-owned allowlist source if this area changes again.
3. Replace the rough ad-hoc file `Research/model-catalogue-recommendations.md` with this report as the canonical artifact for this session.
