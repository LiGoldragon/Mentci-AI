# Reports

Prompt-level agent answer artifacts are stored in this directory.

## Required Coverage
- `answer`: standard completion response.
- `draft`: partial/proposed response.
- `question`: question-only or advisory response.

Reports are required even when no repository files are modified (`change-scope: no-files`).

## Filename Contract
Use chronographic naming:

`YEARSIGNDEGREEMINUTESECOND_KIND_TITLE.md`

Generated via:

`bb scripts/answer_report/main.clj`


## Subject Topics
Topics are subdirectory names under `Reports/` (for example `Reports/Prompt-Report-System/`).
Each topic must have a corresponding `strategies/<Subject>/` directory.
