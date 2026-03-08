# Tool Usage Log — JSON vs Markdown Reliability Supplement

- **Solar:** `5919.12.18.50.45`
- **Programming:** `3wyybz4j`
- **Purpose:** document research tool usage for supplement request on evidence that JSON/schema-constrained outputs outperform markdown/free-form outputs in automation reliability.

## Tools used

1. **`linkup_web_search`**
   - Queries run for structured outputs reliability, Anthropic schema guarantees, and constrained decoding benchmarks.
   - Outcome: identified authoritative provider docs and benchmark sources.

2. **`linkup_web_fetch`**
   - Fetched canonical pages for:
     - OpenAI Structured Outputs announcement,
     - MCP architecture (context overhead relevance from previous lane),
   - plus direct citations from docs surfaced via search.

3. **`read`**
   - Read local `index.edn` files before updates.

4. **`write` / `edit`**
   - Wrote supplement report and updated topic index.

## Source quality filtering notes

- Kept **provider docs + benchmark papers** as primary evidence.
- Treated blogs/reddit/community posts as secondary/illustrative only.

## Shortcomings

- Cross-provider apples-to-apples benchmarks are still uneven in publication style and metric definitions.
- Strongest hard reliability claims come from vendor evals and schema-constrained APIs; independent benchmarking exists but focuses on constrained-decoding engines more than markdown-vs-json UX prompts.
