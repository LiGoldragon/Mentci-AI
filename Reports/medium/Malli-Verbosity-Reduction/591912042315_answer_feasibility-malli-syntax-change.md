# Agent Report

- Date: `2026-02-22`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Malli-Verbosity-Reduction`
- Title: `Feasibility Report: Malli Syntax Change`

## Prompt

report on feasability of malli syntax change

## Feasibility Summary

Overall: **feasible with constraints**.

### 1) Remove explicit `:map` in schema declarations
- Status: **Feasible now**.
- Approach: local macro (`defobj`) or Malli lite wrapper that expands to canonical `[:map ...]`.
- Constraint: retain canonical expanded schema for tooling/debug visibility.

### 2) Remove map field keys (for example `:raw`)
- Status: **Not feasible without semantic breakage**.
- Reason: keys are part of object contract; removing them changes data shape and call sites.

### 3) Reduce function signature verbosity
- Status: **Feasible with constraints**.
- Approach: macro sugar (`defn1`) that expands to `[:=> [:cat InputSchema] OutputSchema]`.
- Critical constraint: output type must be explicit or deterministically inferred.
- Explicitly disallowed: defaulting to `:any`.

### 4) Methods-on-objects-first alignment
- Status: **Feasible and recommended**.
- Approach: keep protocol/record method domains primary; use signature sugar without re-centering free-function style.

## Risk Assessment

1. Experimental upstream dependency risk if hard-coupled to `malli.experimental.lite`.
2. Macro opacity risk during debugging.
3. Migration churn risk if applied repo-wide at once.

## Recommended Implementation Order

1. Implement local `defobj` and `defn1` in `Components/scripts/lib/malli.clj`.
2. Add compile-time failure for unresolved inferred output.
3. Pilot on one low-risk script.
4. Validate instrumentation and script guards.
5. Expand incrementally.

## Conclusion

Malli syntax reduction is practical and low-risk **if** expansion remains canonical, output typing remains strict, and migration is incremental.

## Reporting Protocol Notes

- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.
