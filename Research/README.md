# Research

Prompt-level research artifacts are stored in this directory.

## Required Coverage
- `answer`: standard completion response.
- `draft`: partial/proposed response.
- `question`: question-only or advisory response.

Research entries are required even when no repository files are modified (`change-scope: no-files`).

## Filename Contract
Use chronographic naming:

`YEARSIGNDEGREEMINUTESECOND_KIND_TITLE.md`

Generated via:

`bb Components/scripts/answer_report/main.clj`


## Subject Topics
Topics are tiered subdirectory names under `Research/` (for example `Research/high/Prompt-Report-System/`).
Each topic must have a corresponding `Development/<priority>/<Subject>/` directory.

## Priority Governance
Subject importance is maintained centrally in:
- `Development/high/Repo-Intent-Realignment/PRIORITY_TRIAGE.md`

Use that matrix to decide whether new report entries should:
- stay in the current subject,
- be redirected to a canonical owner subject, or
- remain parked as low-priority context.
