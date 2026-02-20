# Goal 1 Task: Attractor DOT Job Handoff

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `docs/architecture/ASKI_POSITIONING.md`.

Goal source: `docs/architecture/HIGH_LEVEL_GOALS.md` (Goal 1)

## Objective

Make the first real Attractor call using a versioned DOT artifact and verify deterministic, auditable handoff.

## Checklist

- [ ] Confirm prerequisites are available.
- [ ] Verify `inputs/untyped/brynary-attractor/attractor` exists.
- [ ] Verify tools: `bun`, `curl`, `jq`.
- [ ] Reserve a local test port and define run workspace under `workspace/` or `Logs/`.

- [ ] Define the DOT job artifact.
- [ ] Create a committed DOT file (for example `workflows/first_real_call.dot`) with explicit `start` and `exit` nodes.
- [ ] Validate DOT parseability with Attractor-compatible syntax (node shapes and edges).
- [ ] Record artifact identity (`jj log -r @` changeset, file path, and optional `sha256sum`).

- [ ] Start Attractor server.
- [ ] Launch from `inputs/untyped/brynary-attractor/attractor` via `bun run bin/attractor-server.ts`.
- [ ] Pin host/port (`ATTRACTOR_HOST=127.0.0.1`, `ATTRACTOR_PORT=<port>`).
- [ ] Capture server stdout/stderr logs to a run log file.
- [ ] Confirm port is listening before submitting job.

- [ ] Create immutable request payload file.
- [ ] Write JSON payload file containing exactly `{ "dot": "<digraph ...>" }`.
- [ ] Store payload in a tracked path (for example `Logs/attractor-first-call-request.json`).
- [ ] Do not pass DOT via ad-hoc env vars; use the data file as source of truth.

- [ ] Submit first real pipeline request.
- [ ] `POST /pipelines` with `curl --data-binary @<request.json>`.
- [ ] Persist response body to `Logs/attractor-first-call-create-response.json`.
- [ ] Extract and record `pipeline_id`.

- [ ] Observe run to completion.
- [ ] Poll `GET /pipelines/:id` until terminal state.
- [ ] Persist final status response.
- [ ] If pipeline blocks on human input, query `GET /pipelines/:id/questions` and answer with `POST /pipelines/:id/questions/:qid/answer`; persist each request/response.

- [ ] Capture audit artifacts.
- [ ] Fetch and save `GET /pipelines/:id/graph` output.
- [ ] Fetch and save `GET /pipelines/:id/context` output.
- [ ] Fetch and save `GET /pipelines/:id/checkpoint` output.
- [ ] Optionally capture SSE transcript from `GET /pipelines/:id/events`.

- [ ] Verify success criteria.
- [ ] Pipeline reaches expected terminal state (`completed` for happy-path run).
- [ ] Graph/context/checkpoint artifacts are non-empty and consistent with the submitted DOT.
- [ ] Handoff is reproducible from committed DOT + committed/persisted request/response files.

- [ ] Close out with provenance.
- [ ] Commit all run artifacts and any workflow/doc updates atomically.
- [ ] Commit message references Goal 1 and pipeline id.
- [ ] Add a short run summary to `docs/guides/RESTART_CONTEXT.md` (date, commit id, artifact paths).

## Suggested Minimal Command Set

```bash
# Start server
(cd inputs/untyped/brynary-attractor/attractor && \
  ATTRACTOR_HOST=127.0.0.1 ATTRACTOR_PORT=38080 bun run bin/attractor-server.ts)

# Create pipeline
curl -sS -X POST "http://127.0.0.1:38080/pipelines" \
  -H "Content-Type: application/json" \
  --data-binary @Logs/attractor-first-call-request.json

# Poll status
curl -sS "http://127.0.0.1:38080/pipelines/<id>"

# Fetch artifacts
curl -sS "http://127.0.0.1:38080/pipelines/<id>/graph"
curl -sS "http://127.0.0.1:38080/pipelines/<id>/context"
curl -sS "http://127.0.0.1:38080/pipelines/<id>/checkpoint"
```
