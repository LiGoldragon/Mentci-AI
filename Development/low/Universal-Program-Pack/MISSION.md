# Mission: Universal Program Pack

## Objective
Fork out reusable orchestration and protocol assets from Mentci core into a universal package that can be mounted in any repository using canonical filesystem paths.

## Desired End State
- One portable root directory contains reusable programs, strategies, and protocol docs.
- Host repositories add their own domain programs and strategies without editing the universal pack.
- Path contracts are stable and machine-discoverable.

## Name Recommendation
`ProgramPack/` is the recommended single directory name.

Rationale:
- Neutral across domains.
- Clear intent: a portable pack of programs and guidance.
- Short and easy to reference in canonical path contracts.
