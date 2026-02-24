# High-Level Goals

This file defines durable, repository-level goals that guide orchestration and implementation priorities.

## Goal 0: mentci-aid Stabilization

Establish **mentci-aid** as a functional, Level 5 autonomous pipeline daemon.

Required outcome:
- `mentci-aid` reaches a "Running State".
- It successfully orchestrates complex DAGs within Nix Jails.
- **[DONE]** Native Rust actor-based orchestration layer (`execute`) implemented.
- **[DONE]** Logic-Data Separation enforced via EDN sidecars.
- **[DONE]** Full Repository Sweep complete (Sections 1-10).
- **Assimilation:** Attractor (StrongDM) and Attractor-Docs (Brynary) are fully assimilated.
- **R&D Mirror Integrity:** `Development/` and `Research/` remain mirrored by subject so execution guidance and traceable findings stay aligned.
- **Control-Gate Integrity:** session and root guard health are maintained as release-blocking gates.
- **Init Envelope Purity:** runtime initialization state is transmitted as Cap'n Proto init message objects, not env-scattered domain config.
- **Terminalized Box Launch Path:** a dedicated launcher component strategy exists for systemd-managed Mentci-Box terminal sessions.

## Goal 1: Attractor DOT Job Handoff

Pass a job to Attractor as a DOT graph artifact.

Task file:
- `Components/tasks/high_level_goals/goal_1_attractor_dot_job_handoff.md`

Required outcome:
- A job intent can be represented as a DOT graph document.
- The DOT graph is passed to Attractor as the job input artifact.
- The handoff path is deterministic and auditable.
