# wrale as a Pi Extension: Correct Integration Path, Reload Behavior, and Test Plan

## Why this note exists
Prior validation used `mcporter` CLI directly from shell to prove wrale functionality.
That is useful for backend validation, but **Pi runtime behavior depends on extension/tool registration lifecycle**.

This document defines the Pi-native path and when `/reload` is required.

---

## Can wrale be used as a Pi extension?
Yes.

Implemented in Mentci:
- Local extension file: `.pi/extensions/mentci-wrale.ts`
- Registered Pi tools:
  - `wrale_register_project`
  - `wrale_get_ast`
  - `wrale_run_query`
- Backend path: `mcporter` runtime -> `config/mcporter.json` -> `wrale-tree-sitter` server definition.

This gives a Pi-native tool surface (no direct shell command usage required by the user).

---

## Do you have to reload Pi?
**Yes, in these cases:**

- you add/remove/change extension package entries in `.pi/extensions.edn` or `.pi/settings.json`
- you edit extension source files in `.pi/extensions/*.ts`
- you change server command/config that extensions read at activation
- you fix env vars needed at activation time

Reason:
- Pi tool registry is built during extension activation/startup.
- Existing session does not always hot-rebind newly available tools/config.

This matches observed Linkup behavior root cause in Mentci: env-gated tools were unavailable until `/reload` re-ran activation.

---

## Canonical wrale test sequence (Pi-native)

1. Ensure wrale server is installed and configured (`config/mcporter.json`).
2. Ensure extension sources parse cleanly (a syntax error in any extension can hide tools).
3. Start Pi session.
4. Run `/reload`.
5. In Pi, run wrapper tools:
   - `wrale_register_project` (path `.` name `mentci`)
   - `wrale_get_ast` (project `mentci`, path `Components/mentci-user/src/main.rs`)
6. Verify output appears as tool result in Pi UI (not only shell).

If tool not found:
- confirm config path is correct
- confirm command path exists
- run `/reload` again
- verify no startup errors in Pi extension logs

---

## Suggested Mentci policy
- Keep wrale as **analysis/blast-radius plane**.
- Keep local `mentci-mcp-edit` as **deterministic apply plane**.
- Require `/reload` after integration/config/env changes before claiming wrale availability.

---

## Minimal acceptance criteria
- [ ] wrale tool appears in Pi tool registry after `/reload`
- [ ] register project succeeds from Pi
- [ ] get_ast succeeds from Pi on `Components/mentci-user/src/main.rs`
- [ ] result is visible in Pi tool output panel
- [ ] fallback path documented if wrale unavailable (use local analysis tools)
