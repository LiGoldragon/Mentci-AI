# Reports

Prompt-level agent answer artifacts are stored in this directory.

## Required Coverage
- `answer`: standard completion response.
- `draft`: partial/proposed response.
- `question`: question-only or advisory response.

Reports are required even when no repository files are modified (`change-scope: no-files`).

## Filename Contract
Use chronographic naming:

`YEAR_SIGN_DEGREE_MINUTE_SECOND_KIND_TITLE.md`

Generated via:

`bb scripts/answer_report/main.clj`


## Subject Topics
Subject mapping files live in `Reports/topics/`.
Each topic must have a corresponding `strategies/<subject>/` directory.
