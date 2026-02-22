# Attractor API and Use-Cases

This document records the Attractor API surface currently referenced by Mentci-AI and the primary use-cases it enables.

Mentci-AI incorporates the Attractor standard and API patterns. The reference implementation is read-only under:
- `inputs/untyped/brynary-attractor/attractor`

## 1. API Surfaces

Attractor is exposed through two primary interfaces:
- TypeScript runtime/library API
- HTTP server API

### 1.1 TypeScript Runtime API

Primary exports are available from:
- `inputs/untyped/brynary-attractor/attractor/src/index.ts`

Core entry points:
- `parse(dot: string)` parses DOT into a graph object.
- `PipelineRunner` executes a parsed graph.
- `createHandlerRegistry()` creates handler registration for node execution.
- `PipelineEventEmitter` streams lifecycle events.
- `createServer(config)` starts the HTTP API server.

Handler exports:
- `StartHandler`
- `ExitHandler`
- `CodergenHandler`
- `WaitForHumanHandler`
- `ConditionalHandler`
- `ParallelHandler`
- `FanInHandler`
- `ToolHandler`
- `ManagerLoopHandler`
- `SubPipelineHandler`

Backend exports:
- `StubBackend`
- `SessionBackend`
- `CliAgentBackend`
- `ClaudeCodeBackend`
- `CodexBackend`
- `GeminiBackend`

Interviewer exports:
- `AutoApproveInterviewer`
- `ConsoleInterviewer`
- `CallbackInterviewer`
- `QueueInterviewer`
- `RecordingInterviewer`
- `WebInterviewer`

### 1.2 DOT Node-Type Mapping API

Canonical shape-to-handler mapping is documented in:
- `inputs/untyped/brynary-attractor/attractor/README.md`
- `inputs/untyped/brynary-attractor/docs/specs/attractor-spec.md`

Key mappings:
- `Mdiamond` -> `start`
- `Msquare` -> `exit`
- `box` -> `codergen`
- `hexagon` -> `wait.human`
- `diamond` -> `conditional`
- `component` -> `parallel`
- `tripleoctagon` -> `parallel.fan_in`
- `parallelogram` -> `tool`
- `house` -> `stack.manager_loop`

### 1.3 HTTP Server API

Reference route implementation:
- `inputs/untyped/brynary-attractor/attractor/src/server/routes.ts`

Base resource:
- `/pipelines`

Endpoints:
1. `POST /pipelines`
- Purpose: Create and start a pipeline run from DOT source.
- Request body: `{ "dot": "<digraph ...>" }`
- Success: `201` with `{ "id": "<uuid>", "status": "running" }`
- Errors: `400` invalid JSON, missing/invalid `dot`, or DOT parse failure.

2. `GET /pipelines/:id`
- Purpose: Fetch run status summary.
- Success: `200` with run status and optional result fields.
- Errors: `404` unknown pipeline id.

3. `GET /pipelines/:id/events`
- Purpose: Stream run events via SSE.
- Content-Type: `text/event-stream`
- Errors: `404` unknown pipeline id.

4. `POST /pipelines/:id/cancel`
- Purpose: Cancel an active run.
- Success: `200` with status `cancelled`.
- Errors: `404` unknown id, `409` if run is not active.

5. `GET /pipelines/:id/questions`
- Purpose: Get pending human question for `wait.human`.
- Success: `200` with question payload or `question: null`.
- Errors: `404` unknown pipeline id.

6. `POST /pipelines/:id/questions/:qid/answer`
- Purpose: Submit human answer.
- Request body: `{ "value": "<choice>", "text": "<optional free text>" }`
- Success: `200` with `{ "submitted": true }`
- Errors: `400` invalid body, `404` unknown id, `409` no pending question or mismatched `qid`.

7. `GET /pipelines/:id/graph`
- Purpose: Retrieve run graph as SVG when Graphviz is available; otherwise raw DOT.
- Success: `200` with `image/svg+xml` or `text/vnd.graphviz`.
- Errors: `404` unknown pipeline id.

8. `GET /pipelines/:id/context`
- Purpose: Fetch current/final context snapshot.
- Success: `200` with `{ "context": { ... } }`
- Errors: `404` unknown pipeline id.

9. `GET /pipelines/:id/checkpoint`
- Purpose: Fetch current/final checkpoint summary.
- Success: `200` with checkpoint object or `null`.
- Errors: `404` unknown pipeline id.

## 2. Documented Use-Cases

The following use-cases are explicitly supported by the documented Attractor model and current reference routes/handlers.

1. Deterministic DOT job execution
- Input is a versioned DOT graph.
- Execution proceeds from `start` to `exit` through typed node handlers.

2. LLM task orchestration
- `codergen` nodes execute prompts through pluggable backends.
- Backend swap does not require DOT structure changes.

3. Human approval and steering
- `wait.human` nodes pause execution and request operator input.
- Operator decisions are submitted through interviewer interfaces or HTTP question/answer routes.

4. Conditional retry loops
- `conditional` nodes route based on outcome/context conditions.
- Supports validation loops such as implement -> test -> rework.

5. Parallel fan-out and fan-in
- `parallel` and `parallel.fan_in` nodes support concurrent branch execution and merge.

6. Tool invocation stages
- `tool` nodes run external tool actions as explicit workflow steps.

7. Supervisor and child pipeline orchestration
- `stack.manager_loop` and `sub_pipeline` patterns enable parent/child pipeline control via child DOT artifacts.

8. Remote run control and observability
- HTTP API supports run submission, status reads, cancellation, graph retrieval, context/checkpoint reads, and SSE event streaming.

## 3. Mentci Integration Direction

Current high-level goal alignment:
- `docs/architecture/HIGH_LEVEL_GOALS.md` Goal 1 requires DOT job handoff to Attractor.

Required integration stance:
- Treat Attractor API contracts as external reference contracts.
- Keep Mentci orchestration data auditable and deterministic when constructing DOT job artifacts and invoking Attractor.

## 4. Achieved Milestone: First Gemini Editing Job

Date:
- 2026-02-20

Milestone status:
- Achieved

Recorded runs:
1. Gemini codergen validation run
- Pipeline id: `1bcfe935-12ec-449c-82d6-1a724b8b66b9`
- Outcome: `completed` / `success`
- Evidence: context `last_response` was `GEMINI_OK`

2. First Attractor DOT repo-editing job (Gemini + tool stages)
- Pipeline id: `b378d76b-7962-46bf-a770-f5ca8701d9ca`
- Outcome: `completed` / `success`
- Completed nodes: `start`, `plan`, `format`, `test`, `commit`
- Job shape: `codergen` planning stage + `tool` execution stages (`format`, `test`, conditional `jj commit`)
- Commit-stage result: no-op when no diff remained (`No style changes to commit`)

Operational note:
- Gemini was executed through Attractor `GeminiBackend` (CLI subprocess path), using the locally authenticated CLI session.
- No repository secrets were written into versioned files as part of this milestone.
