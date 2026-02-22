# Agent Report

- Chronography: `12.4.57.10 | 5919 AM`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Top-Level-FS-Spec`
- Title: `components gitignore policy for jj subtree workflows`

## Prompt

report on wether or not Components top FS directory should be in gitignore (are subtree put in .gitignore when working with jj?)


## Agent Answer

Conclusion: `Components/` should **not** be in `.gitignore` if it is intended to host Mentci-owned component subtrees that must be versioned and integrated.

Findings:
- `.gitignore` currently excludes ephemeral/runtime roots like `workspace/`, not canonical durable roots.
- `jj` uses the underlying Git index semantics for tracked/ignored files. There is no JJ-specific rule that auto-ignores subtrees.
- Git subtree-style integration is tracked content and must not be ignored.
- Git submodule-style integration is represented by tracked gitlinks (plus `.gitmodules`), and the path still must not be blanket-ignored.

Recommendation:
1. Keep `Components/` tracked.
2. Ignore only local/runtime byproducts under scoped patterns (for example `Components/**/target/`, `Components/**/.direnv/`, `Components/**/.cache/`) as needed.
3. If a component is intentionally external-only and not pinned in root history, keep its checkout outside canonical `Components/` or under an explicitly ignored local-only namespace (e.g., `ComponentsLocal/`).


## Reporting Protocol Notes

- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.
