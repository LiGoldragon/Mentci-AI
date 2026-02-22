# Agent Report

- Chronography: `12.4.53.20 | 5919 AM`
- Kind: `answer`
- Change Scope: `no-files`
- Subject: `Workspace-Pwd-History-Purge-And-Jj-Config-Migration`
- Title: `workspace-ancient-copy-root-cause`

## Prompt

create a report on workspace/ possibly being a wrongly-commited ancient copy of mentci-ai itself, and finding the root cause

## Agent Answer

Findings:
- `workspace/` is not a separate Git repository (`workspace has no .git dir`), but it is a separate JJ workspace (`jj rev-parse` true from inside `workspace/` and local `.jj/` present).
- File timestamps in `workspace/` cluster around Feb 19, while primary repo has much newer edits, indicating stale snapshot drift.
- The directory mirrors an older Mentci layout (legacy lowercase roots and older doc placements), consistent with a historical workspace bootstrap snapshot.

Root cause (most likely):
- Jail tooling intentionally creates a writable JJ workspace at `workspace/` (`jj workspace add`) for isolated implementation and shipping.
- Long-lived workspace content was not periodically reconciled/recreated from current trunk, so stale tree state accumulated and appears like an ancient copy.

Contributing factors:
- `.gitignore` excludes `workspace/`, reducing visibility in normal VCS review.
- Runtime tooling references `workspace/.mentci/runtime.json`, reinforcing persistent workspace usage.

Recommended corrective strategy:
1. Treat `workspace/` as ephemeral implementation state only.
2. Add a freshness guard: compare workspace head/change-age against trunk on startup.
3. Rebuild workspace automatically when stale beyond threshold or root layout mismatch is detected.
4. Keep durable artifacts in canonical roots, never in `workspace/`.

## Reporting Protocol Notes

- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.
