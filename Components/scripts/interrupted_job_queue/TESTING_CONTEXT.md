# Testing Context: interrupted_job_queue

## Purpose
Recover interrupted prompts from the interruption audit report and materialize a
prioritized queue artifact for execution.

## Quick Checks
- `bb Components/scripts/interrupted_job_queue/main.clj`
- `bb Components/scripts/interrupted_job_queue/main.clj --write`
- `bb Components/scripts/interrupted_job_queue/main.clj --source-report Research/high/Prompt-Report-System/591912042315_answer_interrupted-jobs-audit.md --output Research/high/Prompt-Report-System/InterruptedJobQueue.md --write`
- `bb Components/scripts/interrupted_job_queue/test.clj`

## Expected Result
- Dry run prints recovered prompt/job counts.
- `--write` creates/updates `Research/high/Prompt-Report-System/InterruptedJobQueue.md`.
- Queue rows include subject, class, priority, and acceptance criteria.
