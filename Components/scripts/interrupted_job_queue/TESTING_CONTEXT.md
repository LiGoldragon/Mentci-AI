# Testing Context: interrupted_job_queue

## Purpose
Recover interrupted prompts from the interruption audit report and materialize a
prioritized queue artifact for execution.

## Quick Checks
- `bb Components/scripts/interrupted_job_queue/main.clj`
- `bb Components/scripts/interrupted_job_queue/main.clj --write`
- `bb Components/scripts/interrupted_job_queue/main.clj --source-report Reports/Prompt-Report-System/591912042315_answer_interrupted-jobs-audit.md --output Reports/Prompt-Report-System/INTERRUPTED_JOB_QUEUE.md --write`
- `bb Components/scripts/interrupted_job_queue/test.clj`

## Expected Result
- Dry run prints recovered prompt/job counts.
- `--write` creates/updates `Reports/Prompt-Report-System/INTERRUPTED_JOB_QUEUE.md`.
- Queue rows include subject, class, priority, and acceptance criteria.
