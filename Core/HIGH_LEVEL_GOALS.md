# High-Level Goals

This file defines durable, repository-level goals that guide orchestration and implementation priorities.

## Goal 0: mentci-aid Stabilization

Establish **mentci-aid** as a functional, Level 5 autonomous pipeline daemon.

Required outcome:
- `mentci-aid` reaches a "Running State".
- It successfully orchestrates complex DAGs within Nix Jails.
- It adheres strictly to Sema Object Style and Handshake Logging.
- **Assimilation:** Attractor (StrongDM) and Attractor-Docs (Brynary) are fully assimilated (rewritten internally in Sema-standard Aski + Rust + Clojure + Nix) as native components.

## Goal 1: Attractor DOT Job Handoff

Pass a job to Attractor as a DOT graph artifact.

Task file:
- `tasks/high_level_goals/goal_1_attractor_dot_job_handoff.md`

Required outcome:
- A job intent can be represented as a DOT graph document.
- The DOT graph is passed to Attractor as the job input artifact.
- The handoff path is deterministic and auditable.
