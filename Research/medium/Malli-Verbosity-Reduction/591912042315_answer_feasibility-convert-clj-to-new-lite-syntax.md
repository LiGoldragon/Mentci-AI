# Agent Report

- Date: `2026-02-22`
- Kind: `answer`
- Change Scope: `modified-files`
- Subject: `Malli-Verbosity-Reduction`
- Title: `Feasibility: Converting Clojure Scripts to New Lite Syntax`

## Prompt

feasability of converting clj to new lite syntax

## Verdict

**Feasible, but only as phased migration (not big-bang).**

## Current State Snapshot

- `main` macro already supports Malli lite input forms in concise mode.
- `defn*` is still the dominant function declaration mechanism across scripts.
- Script validation currently enforces `defn*` usage, which couples migration pace to validator updates.
- `defobj` / `impl` are still strategic targets, not implemented baseline macros.

## Feasibility Dimensions

### 1) Entrypoint conversion (`-main`)
- Status: **high feasibility now**.
- Reason: `main` macro exists and supports both symbol schemas and lite map syntax.
- Risk: low, if converted per-script with tests.

### 2) General function signature conversion
- Status: **medium feasibility**.
- Reason: no finalized replacement for `defn*` yet; prior `fn` naming collided with Clojure special form.
- Required before broad conversion: settle sanctioned macro API (`defn*` retained or replacement introduced).

### 3) Schema declaration conversion (map/vector -> lite authoring)
- Status: **medium-high feasibility**.
- Reason: Malli lite can represent map/shape definitions concisely.
- Risks: readability drift, debug opacity, and accidental semantic changes if transformation is mechanical.

### 4) Full-repo migration in one pass
- Status: **not advisable**.
- Reason: validator coupling + naming policy shifts + macro surface still evolving.

## Main Blockers

1. Validator hard requirement on `defn*` usage (`execute`).
2. Missing canonical replacement macros for all target forms (`defobj`, `impl` not yet baseline).
3. Need for naming-policy enforcement during migration (`Input` in `main.clj`, no redundant flow/type names).

## Recommended Migration Plan

1. **Stabilize macro surface first**
- Keep `defn*` for non-entrypoint functions.
- Use `main` for entrypoints.
- Implement/confirm `defobj` and `impl` before broader syntax shift.

2. **Update validator policy**
- Accept sanctioned macro set instead of only `defn*` string checks.
- Add rule checks for naming/context-local constraints.

3. **Pilot batch**
- Convert 1-2 low-risk scripts end-to-end.
- Measure diff readability, runtime behavior, and error clarity.

4. **Incremental rollout**
- Convert script-by-script with review gates.
- Avoid mass regex rewrite.

## Conversion Feasibility Score (Practical)

- Entrypoints: **8/10**
- General functions: **5/10** (until macro/validator convergence)
- Schema authoring: **7/10**
- Whole-repo immediate conversion: **3/10**

## Bottom Line

Converting `clj` to the new lite syntax is feasible and worthwhile, but only under staged governance:
- stabilize macros,
- align validator,
- pilot,
- then expand.

## Reporting Protocol Notes

- This report is required for `answer`, `draft`, and `question` responses.
- Reporting still applies when response scope is `no-files`.
