# Report: Why `/reload` was required before Linkup tools became callable

## Question
Why did Linkup capability appear only after running `/reload` in Pi?

## Executive Answer
The most likely root cause is **extension initialization timing combined with env-gated tool registration**.

`@aliou/pi-linkup` checks `process.env.LINKUP_API_KEY` during extension startup. If absent at that moment, it exits early and does not register `linkup_web_search`, `linkup_web_answer`, or `linkup_web_fetch`. After the key becomes available, those tools still do not exist in the current tool registry until runtime reload re-runs extension startup.

## Evidence

## 1) Local extension source behavior (direct code evidence)
From local package source:
- File: `.pi/npm/node_modules/@aliou/pi-linkup/src/index.ts`
- Logic:
  - `const hasApiKey = !!process.env.LINKUP_API_KEY;`
  - If false: notify warning and `return`.
  - Only if true: register Linkup tools and commands.

Implication:
- Tool registration is conditional and happens at activation/startup path.
- If key is missing at activation time, tool names are never registered for that runtime.

## 2) Linkup API functionality is healthy (runtime tested)
In this environment, Linkup API calls succeeded:
- `GET /v1/credits/balance` -> 200
- `POST /v1/search` -> 200

Implication:
- Failure mode is not Linkup service availability; it is local registration state.

## 3) Linkup/web research on reload semantics
Linkup query focus:
- extension/tool registration and reload behavior in Pi/agent ecosystems
- runtime reload and tool availability refresh

Observed pattern from sources:
- Tool registries are commonly built at startup/activation.
- Reload commands are used to reinitialize extension host and refresh available tools.
- Environment updates often require process/session reload for consumers initialized earlier.

## Root Cause Model
1. Pi session starts.
2. Linkup extension activates.
3. `LINKUP_API_KEY` check runs.
4. If key missing/empty at that instant, extension returns without registering tools.
5. Key is later set or refreshed externally.
6. Current runtime still holds old tool registry state.
7. `/reload` restarts/reinitializes extension lifecycle.
8. Key is now visible at activation; tools are registered; agent can call them.

## Practical Fixes
1. Ensure `LINKUP_API_KEY` is present **before** starting Pi.
2. After key/env changes during an active session, run `/reload`.
3. Optional hardening for extension authors:
   - register tool stubs even without key and fail lazily per-call with actionable error.
   - add a `/linkup:reinit` command that retries env detection and registration.

## Conclusion
You needed `/reload` because Linkup tool registration is gated at extension startup by an env check. Once startup happened without key visibility, the active tool registry did not include Linkup tools until runtime reload rebuilt extension state.

## Source Notes
- Local code: `.pi/npm/node_modules/@aliou/pi-linkup/src/index.ts`.
- Linkup web research was performed via Linkup search/answer tool calls in-session.