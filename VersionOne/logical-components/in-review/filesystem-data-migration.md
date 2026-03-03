# Filesystem-to-Data Migration

## Review Focus
Define staged migration from path-centric mutation to message/data-centric mutation.

## Current Baseline
Filesystem remains the immediate mutation surface for implementation repositories.

## Transition Objective
Increase percentage of operational state represented and replayed through Saṃskāra objects, reducing implicit filesystem coupling.

## Active Review Topics
- migration checkpoints per component,
- compatibility with existing JJ workflows,
- storage and replay strategy for message-led state.
