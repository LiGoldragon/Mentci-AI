# Session Report

- Kind: `strategy`
- Subject: `Top-Level-FS-Spec`
- Title: `top-level-minimization`

## Prompt
strategy to minimize files at top-level, down to the 7 top FS special directories and files that, by design, *must* live at top-level [flake.nix .gitignore etc]

## Outcome
Added `Development/Top-Level-FS-Spec/TOP_LEVEL_MINIMIZATION.md` with:
- explicit 7-directory root contract,
- required/conditional top-level file allowlists,
- phased migration + enforcement plan,
- root guard failure conditions,
- acceptance criteria for sustained compliance.
