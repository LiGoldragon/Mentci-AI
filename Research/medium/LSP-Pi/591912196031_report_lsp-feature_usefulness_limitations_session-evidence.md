# LSP Feature Evaluation (Usefulness + Limitations)

## What was tested
- `symbols` on Rust file (`Components/mentci-execute/src/bin/execute.rs`)
- `definition` on Rust symbol (`resolve_target_bookmark`)
- `diagnostics` on Rust file
- `workspace-diagnostics` across Rust + TS file
- `diagnostics` on Nix file (`Components/nix/dev_shell.nix`) before and after fix

## Usefulness
1. **Fast semantic lookup for Rust**
   - `symbols` and `definition` worked reliably and pinpointed exact symbol location.
2. **Immediate defect detection in Nix edits**
   - LSP diagnostics exposed a real interpolation bug in `dev_shell.nix` (unescaped `${...}` inside Nix string), enabling immediate correction.
3. **Validation loop after edit**
   - Re-running diagnostics confirmed remediation success (errors cleared, only low-severity warning remained).
4. **Low-friction confidence checks**
   - Useful as a lightweight guard before heavier build/test cycles.

## Limitations observed
1. **Language coverage mismatch per lane**
   - `workspace-diagnostics` reported `No LSP for .ts` for `.pi/extensions/mentci-workspace.ts` in current runtime.
2. **Hover call strictness**
   - `hover` required exact line/column or resolvable query; fuzzy symbol name without match failed.
3. **Intermittent subagent report emptiness**
   - Not an LSP bug directly, but impacted agent-mediated review loops where LSP findings were expected in subagent outputs.

## Operational recommendation
- Keep LSP in the inner dev loop for:
  - symbol navigation,
  - targeted diagnostics,
  - pre-commit sanity checks.
- Do not rely on LSP as sole verifier; pair with command-level evidence (`cargo test`, `nix flake check`, etc.).
- For TS coverage, ensure appropriate language server availability in the active pi tool lane.

## Net assessment
**Useful and high-leverage for Rust/Nix semantic checks; constrained by per-language server availability and invocation precision requirements.**
