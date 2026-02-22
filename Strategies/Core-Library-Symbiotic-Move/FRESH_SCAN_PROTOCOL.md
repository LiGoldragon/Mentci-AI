# Fresh Scan Protocol (Change-ID Anchored)

## Objective
Ensure next-prompt implementation uses a fresh, trustworthy affected-path inventory without rescanning blindly.

## Anchor Data
- Baseline migration change id: `ltkttsyqmozk`
- Parent pre-migration change id: `xzlswptlmxkn`
- Snapshot file: `strategies/Core-Library-Symbiotic-Move/FRESH_AFFECTED_PARTS.md`

## Procedure
1. Read baseline change id from this protocol and snapshot.
2. Verify branch freshness:
   - `jj log -r @ --no-graph -T 'change_id.short()'`
3. Compare with baseline `ltkttsyqmozk`.
4. If equal: reuse the snapshot list directly (no discovery search required).
5. If different: regenerate snapshot and update this protocol in a new commit.

## Freshness Rule
A path inventory is **fresh** only when it is linked to the current change id in branch head.

## Minimum Regeneration Set
- `jj diff --summary -r '@-..@'`
- `rg -n 'Core/|Library/' -g'!target' -g'!.git' -g'!.jj'`
- Update `generated_from` and `parent_change` in snapshot.
