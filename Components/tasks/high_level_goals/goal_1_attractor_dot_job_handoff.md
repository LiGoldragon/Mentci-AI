# Goal 1 Task: Attractor DOT Job Handoff

Goal source: `Core/HIGH_LEVEL_GOALS.md` (Goal 1)

## Objective

Make the first real Attractor call using a versioned DOT artifact and verify deterministic, auditable handoff.

## Checklist

- [x] Confirm prerequisites are available.
- [x] Verify `Inputs/untyped/brynary-attractor/attractor` exists.
- [x] Verify tools: `bun`, `curl`, `jq`. (`bun` provided via `nix shell nixpkgs#bun` for this run.)
- [x] Reserve a local test port and define run workspace under `workspace/` or `Outputs/Logs/`. (Used `127.0.0.1:38080`, artifacts in `Outputs/Logs/`.)

- [x] Define the DOT job artifact.
- [x] Create a committed DOT file (for example `Components/workflows/first_real_call.dot`) with explicit `start` and `exit` nodes.
- [x] Validate DOT parseability with Attractor-compatible syntax (node shapes and edges). (Run accepted by `POST /pipelines`.)
- [x] Record artifact identity (`jj log -r @` changeset, file path, and optional `sha256sum`).

- [x] Start Attractor server.
- [x] Launch from `Inputs/untyped/brynary-attractor/attractor` via `bun run bin/attractor-server.ts`.
- [x] Pin host/port (`ATTRACTOR_HOST=127.0.0.1`, `ATTRACTOR_PORT=<port>`).
- [x] Capture server stdout/stderr logs to a run log file.
- [x] Confirm port is listening before submitting job.

- [x] Create immutable request payload file.
- [x] Write JSON payload file containing exactly `{ "dot": "<digraph ...>" }`.
- [x] Store payload in a tracked path (for example `Outputs/Logs/attractor-first-call-request.json`).
- [x] Do not pass DOT via ad-hoc env vars; use the data file as source of truth.

- [x] Submit first real pipeline request.
- [x] `POST /pipelines` with `curl --data-binary @<request.json>`.
- [x] Persist response body to `Outputs/Logs/attractor-first-call-create-response.json`.
- [x] Extract and record `pipeline_id`.

- [x] Observe run to completion.
- [x] Poll `GET /pipelines/:id` until terminal state.
- [x] Persist final status response.
- [x] If pipeline blocks on human input, query `GET /pipelines/:id/questions` and answer with `POST /pipelines/:id/questions/:qid/answer`; persist each request/response. (Not required for this minimal start->exit run.)

- [x] Capture audit artifacts.
- [x] Fetch and save `GET /pipelines/:id/graph` output.
- [x] Fetch and save `GET /pipelines/:id/context` output.
- [x] Fetch and save `GET /pipelines/:id/checkpoint` output.
- [x] Optionally capture SSE transcript from `GET /pipelines/:id/events`. (Captured file is empty for this fast run.)

- [x] Verify success criteria.
- [x] Pipeline reaches expected terminal state (`completed` for happy-path run).
- [x] Graph/context/checkpoint artifacts are non-empty and consistent with the submitted DOT.
- [x] Handoff is reproducible from committed DOT + committed/persisted request/response files.

- [x] Close out with provenance.
- [x] Commit all run artifacts and any workflow/doc updates atomically.
- [x] Commit message references Goal 1 and pipeline id.
- [x] Add a short run summary to `Library/RESTART_CONTEXT.md` (date, commit id, artifact paths).

## Run Record

- Date: 2026-02-20
- Pipeline ID: `7746ead5-72fd-4821-92e3-9dfab8bd04f1`
- Final status: `completed`
- DOT artifact: `Components/workflows/first_real_call.dot`
- Request payload: `Outputs/Logs/attractor-first-call-request.json`
- Server log: `Outputs/Logs/attractor-first-call-server.log`
- Persisted responses:
  - `Outputs/Logs/attractor-first-call-create-response.json`
  - `Outputs/Logs/attractor-first-call-status-last.json`
  - `Outputs/Logs/attractor-first-call-context.json`
  - `Outputs/Logs/attractor-first-call-checkpoint.json`
  - `Outputs/Logs/attractor-first-call-graph.out`
  - `Outputs/Logs/attractor-first-call-events.sse`
- SHA-256:
  - `Components/workflows/first_real_call.dot`: `1714ddf4cdd0d768e93e55f740f99bc236a66e7109bb3258a3305875316dd266`
  - `Outputs/Logs/attractor-first-call-request.json`: `9f6790a72b155d9a85d5bf2fee1099216ef5bb5a43455b185fbc2131e0e772d0`
  - `Outputs/Logs/attractor-first-call-create-response.json`: `bc8543c3be41353cce83dcbeaed222c2ac7765da90be74e061190cee1bedc484`

## Suggested Minimal Command Set

```bash
# Start server
(cd Inputs/untyped/brynary-attractor/attractor && \
  ATTRACTOR_HOST=127.0.0.1 ATTRACTOR_PORT=38080 bun run bin/attractor-server.ts)

# Create pipeline
curl -sS -X POST "http://127.0.0.1:38080/pipelines" \
  -H "Content-Type: application/json" \
  --data-binary @Outputs/Logs/attractor-first-call-request.json

# Poll status
curl -sS "http://127.0.0.1:38080/pipelines/<id>"

# Fetch artifacts
curl -sS "http://127.0.0.1:38080/pipelines/<id>/graph"
curl -sS "http://127.0.0.1:38080/pipelines/<id>/context"
curl -sS "http://127.0.0.1:38080/pipelines/<id>/checkpoint"
```
