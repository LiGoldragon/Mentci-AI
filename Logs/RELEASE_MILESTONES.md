# Mentci-AI: Release Milestones and Goal Logs

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `core/ASKI_POSITIONING.md`.

This file maintains the durable record of session milestones and goal outcomes since the last major release tag.

## 4.1 Latest Goal Run (Goal 1)
*   **Date:** 2026-02-20
*   **Goal:** Attractor DOT Job Handoff (first real call)
*   **Pipeline ID:** `7746ead5-72fd-4821-92e3-9dfab8bd04f1`
*   **Final Status:** `completed`
*   **DOT Artifact:** `workflows/first_real_call.dot`
*   **Run Artifacts:**
    *   `Logs/attractor-first-call-request.json`
    *   `Logs/attractor-first-call-create-response.json`
    *   `Logs/attractor-first-call-status-last.json`
    *   `Logs/attractor-first-call-graph.out`
    *   `Logs/attractor-first-call-context.json`
    *   `Logs/attractor-first-call-checkpoint.json`
    *   `Logs/attractor-first-call-server.log`

## 4.2 Latest Attractor Editing Milestone
*   **Date:** 2026-02-20
*   **Milestone:** First Gemini-backed Attractor repo-editing DOT job completed
*   **Pipeline ID:** `b378d76b-7962-46bf-a770-f5ca8701d9ca`
*   **Final Status:** `completed` (`success`)
*   **Path:** `start -> plan(codergen) -> format(tool) -> test(tool) -> commit(tool) -> exit`
*   **Commit Stage Result:** `No style changes to commit` (safe no-op when no diff existed)

## 4.3 mentci-aid Identification Milestone
*   **Date:** 2026-02-21
*   **Milestone:** Formally identified core Rust logic as **mentci-aid** and established "Not in a Running State" as canonical baseline.
*   **Version:** `v0.12.3.45.53`

## 4.4 Inputs Mapping and Launcher Improvement
*   **Date:** 2026-02-21
*   **Milestone:** Fixed `attractor` (StrongDM) vs `attractor-docs` (Brynary) mapping. Improved launcher to mount `srcPath` (Nix store).
*   **Version:** `v0.12.3.48.16`

## 4.5 Language Hierarchy and Assimilation
*   **Date:** 2026-02-21
*   **Milestone:** Established Language Authority (Aski > Rust > Clojure > Nix) and mandated assimilation of Attractor inputs.
*   **Version:** `v0.12.3.50.22`

## 4.6 Automatic Loading of Authority Sources
*   **Date:** 2026-02-21
*   **Milestone:** Strengthened Enforcement Contract to mandate automatic loading of architectural guidelines (including Clojure).
*   **Version:** `v0.12.3.52.14`

## 4.7 Chronos Accuracy and Notation Fix
*   **Date:** 2026-02-21
*   **Milestone:** Fixed chronos inaccuracy by adding planetary perturbations. Confirmed algorithmic error (not notation shift) was the source of discrepancy observed by Li Goldragon. Introduced `--notation standard` for easier verification.
*   **Version:** `v0.12.3.53.1`

## 4.8 Programming Version System (Prototype)
*   **Date:** 2026-02-21
*   **Milestone:** Moved core architectural files to `core/`. Developed `scripts/program_version.clj` to generate content-addressed "Programming Version" hashes (jj-style). Mandated version signature in agent responses.
*   **Version:** `v0.12.3.55.3`

## 4.9 Chronos Formatting Fix
*   **Date:** 2026-02-21
*   **Milestone:** Removed spaces from Chronos Unicode output for higher density (e.g., `♓︎3°50'24"`).
*   **Version:** `v0.12.3.56.12`

## 4.10 Developer Environment: Direnv and Emacs
*   **Date:** 2026-02-21
*   **Milestone:** Setup `nix-direnv` (`.envrc`). Created `tools/emacs/mentci.el` for automated Read-Only management of `Inputs/`, auto-revert, and git-gutter integration.
*   **Version:** `v0.12.3.57.45`
