# Refusal Feedback Loop Standard

## Purpose
Replace terminal denials with a productive iterative review cycle.

## Rule
Every refusal is a message object, not a dead end.

## Mandatory Refusal Payload
`RefusalFeedbackMessage` contains:
- `reasons[]` — policy-linked refusal statements,
- `suggestions[]` — concrete amendment paths,
- `questions[]` — unresolved points,
- `requiredEvidence[]` — missing validation artifacts.

## Return Direction
Refusal payload must return to the source agent that authored the request.

## Revision
Source agent may emit `RevisionResponseMessage`:
- references original request hash,
- states modifications,
- links new evidence hashes.

## Deadlock Handling
After second unresolved loop, escalate through `HumanAuditorRequired`.

## Benefit
This preserves:
- argument lineage,
- protocol learning memory,
- and high-quality inter-agent idea exchange in standardized form.
