# High-Level Goals

This file defines durable, repository-level goals that guide orchestration and implementation priorities.

## Goal 1: Attractor DOT Job Handoff

Pass a job to Attractor as a DOT graph artifact.

Required outcome:
- A job intent can be represented as a DOT graph document.
- The DOT graph is passed to Attractor as the job input artifact.
- The handoff path is deterministic and auditable.

