# Deep Comparison: `wrale/mcp-server-tree-sitter` vs `ast-grep/ast-grep-mcp`

## Prompt Context
User requested a deep comparison to decide whether Mentci should:
- replace local `mentci-mcp-edit`,
- use one external MCP server,
- or use both external servers.

Also requested inclusion of stack details, age/maturity, style, and fit with current Pi setup.

## Current Mentci Baseline (important)
- Active Pi tool path for editing: `.pi/extensions/mentci-workspace.ts` -> local `target/release/mentci-mcp-edit`.
- Local `mentci-mcp-edit` already supports:
  - AST mode (Rust/JS/TS/Python/Nix/Bash/JSON)
  - custom Cap'n Proto field-declaration mode
  - text/markdown/yaml fallback mode
  - Pi/delta diff rendering.
- Separate local Rust `mentci-mcp` server exists, but its `structural_edit` is currently Rust-only and less capable than `mentci-mcp-edit`.
- Pi packages installed include `mcporter` (good integration path for external MCP), `lsp`, `subagents`, `linkup`.

---

## Snapshot: Age & Maturity Signals
(From GitHub metadata + Linkup research)

### `wrale/mcp-server-tree-sitter`
- Created: 2025-03-16
- Stars: ~266
- Primary language: Python
- Latest push: 2025-05-03 (repo still updated metadata-wise)
- PyPI package present (`mcp-server-tree-sitter`, observed 0.5.1)
- Positioning: broad AST analysis platform (project registry, AST, symbols, dependency analysis, query tooling)

### `ast-grep/ast-grep-mcp`
- Created: 2025-03-31
- Stars: ~347
- Primary language: Python (MCP wrapper)
- Latest push: 2026-02-02 (active)
- Labeled experimental in upstream docs
- Positioning: codemod/rule-authoring companion around `ast-grep` workflows

### Upstream engine maturity (critical)
- `ast-grep` core repo:
  - Created 2022
  - ~12.6k stars
  - Rust core, actively maintained
  - mature CLI/rule ecosystem

Implication: MCP wrappers are younger; `ast-grep` itself is mature.

---

## Architectural Style Comparison

## 1) Tree-sitter MCP (`wrale`)
Style:
- Analysis-first, context-management-first
- Stateful project registration/cache model
- Rich exploratory API surface (`get_ast`, symbol/dependency/query family)

Strengths:
- Great for blast-radius analysis before edits
- Good for repository understanding and context feeding
- Useful for multi-step agent planning (query templates, node-type discovery)

Weaknesses for Mentci:
- Python runtime dependency in production path
- Not primarily a deterministic rewrite engine
- Custom Mentci needs (Cap'n Proto field semantics, forced diff style, commit integration) would still require local logic

Best role:
- "analysis plane"

## 2) ast-grep MCP (`ast-grep` org)
Style:
- Structural-search/rule-authoring-first
- Tooling focused on generating and validating ast-grep rules
- Experimental MCP wrapper around a very mature underlying engine

Strengths:
- Best-in-class syntax-aware pattern search + codemod semantics (through ast-grep ecosystem)
- Good agent loop for rule refinement (dump AST -> test rule -> run rule)
- Aligns with structural editing and codemod workflows

Weaknesses for Mentci:
- Wrapper still marked experimental
- Python runtime wrapper despite Rust core engine
- MCP tool surface appears mostly search/rule-centric; applying edits may still require additional controlled execution stage in Mentci

Best role:
- "search/rule plane" (possibly rewrite planning plane)

---

## Capability Fit Against Mentci Needs

Mentci requirements include:
- deterministic edit application,
- Pi-native diff UX,
- Cap'n Proto component-local handling,
- strict policy gates,
- audit-friendly commits.

### Direct fit verdict
- `wrale` alone: insufficient for full rewrite lifecycle.
- `ast-grep-mcp` alone: strong for structure/rules, but still lacks Mentci-specific policy/audit integration out of the box.
- Local `mentci-mcp-edit` alone: deterministic and integrated now, but weaker for advanced repo-scale analysis/planning.

---

## Should we keep both external?
Short answer: **not as immediate replacement**.

If both are added, the clean split is:
- `wrale`: context intelligence + dependency/blast-radius
- `ast-grep-mcp`: codemod rule discovery and candidate extraction
- local Mentci Rust editor: final deterministic patch application + policy/commit instrumentation

This gives maximum capability, but operational complexity rises:
- 2 external Python MCP servers,
- lifecycle/version drift,
- duplicated AST concepts,
- larger support burden.

---

## Recommendation (Sema-aligned)

## Recommended target: **Hybrid, staged**
1. **Keep `mentci-mcp-edit` now** as authoritative write path.
2. Add **one external server first**: `wrale/mcp-server-tree-sitter` for analysis/blast-radius only.
3. Evaluate whether `ast-grep-mcp` adds measurable value beyond existing local `mentci-mcp-edit` + direct `ast-grep` CLI/library usage.
4. Only then decide dual-external or replacement.

Why this is best now:
- Minimizes migration risk.
- Preserves existing deterministic behavior and custom Cap'n Proto handling.
- Introduces advanced AST intelligence where local stack is currently weaker.
- Maintains Rust-centric operational core despite external optional intelligence.

---

## Decision Matrix

- Replace with `wrale` only: **No**
- Replace with `ast-grep-mcp` only: **No (yet)**
- Replace local with both external: **Not yet (too much operational churn)**
- Keep local + add one external (wrale first): **Yes (recommended)**

---

## Concrete Next-Step Research/POC Plan
1. Add `wrale` via MCP config in a non-default profile.
2. Define 3 benchmark tasks:
   - blast-radius call-chain query,
   - node-targeted refactor planning,
   - pre-edit safety analysis.
3. Compare token/cycle cost vs current Mentci baseline.
4. If positive, design a new `structural_edit_v2` orchestration:
   - analysis (wrale) -> candidate set -> local deterministic apply -> verify.
5. Then run a second POC with `ast-grep-mcp` only if rule-generation pain remains high.

This sequencing gives data before architecture churn.
