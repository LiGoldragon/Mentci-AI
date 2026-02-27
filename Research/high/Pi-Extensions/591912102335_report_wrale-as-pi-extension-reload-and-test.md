# wrale as a Pi Extension: Correct Integration Path, Reload Behavior, and Test Plan

## Why this note exists
Prior validation used `mcporter` CLI directly from shell to prove wrale functionality.
That is useful for backend validation, but **Pi runtime behavior depends on extension/tool registration lifecycle**.

This document defines the Pi-native path and when `/reload` is required.

---

## Can wrale be used as a Pi extension?
Yes.

Two practical options:

1. **Via MCP runtime package path (mcporter-backed)**
   - Keep wrale configured in `config/mcporter.json`.
   - Expose wrale operations through Pi MCP tooling layer.

2. **Via explicit local Pi extension wrapper**
   - Add wrapper tools in `.pi/extensions/mentci-workspace.ts` (or separate extension) that call the configured wrale server endpoint.
   - Example wrapper tools: `wrale_register_project`, `wrale_get_ast`, `wrale_run_query`, `wrale_analyze_project`.

For Mentci, option 2 gives clearer UX and deterministic tool names in Pi.

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
2. Start Pi session.
3. Run `/reload`.
4. In Pi, run a wrale tool call (or wrapper tool call):
   - register project
   - query AST for a Rust file
5. Verify output appears as tool result in Pi UI (not only shell).

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
