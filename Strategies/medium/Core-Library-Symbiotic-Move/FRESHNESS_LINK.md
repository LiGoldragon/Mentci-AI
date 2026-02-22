# Freshness Link Commit

Purpose: this commit contains only freshness-link metadata for the previous intel commit.

Anchored parent change id: ltkttsyqmozk

Verification rule:
- The parent change id of this commit must equal the anchored parent change id above.
- If not equal, intel data is stale and must be regenerated.

Verify with:
1. jj log -r @- --no-graph -T 'change_id.short()'
2. Compare against "Anchored parent change id" in this file.
