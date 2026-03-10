---
name: logical-context-persistence
description: Use to systematically persist session knowledge into the repo's semantic hierarchy, ensuring all intent, research, and implementation artifacts are placed according to the Logical File System (LFS) ontology.
---

> **Related skills:** `/skill:sema-programmer`, `/skill:writing-plans`, `/skill:verification-before-completion`

# Logical Context Persistence

## Overview

This skill ensures that session knowledge (context) is not lost and is moved from ephemeral chat to durable, semantically correct locations in the Mentci-AI repository. It operates in parallel with **Logical Reading** (understanding context) and **Logical Editing** (structural modification).

Primary goals:
1. Standardize **Knowledge Routing**.
2. Automate **LFS Metadata Tagging** (integration with upcoming Logical File System).
3. Preserve the **Metabolism of Intent** (from trace to arranged logic).
4. Maintain **Audit Integrity** via atomic commits.

## Preconditions

Before persisting context:
1. Ask the `jj-expert` agent for a bounded JJ state check to confirm the correct runtime target bookmark (`$MENTCI_TARGET_BOOKMARK`).
2. Verify the **Semantic Category** of the context you are about to save (Research, Library, Component, or Core).
3. Ensure you have the `solar` timestamp and `programming` version for commit metadata.

## The Semantic Routing Logic

Follow the **Aski-FS Ontology** (`Library/specs/AskiFsSpec.md`) when choosing a target path:

### 1) Research (`Research/<priority>/<Subject>/`)
- **Use for:** "Half-formed thoughts," findings, web search results, bug reproductions, and investigative answers.
- **LFS Tag:** `semantic_type: 'research'`.
- **Naming:** Use the numeric-prefix timestamp convention (e.g. `591912110100_report_...`).

### 2) Library (`Library/<category>/`)
- **Use for:** Stable context, external memory, specifications, and positioning documents.
- **LFS Tag:** `semantic_type: 'library'`.
- **Durability:** High. These files ensure ideas have force before they become implementation.

### 3) Core (`Core/`)
- **Use for:** Supreme Law and stable contracts (Guidelines, Protocols).
- **LFS Tag:** `semantic_type: 'core'`.
- **Constraint:** Use ALL_CAPS for supreme law files, PascalCase for stable contracts.

### 4) Components (`Components/<name>/`)
- **Use for:** Arranged logic (Rust code), component-local schemas (`.capnp`), and sidecar data (`.edn`).
- **LFS Tag:** `semantic_type: 'logic'` or `semantic_type: 'schema'`.

## Persistence Workflow

### 1) Extract the Essence
- Do not dump raw chat logs. 
- Refine the agreed-upon intent or finding into a concise markdown or EDN artifact.
- Ensure **Logic-Data Separation** (move configuration/paths to `.edn` sidecars).

### 2) Indexing (LFS Integration)
- Every new file or directory **must** be reflected in the local `index.edn`.
- Paths must be **relative** for portability.
- (Nascent LFS): Once the `logical_index_repo` tool is available, run it to sync the SQLite shadow index (`.mentci/logical_fs.db`).

### 2.1) Tool Usage Ledger (Mandatory for Tool-Heavy Sessions)
- For sessions that depend on structured queries, LSP/MCP inspection, or tool-behavior debugging, persist a tooling log artifact under `Research/medium/Tool-Usage-Documentation/`.
- The log must include:
  - tool name and bounded scope,
  - purpose of each query/action,
  - observed outcomes,
  - explicit shortcomings/coverage gaps.
- Update the local `index.edn` in that subject directory alongside the log file.

### 3) Protocol-Compliant Commits
- Commit each artifact atomically.
- Include mandatory context headers in the commit message:
  - `## Prompt`
  - `## Context`
  - `## Summary`

## Anti-Patterns (Forbidden)
- Leaving valuable context inside the ephemeral chat window only.
- Saving research findings into `Components/`.
- Hardcoding session-specific paths in `Core/` or `Library/`.
- Finshing a session without promoting validated research to arranged logic.

## Completion Checklist
- [ ] Content refined from raw chat into structured artifact.
- [ ] Correct semantic root chosen (Research vs Library vs Component).
- [ ] Local `index.edn` updated with relative path.
- [ ] (Future) `logical_index_repo` executed to sync shadow DB.
- [ ] Atomic commit with full Prompt/Context/Summary headers.
- [ ] `jj-expert` confirmed bookmark movement/push for the persisted artifact with bounded evidence.
