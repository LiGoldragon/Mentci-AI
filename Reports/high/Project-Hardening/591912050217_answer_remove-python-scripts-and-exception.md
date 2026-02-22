# Session Report

- Kind: `answer`
- Subject: `Project-Hardening`
- Title: `remove-python-scripts-and-exception`

## Prompt
there is no allowed python script, remove that - and remove any python script still in the repo

## Outcome
Removed all Python source/bytecode files from the repository and removed policy exceptions that previously allowed a Python script.

### Deleted
- All `*.py` and `__pycache__/*.pyc` files under `Components/tools/edn_format/`.

### Updated
- `Components/scripts/validate_scripts/main.clj`
  - removed the allowlist exception for `prefetch_orchestrator.py`.
  - now fails on any Python file under `Components/scripts/`.
- `Core/AGENTS.md`
  - removed Python prefetch orchestrator requirement/command.
  - updated script guard rule: Python forbidden under `Components/scripts/` with no exceptions.
- `Library/guides/TRANSITION_PYTHON_TO_CLOJURE.md`
  - removed “only exception” wording.
- `Components/workflows/mentci.aski-fs`
  - removed `prefetch_orchestrator.py` inventory entry.

### Verification
- `rg --files -g '*.py' -g '*.pyc' -g '*.pyo'` returns no results.
- `bb Components/scripts/validate_scripts/main.clj` still fails on pre-existing unrelated issue:
  - `Missing defn* usage in Components/scripts/program_version/main.clj`.
