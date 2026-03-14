---
name: systematic-debugging
description: Use when encountering any bug, test failure, or unexpected behavior, before proposing fixes
---

> **Related skills:** Write a failing test for the bug with `/skill:test-driven-development`. Verify the fix with `/skill:verification-before-completion`.

## Repo-Local Nix Purity Rule
- Treat every repository as a self-contained world during Nix evaluation.
- Never reference files from a parent repo, sibling checkout, ad-hoc absolute path, or undeclared local path escape inside Nix code.
- If reusable Nix code is needed, it must live inside the active repository or arrive through a declared flake input; if we create that code, it belongs in a repository and our repository workflow remains Git-backed JJ.
- Deep modules must not `../`-escape repo boundaries to find package code. Root-wire shared derivations from the active repo root and pass them down through module arguments / `specialArgs`.


# Systematic Debugging

## Overview

Random fixes waste time and create new bugs. Quick patches mask underlying issues.

**Core principle:** ALWAYS find root cause before attempting fixes. Symptom fixes are failure.

**Violating the letter of this process is violating the spirit of debugging.**

> The workflow-monitor extension tracks your debugging: it detects fix-without-investigation and counts failed fix attempts, surfacing warnings in tool results. Use `workflow_reference` with debug topics for additional guidance.

If a tool result contains a ⚠️ workflow warning, stop immediately and address it before continuing.

## The Iron Law

```
NO FIXES WITHOUT ROOT CAUSE INVESTIGATION FIRST
```

If you haven't completed Phase 1, you cannot propose fixes.

## When to Use

Use for ANY technical issue: test failures, bugs, unexpected behavior, performance problems, build failures, integration issues.

**Use this ESPECIALLY when:**
- Under time pressure (emergencies make guessing tempting)
- "Just one quick fix" seems obvious
- You've already tried multiple fixes
- Previous fix didn't work
- You don't fully understand the issue

**Don't skip when:**
- Issue seems simple (simple bugs have root causes too)
- You're in a hurry (rushing guarantees rework)

## The Four Phases

You MUST complete each phase before proceeding to the next.

### Phase 1: Root Cause Investigation

**BEFORE attempting ANY fix:**

1. **Read Error Messages Carefully** — Don't skip past errors or warnings. Read stack traces completely. Note line numbers, file paths, error codes.

2. **Reproduce Consistently** — Can you trigger it reliably? What are the exact steps? If not reproducible → gather more data, don't guess.

3. **Check Recent Changes** — Git diff, recent commits, new dependencies, config changes, environmental differences.

4. **Gather Evidence in Multi-Component Systems** — For each component boundary: log what enters, what exits, verify config propagation. Run once to see WHERE it breaks, then investigate that component.

   **Example (multi-layer system):**
   ```bash
   # Layer 1: Workflow
   echo "=== Secrets available: ==="
   echo "IDENTITY: ${IDENTITY:+SET}${IDENTITY:-UNSET}"

   # Layer 2: Build script
   echo "=== Env vars in build script: ==="
   env | grep IDENTITY || echo "IDENTITY not in environment"

   # Layer 3: Signing
   echo "=== Keychain state: ==="
   security list-keychains
   security find-identity -v
   ```
   **This reveals:** Which layer fails (e.g., secrets → workflow ✓, workflow → build ✗)

5. **Trace Data Flow** — Where does the bad value originate? What called this with the bad value? Keep tracing up until you find the source. Fix at source, not at symptom. See `root-cause-tracing.md` for the complete technique.

6. **Protect the Debugging Context** — Do not poison the main context while investigating. Avoid broad direct shell searches whose output size is uncertain, especially across logs, generated trees, build artifacts, vendored sources, or mixed-root repository scans. Prefer subagents first; if forced to inspect directly, use tightly bounded paths and output limits.

7. **Respect Exact Evidence Challenges** — If the user disputes a claim with an exact command, exact token, or literal search result (for example `rg LargeAI`), reproduce that exact check before arguing from semantic equivalence. Distinguish clearly between exact-token absence and feature absence. If the code uses a different spelling or casing (for example `LargeAi` or `largeAI`), say so explicitly instead of treating the user’s literal search as mistaken.

8. **Resolve Contradictory Tool Reports with Direct Post-Gates** — If a subagent/tool report contains mixed signals (for example `Status: blocked` but also claims push success, or a cleanup report conflicts with a direct log), stop interpretation and run direct bounded post-gates yourself. Treat raw current-state evidence as authoritative over the summary prose.

**Prometheus Runtime Post-Test Safety:** When debugging or testing any Prometheus LLM-serving runtime, include an explicit post-test check for process heat/runaway. If the node remains hot after a test, capture logs, PIDs, CPU usage snapshots (5s, 15s, 60s), and any relevant LLM/gateway /v1/models status. After evidence capture, perform a force-stop of the serving stack as an emergency mitigation and document the action and evidence in the debugging packet.

### Phase 2: Pattern Analysis

1. **Find Working Examples** — Locate similar working code in same codebase.
2. **Compare Against References** — Read reference implementation COMPLETELY. Don't skim.
3. **Identify Differences** — List every difference, however small. Don't assume "that can't matter."
4. **Understand Dependencies** — What components, settings, config, environment does this need?

### Phase 3: Hypothesis and Testing

1. **Form Single Hypothesis** — State clearly: "I think X is the root cause because Y." Be specific, not vague.
2. **Test Minimally** — Make the SMALLEST possible change. One variable at a time. Don't fix multiple things at once.
3. **Verify Before Continuing** — Did it work? Yes → Phase 4. No → Form NEW hypothesis. DON'T add more fixes on top.

### Phase 4: Implementation

1. **Create Failing Test Case** — Use `/skill:test-driven-development` for writing proper failing tests. MUST have before fixing.

2. **Implement Single Fix** — ONE change at a time. No "while I'm here" improvements. No bundled refactoring.

3. **Verify Fix** — Test passes? No other tests broken? Issue actually resolved?

4. **If Fix Doesn't Work:**
   - If < 3 attempts: Return to Phase 1, re-analyze with new information
   - **If ≥ 3 attempts: STOP (see below)**

### When 3+ Fixes Fail: Question Architecture

**This is NOT a failed hypothesis — it's a wrong architecture.**

Pattern indicating architectural problem:
- Each fix reveals new shared state/coupling in different places
- Fixes require "massive refactoring" to implement
- Each fix creates new symptoms elsewhere

**STOP and question fundamentals:**
- Is this pattern fundamentally sound?
- Are we sticking with it through sheer inertia?
- Should we refactor architecture vs. continue fixing symptoms?

**Discuss with your human partner before attempting more fixes.**

## Red Flags — STOP and Follow Process

If you catch yourself thinking:
- "Quick fix for now, investigate later"
- "Just try changing X and see if it works"
- "Add multiple changes, run tests"
- "Skip the test, I'll manually verify"
- "It's probably X, let me fix that"
- "I don't fully understand but this might work"
- "Pattern says X but I'll adapt it differently"
- "Here are the main problems: [lists fixes without investigation]"
- Proposing solutions before tracing data flow
- Running broad direct shell searches that can flood or poison the main context
- **"One more fix attempt" (when already tried 2+)**
- **Each fix reveals new problem in different place**

**ALL of these mean: STOP. Return to Phase 1.**

**If 3+ fixes failed:** Question the architecture (see above).

## Common Rationalizations

| Excuse | Reality |
|--------|---------|
| "Issue is simple, don't need process" | Simple issues have root causes too. Process is fast for simple bugs. |
| "Emergency, no time for process" | Systematic debugging is FASTER than guess-and-check thrashing. |
| "Just try this first, then investigate" | First fix sets the pattern. Do it right from the start. |
| "I'll write test after confirming fix works" | Untested fixes don't stick. Test first proves it. |
| "Multiple fixes at once saves time" | Can't isolate what worked. Causes new bugs. |
| "Reference too long, I'll adapt the pattern" | Partial understanding guarantees bugs. Read it completely. |
| "I see the problem, let me fix it" | Seeing symptoms ≠ understanding root cause. |
| "One more fix attempt" (after 2+ failures) | 3+ failures = architectural problem. Question pattern, don't fix again. |

## When Process Reveals "No Root Cause"

If investigation reveals issue is truly environmental, timing-dependent, or external:
1. Document what you investigated
2. Implement appropriate handling (retry, timeout, error message)
3. Add monitoring/logging for future investigation

**But:** 95% of "no root cause" cases are incomplete investigation.

## Supporting Techniques

These techniques are part of systematic debugging and available in this directory:

- **`root-cause-tracing.md`** — Trace bugs backward through call stack to find original trigger
- **`defense-in-depth.md`** — Add validation at multiple layers after finding root cause
- **`condition-based-waiting.md`** — Replace arbitrary timeouts with condition polling

Use `workflow_reference` for: `debug-rationalizations`, `debug-tracing`, `debug-defense-in-depth`, `debug-condition-waiting`
