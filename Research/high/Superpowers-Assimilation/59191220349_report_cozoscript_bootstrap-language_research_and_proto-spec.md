# CozoScript as Mentci-AI Bootstrap Language (Research + Proto-Spec)

## Objective
Test whether CozoScript can act as the heart bootstrap language for agent-to-agent communication in Mentci-AI, especially for logical constraints.

## Method
1. Attempted subagent-led ideation (explore/planner/task/reviewer).
2. Performed direct repository evidence scan for CozoScript/Datalog/AI-init lanes.
3. Synthesized a compact message protocol that can merge with existing spec.

## Subagent orchestration result
- Multiple subagent invocations succeeded at tool level but returned `(no output)` in capture.
- One run failed with provider/tooling lane errors (`No API key found ...`, `Unknown tool "glob"`).
- Conclusion: for this session, synthesis used verified repo evidence + direct authoring.

## Existing authority surfaces
- `VersionOne/samskara-layer/protocol/cozoscript-agent-queries.md` (query examples)
- `VersionOne/samskara-layer/representations/agent-operational-init.md` (`AI-init.cozo` contract)
- `Components/AI/src/lib.rs` (required Cozo init relation validation)
- `Components/mentci-cozo/src/lib.rs` (MVP logical-constraint handoff compiler)

## Proposed bootstrap positioning
CozoScript should be the **message substrate for agent coordination state**, not arbitrary free-form chat replacement.

### MVP boundary (recommended now)
- CozoScript for: delegation intent, logical constraints, review findings, evidence references, blocked/completion status.
- Keep natural language for: narrative explanation and human-facing summaries.

### Deferred
- Full conversational replacement.
- Global world-state synchronization and distributed conflict resolution.

## Agent-generated dialect draft (merged proposal)
A compact relation set for message exchange:
- `agent_msg`
- `agent_constraint`
- `agent_evidence`
- `agent_decision`

Message kinds in MVP:
- `delegate`
- `review`
- `evidence`
- `blocked`
- `complete`

Constraint operators in MVP:
- `eq`, `neq`, `contains`, `not_contains`, `exists`

## Usefulness verdict
- High utility for deterministic handoff contracts and logical governance.
- Strong fit with existing `AI-init.cozo` and `mentci-cozo` lanes.
- Immediate gain: machine-checkable intent + less ambiguity in multi-agent loops.

## Integration action taken
Merged this protocol shape into:
- `VersionOne/samskara-layer/protocol/cozoscript-agent-queries.md`
with concrete CozoScript message examples for delegate/review/evidence/blocked/complete.

## Next implementation suggestion
Add a minimal translator in `mentci-cozo`:
- input: JSON handoff packet
- output: CozoScript `:put` facts in the new message relations
- plus a validator query for required fields per message kind.
