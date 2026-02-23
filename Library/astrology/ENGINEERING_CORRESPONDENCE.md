# Engineering Correspondence (Book of Sol)

## Scope
Operational mapping from zodiac/planetary symbolism to engineering and software design decisions.

Primary source anchors:
- `Sources/TheBookOfSol/The_Zodiac.md`
- `Sources/TheBookOfSol/The_Solar_Matrix_of_Creation.md`
- `Sources/TheBookOfSol/Sidereal.md`
- `Sources/TheBookOfSol/1-Sol.md`
- `Sources/TheBookOfSol/2-Luna.md`

## Core Model
- Zodiac signs are treated as functional phases of system lifecycle.
- Planets/luminaries are treated as modifiers on decision style and execution quality.
- Sol/Luna polarity is used as architecture balance:
  - Sol: coherence, center, invariant, directional intent.
  - Luna: adaptation, reflection, buffering, cadence.

## Zodiac -> Engineering / Software Design

| Sign | System Function | Engineering Focus | Software Design Pattern | Failure Mode | Corrective Action |
| --- | --- | --- | --- | --- | --- |
| Aries | Initiation | Prototype spike, first runnable path | Thin vertical slice | Premature scale-up | Keep strict scope + demo criterion |
| Taurus | Stabilization | Reliability, repeatability, infra durability | Hardened baseline + golden path | Inertia, resistance to needed change | Add scheduled refactor windows |
| Gemini | Relation | Interfaces, protocol translation, docs | Adapter/facade boundaries | Fragmented naming, duplicate APIs | Canonical interface glossary |
| Cancer | Continuity | State continuity, handoff, backups | Checkpoint/replay, resumable workflows | Context loss, brittle restarts | Durable context snapshots |
| Leo | Centering | Product/core authority, decision ownership | Single owner for critical path | Hero bottleneck | Explicit delegation matrix |
| Virgo | Refinement | Testing, linting, schema rigor | Validation-first pipelines | Perfection loop | Define "good enough" gates |
| Libra | Balance | Tradeoffs, coupling boundaries | Contract-first negotiation | Endless debate | Timeboxed ADR with decision owner |
| Scorpio | Transformation | Incidents, migrations, deep fixes | Controlled rewrites + reversible migrations | Destructive fix without lineage | Reversible steps + audit trail |
| Sagittarius | Expansion | Exploration, research, new capability | Experimental branch with kill criteria | Scope explosion | Research budget + cutoff date |
| Capricorn | Execution | Delivery discipline, operational runbooks | Release trains + SLO-driven ops | Bureaucratic drag | Tie process step to outcome metric |
| Aquarius | Systemic redesign | Platform architecture, future-proofing | Modular architecture + extension points | Novelty for novelty | Compatibility harness |
| Pisces | Dissolution/integration | Consolidation, deprecation, archival | Unified interface + retirement workflow | Ambiguous closure | Explicit completion checklist |

## Planetary/Luminary Modifiers

| Body | Engineering Expression | Use When | Watch For |
| --- | --- | --- | --- |
| Sol | Architectural center and non-negotiable invariants | Defining system identity, core constraints | Dogma that ignores runtime signals |
| Luna | Feedback loops, cadence, adaptation | Iterative planning, incident learning, UX iteration | Drift without stable target |
| Mercury | Communication and tooling clarity | Protocols, scripts, developer ergonomics | Excess chatter, under-delivery |
| Venus | Human experience quality | UX polish, docs readability, adoption | Surface aesthetics over correctness |
| Mars | Decisive implementation force | Migrations, urgent remediation, performance pushes | Unsafe changes without guardrails |
| Jupiter | Principle and expansion | Policy, long-range architecture, standards | Abstraction detached from execution |
| Saturn | Boundaries and durability | Security, compliance, reliability, invariants | Friction that blocks needed iteration |

## Design Application Protocol
1. Classify the current task into one primary sign (lifecycle phase).
2. Choose one dominant planetary modifier (execution style).
3. Define one explicit failure mode from the tables above.
4. Add one corrective control directly in the implementation plan.
5. Record sign/modifier/control in the report or strategy entry.

## Example Classifications
- Critical production outage: `Scorpio + Saturn`.
- New feature from zero: `Aries + Mercury`.
- Major architecture simplification: `Aquarius + Sol`.
- Stabilizing flaky CI: `Taurus + Virgo`.
- End-of-project consolidation: `Pisces + Luna`.
