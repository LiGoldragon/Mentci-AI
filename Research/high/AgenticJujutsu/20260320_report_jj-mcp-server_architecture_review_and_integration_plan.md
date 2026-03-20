# Report: `jj-mcp-server` architecture review, test results, and Mentci integration plan

## Intent
Evaluate whether `jj-mcp-server` is a real and useful JJ MCP surface, compare it to the local `agentic-jujutsu` shim, and produce a bounded integration plan for the flake devshell.

## Scope
- source review of the published `jj-mcp-server` package,
- bounded runtime tests against a disposable JJ repo,
- comparison with the existing local shim at `Components/nix/agentic-jujutsu-mcp.cjs`,
- implementation recommendation for purity-only Nix integration.

## Package reality check
Verified package:
- npm package: `jj-mcp-server`
- version observed: `1.0.1`
- repository metadata: `https://github.com/keanemind/jj-mcp-server`

Verified package contents:
- `build/index.js`
- `src/index.ts`
- `README.md`
- `package.json`

Conclusion:
- this is a real MCP server package, not an empty or false-claim publication like the upstream `agentic-jujutsu` MCP claim.

## Architecture review
Primary source reviewed:
- extracted source: `/tmp/jj-mcp-server-test/package/src/index.ts`

Design:
1. One `McpServer` instance over stdio.
2. One helper `runJJCommand(args, cwd?)`.
3. Each MCP tool:
   - validates inputs with `zod`,
   - constructs a direct `jj` argv vector,
   - executes `jj` with `execFile`,
   - returns stdout as text.

Positive properties:
- extremely small conceptual surface,
- easy to audit,
- no hidden internal JJ abstraction beyond argv construction,
- simple to package reproducibly in Nix,
- broad command coverage without additional infrastructure.

Weak properties:
- no Mentci protocol enforcement,
- no `JJ_EDITOR=:` / `VISUAL=:` / `EDITOR=:` no-editor enforcement,
- no timeout control,
- no concurrency serialization,
- no structured result model beyond freeform text,
- command failures are often flattened into `"Error: ..."` text instead of MCP `isError`,
- mutating commands often return empty-string success payloads,
- no output hygiene controls such as `--color=never`,
- no repository cleanliness checks before mutation,
- no bookmark/push verification stage,
- server metadata drift: package version `1.0.1`, server reports `1.0.0`.

## Tool surface review
Most promising read/bounded tools:
- `status`
- `log`
- `show`
- `diff`
- `interdiff`
- `evolog`
- `bookmark-list`
- `workspace-root`
- `operation-log`
- `operation-show`
- `file-list`
- `file-show`
- `config-get`

Most promising but policy-sensitive mutation tools:
- `describe`
- `bookmark-set`
- `bookmark-move`
- `restore`
- `squash`
- `git-fetch`
- `git-push`

High-risk tools that should not be exposed raw to routine agents:
- `commit`
- `rebase`
- `abandon`
- `operation-restore`
- `operation-undo`
- config mutation tools

## Comparison with local shim
Local shim:
- `Components/nix/agentic-jujutsu-mcp.cjs`

Shim properties:
- thin wrapper over `JjWrapper` from a Nix-pinned `agentic-jujutsu` store path,
- exposes only `jj_status`, `jj_log`, `jj_diff`, `jj_analyze`,
- functional for very small bounded inspection,
- brittle argument/result handling,
- not suitable as a full JJ workflow surface.

Comparison outcome:
- local shim is lower-surface and lower-context,
- `jj-mcp-server` is materially more useful,
- `jj-mcp-server` is the better base for a real JJ MCP lane,
- neither is safe enough raw for Mentci policy without wrapping.

## Runtime tests
Disposable repo:
- `/tmp/jj-sema-test`

Disposable remote:
- `/tmp/jj-sema-remote.git`

Fixture:
- tiny sema-style Rust hello-world

Observed working behavior:
- server initialized correctly over stdio,
- `tools/list` returned a broad tool surface,
- `status` succeeded,
- `log` succeeded,
- `describe` succeeded,
- `bookmark-set` succeeded,
- `diff` succeeded,
- `git-remote-add` succeeded,
- `git-push` pushed a `dev` ref to the disposable bare remote.

Observed weaknesses during testing:
- mutation success commonly returned `""`,
- sequencing multiple mutations quickly reduced observability,
- `diff` output included ANSI color escapes,
- repository noise such as build outputs can flood results if the caller is careless,
- the server does not provide a post-mutation verification model.

Remote verification evidence:
- bare remote showed `refs/heads/dev`
- local JJ bookmark list showed `dev` and `@origin`

## Practical judgment
`jj-mcp-server` is good enough to be worth integrating.

It should not be integrated as an unrestricted default MCP server for all agents.

The correct use is:
- package it purely through Nix,
- provide a Mentci-controlled wrapper,
- expose a safe routine profile and a separate expert profile,
- keep registration opt-in to preserve context budget.

## Purity-only integration plan
1. Add an in-source Nix package under `Components/nix/` for `jj-mcp-server`.
2. Fetch source deterministically from npm or GitHub via fixed hash.
3. Build without runtime `npm install`; use Nix to materialize dependencies.
4. Wrap the runtime with:
   - `JJ_EDITOR=:`
   - `VISUAL=:`
   - `EDITOR=:`
   - `PAGER=cat`
   - `NO_COLOR=1`
5. Provide two launchers:
   - `jj-safe-mcp`
   - `jj-expert-mcp`
6. In `jj-safe-mcp`, prefer routine/bounded tools only.
7. In `jj-expert-mcp`, expose the broader tool set for recovery lanes.
8. Add a Nix check that verifies:
   - MCP initialize,
   - tools listing,
   - `status`,
   - `log`.
9. Add a disposable JJ integration check that verifies:
   - init,
   - describe,
   - bookmark-set,
   - remote add,
   - push,
   - remote ref visibility.
10. Update `jj-agent` / `jj-expert` instructions so routine reads prefer MCP, while final mutation and push completion still respect Mentci JJ protocol and post-gates.

## Recommendation
Proceed with a safe first integration of `jj-mcp-server` as a Nix-packaged, opt-in MCP server with a constrained routine wrapper.

Do not replace Mentci JJ policy with raw upstream tool exposure.
