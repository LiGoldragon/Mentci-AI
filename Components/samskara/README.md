# Saṃskāra (संस्कार)

## Overview
**Saṃskāra** is the bridge between the legacy Unix world (raw text, streams, untyped IPC) and the Mentci world of specified, structured contract channels (Cap'n Proto, EDN, Aski).

The name is derived from the Sanskrit *saṃskāra*, meaning "to put together," "to refine," or "to perfect." It represents the process of taking ad-hoc, "self-cooked" syntax and samskarizing it into a durable, structured impression.

## Mission
1. **Bridge the Gap:** Ingest raw Unix output (stdout/stderr, file streams) and project it into schema-validated Cap'n Proto messages.
2. **Contract Enforcement:** Ensure that any communication passing through the Saṃskāra bridge is hash-synced and protocol-compliant.
3. **Refinement:** Provide the logic to "polish" emergent data patterns into formal schemas.

## Technical Details
- **IAST Name:** Saṃskāra (Note the **diacritics**: the dot under the 'm' (bindu) and the line over the 'a' (macron)).
- **Core Implementation:** Rust.
- **Contract Format:** Cap'n Proto + EDN.
