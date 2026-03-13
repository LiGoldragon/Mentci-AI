# CriomOS and Maisiliym Node-Truth Operator Guidance Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Recover the current network-guidance drift by documenting that Maisiliym owns node/network truth and CriomOS owns build/deploy consumption of that truth.

**Architecture:** First classify the current suspicious CriomOS networking edits against the existing horizon/node data flow. Then add one canonical operator guidance document per repo plus one short mirror mention in a second high-visibility guidance doc. Keep the docs operator-first and happy-path-only while preserving the general node-truth rule.

**Tech Stack:** JJ, Nix/CriomOS module docs, Maisiliym node proposal docs, Markdown, bounded verification commands.

---

### Task 1: Classify current CriomOS networking drift

**TDD scenario:** Trivial change — use judgment

**Files:**
- Read: `Components/CriomOS/nix/mkCriomOS/network/unbound.nix`
- Read: `Components/CriomOS/nix/mkCriomOS/network/default.nix`
- Read: `Components/CriomOS/nix/mkCrioZones/mkHorizonModule.nix`
- Read: `~/git/maisiliym/datom.nix`

**Step 1: Capture the suspicious local diff**

Run: `cd Components/CriomOS && jj diff --summary`
Expected: bounded networking-related file list only

**Step 2: Compare the diff against the structured data flow**

Read the horizon/node normalization and node proposal files.
Expected: identify which edits preserve external node truth and which edits hardcode consumer truth.

**Step 3: Record the classification in a Research artifact**

Write a concise recovery report under `Research/medium/CriomOS/`.
Expected: one artifact describing valid progress vs invalid DNS/data-authority drift.

**Step 4: Verify the research artifact exists**

Run: `jj diff --summary`
Expected: Research artifact visible in the main repo working copy.

### Task 2: Add canonical CriomOS operator guidance

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify: `Components/CriomOS/docs/GUIDELINES.md`
- Modify: `Components/CriomOS/docs/AGENTS.md`

**Step 1: Add the canonical node-truth operator section to the guidance doc**

Document two short sections:
1. Edit authority (`maisiliym`)
2. Build/deploy authority (`CriomOS`)

Expected: a clear “must update when editing repo” marker and the operator happy path.

**Step 2: Add a short mirror pointer in the secondary guidance doc**

Expected: AGENTS points operators/agents to the canonical node-truth guidance section.

**Step 3: Verify the changed docs still read cleanly**

Run: read both files.
Expected: no contradictory instructions and a single authoritative path.

### Task 3: Add canonical Maisiliym operator guidance

**TDD scenario:** Trivial change — use judgment

**Files:**
- Create or modify: `~/git/maisiliym/docs/GUIDELINES.md`
- Modify: `~/git/maisiliym/README.md`

**Step 1: Create the canonical Maisiliym node-truth guidance doc**

Document that node/network identity changes must be made in `datom.nix` / `NodeProposal` first.
Expected: operator-facing rule with a “must update when editing repo” marker.

**Step 2: Add a short mirror mention from a visible doc**

Expected: README points to the canonical guidance doc without becoming the authority.

**Step 3: Verify the doc accurately reflects current node-truth structure**

Read the new guidance against `datom.nix`.
Expected: exact file paths and no hand-wavy statements.

### Task 4: Verify and review

**TDD scenario:** Trivial change — use judgment

**Files:**
- Verify edited docs and research artifact only

**Step 1: Run bounded verification in both repos**

Run:
- `cd /home/li/git/Mentci-AI--dev && jj status && jj diff --summary`
- `cd /home/li/git/Mentci-AI--dev/Components/CriomOS && jj status && jj diff --summary`
- `cd /home/li/git/maisiliym && jj status && jj diff --summary`

Expected: only intentional documentation changes plus any pre-existing classified debris.

**Step 2: Run one focused Nix behavior check if guidance references a build path**

Run: `cd /home/li/git/Mentci-AI--dev/Components/CriomOS && nix flake show --all-systems | rg 'crioZones|nixosConfigurations'`
Expected: confirm the documented build surface exists.

**Step 3: Request review**

Ask the `reviewer` agent to inspect the changed documentation for architectural accuracy and operator clarity.

**Step 4: Finalize via `jj-agent`**

Ask the `jj-agent` agent to:
- establish bounded JJ state,
- finalize the current intent into the correct described revision,
- push the runtime target bookmark,
- verify local/remote bookmark alignment.
Use `jj-expert` only if the `jj-agent` lane is unavailable or misbehaving.
