# Structured Repository Interaction Protocol

## Purpose
This document defines how repository-aware structured tooling should be used to support code understanding, anomaly investigation, and low-noise navigation.

The goal is deterministic, scope-controlled interaction with the repository structure (symbols, outlines, file trees, and targeted text search) while avoiding context explosions.

## Operating Principles
1. **Scope before query**
   - Always begin from a bounded path or bounded intent.
   - Prefer targeted symbol lookup and file-outline calls over whole-repository tree dumps.

2. **Root-governed ignore policy**
   - Repository filtering must be anchored in root `.gitignore` rules.
   - Runtime/environment directories (for example virtualenvs and build outputs) are considered non-source material and must be excluded.

3. **Anomaly-first investigation flow**
   - Start with structural overview (`repo_outline` / bounded `file_tree`).
   - Narrow into candidate files (`search_symbols` / `search_text`).
   - Escalate to exact extraction (`get_symbol` / `get_symbols`).

4. **Token discipline and auditability**
   - Avoid broad scans when a narrow query can answer the question.
   - Keep investigation artifacts reproducible by recording query scope, exclusions, and outcomes.

## Recommended Query Order
1. Verify index target and scale.
2. Run bounded outline query.
3. Run narrow search query.
4. Extract only required symbols.
5. Summarize findings against intent and expected behavior.

## Exclusion Baseline
Structured repository indexing should exclude environment and transient outputs, including:
- virtual environments (`.venv*`, `venv`)
- build outputs (`target`, `result*`)
- local tool caches and runtime directories (`.jj`, `.direnv`, equivalent ephemeral roots)

## Failure Mode to Avoid
The highest-risk operational failure is unbounded indexing across non-source subtrees, which inflates token use and drowns intent-relevant data.

The mitigation is strict root-level ignore governance plus bounded-query discipline.
