# VersionOne Saṃskārad CozoScript Examples — Implementation Notes

## Scope
Implemented a first executable CozoScript query surface for VersionOne semantic data in `samskarad`.

## What Was Added
- New semantic relations in `samskarad` schema:
  - `lane_policy`
  - `agent_role`
  - `tx_log`
  - `statement`
- Seed sidecars under `Components/samskara/data/` for each relation.
- VersionOne protocol document containing agent-to-CozoScript query examples:
  - `VersionOne/samskara-layer/protocol/cozoscript-agent-queries.md`
- Integration test validating that all six example queries execute and return rows.

## Important Debugging Findings
1. **Single script with multiple `?` rule definitions failed** in Cozo with:
   - `The rule '?' cannot have multiple definitions since it contains non-Horn clauses`
   - Resolution: split seeds into multiple `.cozo` files, one entry/query per script.
2. **Named rules without a `?` entry failed** with:
   - `Program has no entry`
   - Resolution: keep each seed script in `?[...] <- [...]` + `:put ...` shape.
3. **Descending order syntax** accepted by this Cozo dialect is `:order -field`, not `:order field desc`.

## Result
The query examples are now executable in tests, and `samskarad` bootstraps VersionOne-oriented semantic relations from sidecar seed scripts.
