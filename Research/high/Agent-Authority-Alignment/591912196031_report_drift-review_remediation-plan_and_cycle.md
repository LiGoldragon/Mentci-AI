# Drift Review and Remediation Plan (Session Cycle)

## Scope
Requested scope: review filesystem for older guidance drifting from current intent, then run a brainstorm/plan/review/critique/implement/test/review cycle with subagent-heavy analysis.

Primary intent anchors:
- runtime bookmark contract (`MENTCI_TARGET_BOOKMARK`) over hardcoded branch names
- OOM-safe JJ usage (bounded revsets only)
- reliable subagent/reporting behavior

## Cycle Summary
1. **Brainstorm / Discovery (subagents):** explored drift across `Core/`, `.pi/`, `Library/`, `docs/plans/`.
2. **Plan (subagent planner):** produced phased remediation (quick wins now, deeper archive/condense later).
3. **Critique (subagent reviewer):** narrowed to low-risk high-impact edits for this session.
4. **Implement (local edits):** updated high-authority guidance and skill docs.
5. **Test:** exercised LSP actions and validated Nix diagnostics after correction.
6. **Review:** self-review plus evidence summary.

## Drift Findings (Current Session)
- Hardcoded `dev`/`main` workflow instructions remained in core protocol docs.
- `execute report` canonical command in contextual protocol lacked `--subject`.
- Subagent mechanics doc still marked hook model as "Proposed" despite active usage.
- Nix shell hook had interpolation drift (`${...}` inside Nix string), creating LSP parse errors.

## Implemented Remediation
### Edited
- `Core/VersionControlProtocol.md`
  - switched push/verify/new command examples from hardcoded `dev` to `$MENTCI_TARGET_BOOKMARK`
  - updated completion invariants to target runtime bookmark lineage
  - renamed jailed shipping heading to target-bookmark framing
  - added explicit OOM guard against unbounded revsets (`all()`, `heads(all())`)
- `Core/ContextualSessionProtocol.md`
  - added missing `--subject` in `execute report` canonical command
  - switched post-synthesis push/verify/`jj new` examples to `$MENTCI_TARGET_BOOKMARK`
  - updated completion invariants to runtime bookmark language
- `Core/ARCHITECTURAL_GUIDELINES.md`
  - section 0.2 updated from hardcoded `dev` to runtime target bookmark
  - added release-lane clarification (`main` only for explicit release/integration)
- `.pi/skills/finishing-a-development-branch/SKILL.md`
  - default target now `$MENTCI_TARGET_BOOKMARK`
  - option 1 commands switched from `main` to runtime target bookmark
  - clean handover command now `jj new "$MENTCI_TARGET_BOOKMARK"`
- `Library/documentation/Pi_Subagent_Mechanics.md`
  - renamed "Proposed Architecture" to "Active Integration Architecture"
  - added reliability/evidence contract section (non-empty outputs, raw evidence, fail-closed on subagent/tool errors)
- `Components/nix/dev_shell.nix`
  - escaped shell parameter expansion inside Nix string (`''${...}`), fixing LSP syntax/undefined-variable errors

## Test Evidence (LSP)
- `lsp symbols` on `Components/mentci-execute/src/bin/execute.rs` found `resolve_target_bookmark (43:1)`
- `lsp definition` resolved `resolve_target_bookmark` to `execute.rs:43:1`
- `lsp diagnostics` on execute.rs: `No diagnostics`
- `lsp workspace-diagnostics` on execute.rs + `.pi/extensions/mentci-workspace.ts`:
  - 0 errors/0 warnings total
  - limitation surfaced: `No LSP for .ts` in this lane
- `lsp diagnostics` on `Components/nix/dev_shell.nix`:
  - before fix: multiple parse/undefined-variable errors near bookmark inference block
  - after fix: only non-critical warning (`repo_root` unused)

## Remaining Plan (Next Session)
1. Add executable enforcement (`execute branch-guard`, richer `jj-preflight --json`).
2. Add non-VC runtime context producer in `mentci-user` (`runtime-context.json`).
3. Run archive/condense pass for superseded docs/plans (with explicit deprecation markers + index cleanup).
4. Implement Pi extension-level push guard using runtime bookmark authority.

## Risk Notes
- Subagent output capture still intermittently returns `(no output)` despite task success; mitigation should move to explicit structured-output contract and adapter-level fallback handling.
- Avoid broad JJ revset scans to prevent OOM/context blowups.
