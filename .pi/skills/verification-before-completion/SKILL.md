---
name: verification-before-completion
description: Use when about to claim work is complete, fixed, or passing, before committing or creating PRs - requires running verification commands and confirming output before making any success claims; evidence before assertions always
---

> **Related skills:** Follow up with `/skill:requesting-code-review` before merging. Done? `/skill:finishing-a-development-branch`.
>
> **JJ guidance:** @.pi/skills/jj-basic/SKILL.md, @.pi/skills/jj-intermediate/SKILL.md, @.pi/skills/jj-expert/SKILL.md

## Repo-Local Nix Purity Rule
- Treat every repository as a self-contained world during Nix evaluation.
- Never reference files from a parent repo, sibling checkout, ad-hoc absolute path, or undeclared local path escape inside Nix code.
- If reusable Nix code is needed, it must live inside the active repository or arrive through a declared flake input; if we create that code, it belongs in a repository and our repository workflow remains JJ-first, with Git only as backend transport.
- Deep modules must not `../`-escape repo boundaries to find package code. Root-wire shared derivations from the active repo root and pass them down through module arguments / `specialArgs`.


# Verification Before Completion

## Overview

Claiming work is complete without verification is dishonesty, not efficiency.

**Core principle:** Evidence before claims, always.

**Violating the letter of this rule is violating the spirit of this rule.**

If a tool result contains a ⚠️ workflow warning, stop immediately and address it before continuing.

## Boundaries
- Run verification commands: yes
- Read code and output: yes
- Edit source code: no

## The Iron Law

```
NO COMPLETION CLAIMS WITHOUT FRESH VERIFICATION EVIDENCE
```

If you haven't run the verification command in this message, you cannot claim it passes.

## The Gate Function

```
BEFORE claiming any status or expressing satisfaction:

1. IDENTIFY: What command proves this claim?
2. RUN: Execute the FULL command (fresh, complete)
3. READ: Full output, check exit code, count failures
4. VERIFY: Does output confirm the claim?
   - If NO: State actual status with evidence
   - If YES: State claim WITH evidence
5. ONLY THEN: Make the claim

Skip any step = lying, not verifying
```

## Common Failures

| Claim | Requires | Not Sufficient |
|-------|----------|----------------|
| Tests pass | Test command output: 0 failures | Previous run, "should pass" |
| Linter clean | Linter output: 0 errors | Partial check, extrapolation |
| Build succeeds | Build command: exit 0 | Linter passing, logs look good |
| Bug fixed | Test original symptom: passes | Code changed, assumed fixed |
| Regression test works | Red-green cycle verified | Test passes once |
| Agent completed | VCS diff shows changes | Agent reports "success" |
| Requirements met | Line-by-line checklist | Tests passing |

## Rationalization Prevention

| Excuse | Reality |
|--------|---------|
| "Should work now" | RUN the verification |
| "I'm confident" | Confidence ≠ evidence |
| "Just this once" | No exceptions |
| "Linter passed" | Linter ≠ compiler |
| "Agent said success" | Verify independently |
| "I'm tired" | Exhaustion ≠ excuse |
| "Partial check is enough" | Partial proves nothing |
| "Different words so rule doesn't apply" | Spirit over letter |

## Red Flags - STOP

- Using "should", "probably", "seems to"
- Expressing satisfaction before verification ("Great!", "Perfect!", "Done!", etc.)
- About to commit/push/PR without verification
- Trusting agent success reports without checking outputs
- Relying on partial verification
- Thinking "just this once"
- Tired and wanting work over
- **ANY wording implying success without fresh evidence**

## Key Patterns

**Tests:**
```
✅ Run tests + confirm 0 failures before saying "tests pass"
❌ "Should pass now" / "Looks correct"
```

**Regression tests (TDD Red-Green):**
```
✅ Verify red → green sequence before claiming fix durability
❌ "I added a regression test" (without proving red-green)
```

**Build:**
```
✅ Run build command + confirm exit 0
❌ "Linter passed" (linter ≠ build)
```

**Requirements:**
```
✅ Check requirements one-by-one, report verified status
❌ "Tests pass, so everything is complete"
```

**Agent delegation:**
```
✅ Verify agent output + diffs + commands yourself
❌ Trust "agent says success"
```

**JJ completion hygiene:**
```
✅ Follow @.pi/skills/jj-intermediate/SKILL.md for routine completion-state checks and @.pi/skills/jj-expert/SKILL.md for dangling-head or side-history classification
❌ Declare completion while unresolved JJ state still requires those checks
```

**Contradictory agent/tool reports:**
```
✅ If a report mixes success and failure claims, run direct bounded post-gates immediately
❌ Paraphrase the optimistic half and ignore the contradictory evidence
```

**Preservation claims:**
```
✅ When claiming a change was preserved, verify the current surviving file contents in the visible target lineage
❌ Infer preservation only from older commits or historical presence elsewhere in the graph
```

**User-provided exact command output:**
```
✅ Reproduce the exact check first, then explain any naming/casing mismatch
❌ Override the user's literal evidence with a broader semantic claim without reproducing it
```

## Subagent Reliability & Raw Evidence Contract
- **Reliability:** If the JJ execution lane is unavailable or misbehaving, stop and report blocked state rather than fabricating success from partial/empty outputs.
- **JJ evidence routing:** Use @.pi/skills/jj-intermediate/SKILL.md for the canonical routine JJ evidence requirements and @.pi/skills/jj-expert/SKILL.md when the state is contradictory or rescue-heavy.
- **Raw Evidence Packet:**
    - For all completion claims, provide a `## Raw Evidence Packet` section.
    - Include the verification command output (tests/build/etc) plus the JJ evidence required by the relevant JJ skill.

## Enforcement

The workflow-monitor extension monitors `git commit`, `git push`, and `gh pr create`. If you haven't run a passing test suite since your last source file edit, a warning is injected into the tool result. The warning clears automatically after a fresh passing test run.

When all verification passes, mark the verify phase complete: call `plan_tracker` with `{action: "update", status: "complete"}` for the current phase.
