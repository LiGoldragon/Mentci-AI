# Research Report: The Search Intelligence Stack for Mentci-AI

## 1. Goal
Establish a multi-modal "Intelligence Search Stack" that provides agents with both external web truth and internal repository semantics. This satisfies the **Web Validation Mandate**.

## 2. Current Internal Pillar: Logical File Search (LFS)
- **Method:** SQLite-backed Shadow Index (`.mentci/logical_fs.db`).
- **Status:** Designed. Implementation in `mentci-policy-engine` pending.
- **Capabilities:** Instant, architecture-aware filtering (logic, schema, data, component).

## 3. Current External Pillar: Linkup
- **Status:** Integrated.
- **Tools:** `linkup_web_search`, `linkup_web_answer`.
- **Fit:** Excellent for broad research and sourcing.

## 4. The "Coolest Tools" Roadmap (Web)
To reach high-authority autonomy, we should evaluate and add the following specialized search backends via MCP:

### A. Tavily (Search for Agents)
- **Strength:** Optimized for LLM consumption. Returns high-density snippets with minimal noise.
- **Role:** Deep research and technical verification.

### B. Exa (Neural Semantic Search)
- **Strength:** Uses embeddings to find semantically related content rather than keyword matches.
- **Role:** Finding "fit" and obscure technical patterns.

### C. Perplexity (Answer Intelligence)
- **Strength:** Aggregates and synthesizes answers with citations.
- **Role:** Fast, direct answer acquisition.

### D. Brave Search / Serper
- **Strength:** Raw Google/Brave API access.
- **Role:** Identifying current events, version releases, and widespread ecosystem changes.

## 5. Integration Strategy
All search intelligence will be consolidated under the `mentci-mcp` gateway. The agent should be able to call a single `logical_intelligent_search` tool that intelligently routes to Linkup, Tavily, or LFS based on the intent.

## 6. Next Steps
1.  **Benchmarking:** Run a comparison turn between Linkup and Tavily on a complex Rust-compatibility question.
2.  **LFS Implementation:** Finalize the SQLite Rust indexer.
3.  **Composite Tooling:** Build the `logical_search_router`.
