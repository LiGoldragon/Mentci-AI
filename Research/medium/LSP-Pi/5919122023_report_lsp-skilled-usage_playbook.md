# LSP Skilled Usage Playbook (Mentci)

## Goal
Document practical high-signal LSP usage for agent workflows and record current lane limitations.

## Recommended usage sequence
1. **Topology first:** `lsp symbols` on the target file to discover canonical symbols.
2. **Semantic navigation:** `lsp definition` / `lsp references` for identity-safe traversal.
3. **Edit safety checks:** `lsp diagnostics` before and after local edits.
4. **Bounded cross-file check:** `lsp workspace-diagnostics` only for touched files.
5. **Selective auto-fix:** `lsp codeAction` only after reading diagnostics.

## Anti-patterns
- Running broad grep before attempting semantic lookup.
- Running workspace diagnostics on unbounded file sets.
- Treating LSP as universal (file-type support differs by runtime lane).
- Applying codeAction fixes blindly without validating intent.

## Session evidence
- `symbols` / `definition` worked on Rust target:
  - symbol: `resolve_target_bookmark`
  - resolved at `Components/mentci-execute/src/bin/execute.rs:43:1`
- `diagnostics` on `Components/nix/dev_shell.nix`:
  - warning only: unused `repo_root` arg
- `workspace-diagnostics` across mixed file types:
  - warning in Nix file as above
  - limitation surfaced: `No LSP for .md` on skill file

## Limitations and handling
- Some lanes do not provide LSP for all file types (observed for `.md`; previously observed for `.ts` in this environment).
- When unsupported:
  - document limitation,
  - fall back to bounded `read`/`rg`,
  - validate behavior with deterministic commands/tests.

## Skill integration completed
- Added LSP playbook guidance into:
  - `.pi/skills/independent-developer/SKILL.md`
  - `.pi/skills/subagent-driven-development/SKILL.md`
