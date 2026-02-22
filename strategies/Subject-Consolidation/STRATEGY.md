# Strategy: Subject Consolidation

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Information Topology + Operational Clarity)

## 1. Objective
Reduce the number of report/strategy subjects by merging intent-duplicate topics into a smaller canonical set while preserving traceability.

## 2. Problem
Subject sprawl currently includes:
1. near-synonyms (`*Audit`, `*Review`, `*Root-Cause`)
2. lifecycle variants for same intent (`*Strategy`, `*Expansion`, `*Reincorporation`)
3. operation fragments that should be reports under one canonical operational subject

This increases search noise, queue drift, and duplicate planning effort.

## 3. Canonicalization Rules
1. One durable problem-space = one subject.
2. Method words do not define a new subject:
- `Strategy`, `Audit`, `Review`, `Root-Cause`, `Expansion`, `Recovery`
3. Scope-specific subjects are only allowed when they change system boundary:
- example allowed split: `Top-Level-FS-Spec` vs `Workspace-Pwd-History-Purge-And-Jj-Config-Migration`
4. New subject creation requires justification that no canonical parent subject exists.

## 4. Consolidation Taxonomy
### 4.1 Governance/FS
- Canonical: `Top-Level-FS-Spec`
- Absorbed aliases:
- `Docs-Top-Level-Obsolescence-Audit`
- `Core-Root-Migration-Audit`
- `Core-Library-Astral-Migration`

### 4.2 History/Purge/Rewrites
- Canonical: `Workspace-Pwd-History-Purge-And-Jj-Config-Migration`
- Absorbed aliases:
- `Workspace-Ancient-Copy-Root-Cause`
- `Large-Commit-Audit`
- `History-Inputs-Purge`

### 4.3 Guidelines Harmonization
- Canonical: `Guidelines-Wisdom-Reincorporation`
- Absorbed aliases:
- `Expand-Guidelines-Reincorporation`
- `Guideline-Wisdom-Expansion-Subaspect`

### 4.4 Commit/Session Protocols
- Canonical: `Commit-Protocol-Merge-Fanin`
- Absorbed aliases:
- `Session-Aggregation-Guard`
- `Force-Push-Rewritten-Dev-Main-And-Close-Dirty-Tree`

## 5. Merge Decision Function
For candidate subject `S` and canonical `C`, merge when all hold:
1. ≥60% keyword overlap in title tokens after stopword removal.
2. ≥50% file-reference overlap in reports/strategy docs.
3. Shared linked-goal domain in strategy header.

If uncertain, keep separate but add cross-reference in both READMEs.

## 6. Implementation Plan
### Phase A: Inventory
1. Build subject similarity matrix from:
- subject names
- report prompt/title tokens
- referenced file paths
2. Produce proposed merge map.

### Phase B: Controlled Merge
1. Move report files from absorbed subjects into canonical subject directories.
2. Update canonical `Reports/<Subject>/README.md` entries.
3. Remove absorbed strategy/report wrapper directories.
4. Update references pointing to absorbed subjects.

### Phase C: Guardrails
1. Extend `scripts/subject_unifier/main.clj` with optional merge-map input:
- `--merge-map <edn/json>`
2. Enforce canonical-subject allowlist in report generation:
- map alias subjects to canonical subject automatically.
3. Add CI check to fail creation of non-allowlisted alias subjects.

## 7. Traceability Requirements
1. Keep original report filenames unchanged when moved.
2. Preserve provenance by adding alias notes in canonical README.
3. Record merge operation in session commit message under `## Logical Changes`.

## 8. Acceptance Criteria
1. Subject count reduced by at least 30% without loss of report artifacts.
2. No duplicate-intent subject pairs remain in `strategies/` + `Reports/`.
3. `bb scripts/subject_unifier/main.clj --write` keeps state stable (no oscillation).
4. Queue entries in `Library/STRATEGY_QUEUE.md` point only to canonical subjects.
