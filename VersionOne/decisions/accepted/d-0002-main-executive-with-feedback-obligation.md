# D-0002: Main Executive Authority with Feedback Obligation

## Context
Executive authority requires clear control, but refusals must remain productive and auditable.

## Decision
`main` remains executive decision authority. Any refusal in deliberation must emit a structured feedback message to the request source.

## Consequences
- preserves executive lane governance,
- avoids terminal opaque denials,
- enforces iterative improvement loop.

## Follow-Up
Integrate refusal payload validation into governance actor tests.
