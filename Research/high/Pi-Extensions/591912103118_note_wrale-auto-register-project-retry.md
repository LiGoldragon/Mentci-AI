# wrale Pi wrapper improvement: auto-register project on query/ast miss

## Problem
With non-daemon runtime calls, wrale project registration is in-memory and can be absent between calls.
This caused `Project '<name>' not found` when users called `wrale_run_query` directly.

## Improvement
Updated `.pi/extensions/mentci-wrale.ts`:
- `wrale_get_ast` and `wrale_run_query` now detect project-missing errors.
- They auto-call `register_project_tool` (`path: "."`, `name: <project>`) in the same runtime.
- Then they retry the original operation once.

## Runtime behavior change
- Uses one fresh mcporter runtime per tool call (`createRuntime`/`close` in one scope).
- Removes dependence on persistent process state for project registration.

## User impact
After `/reload`, users can run:
- `wrale_run_query(project: "mentci", ...)`
without manually calling `wrale_register_project` first.
