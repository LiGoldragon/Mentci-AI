# VT Code Inspection Roadmap and High-Value Prospects (Mentci)

## Scope
This research records VT Code inspection as a **future task**, then identifies the highest value-per-work opportunities for Mentci based on available VT Code docs/source shape.

- Local source mounted at: `Sources/vtcode -> /home/li/git/vtcode`
- Upstream: `vinhnx/vtcode`

## Future Task: Deep VT Code Inspection

### Task ID
`future/vtcode-inspection-01`

### Objective
Run a focused architecture+implementation inspection of VT Code to extract reusable patterns for Mentci (without cargo-culting UX decisions).

### Proposed Inspection Passes
1. **Security and execution policy internals**
   - shell validation path
   - sandbox policy implementation (Landlock/seccomp path assumptions)
   - policy evaluation ordering
2. **Semantic tooling internals**
   - tree-sitter/ast-grep orchestration boundaries
   - index/update invalidation behavior
3. **Protocol surfaces**
   - ACP/MCP/A2A/Open Responses boundaries and adapters
4. **Config/runtime ergonomics**
   - `vtcode.toml` precedence and lifecycle hooks
5. **Operational telemetry**
   - error/report channels and audit surfaces

### Deliverables
- One assimilation matrix (`adopt / adapt / reject`) per subsystem
- One risk report for shell+sandbox semantics
- One concrete Mentci implementation plan (Rust + Cap'n Proto)

## High Value-per-Work Prospects for Mentci

1. **Policy engine hardening first** (highest ROI)
   - Formalize tool `allow/prompt/deny` matrix with deterministic evaluation order.
   - Reason: immediate safety and predictability gains without large UX churn.

2. **Shell execution boundary hardening**
   - Add argument-level validation contracts before command execution.
   - Reason: closes common injection/error classes quickly.

3. **Lifecycle hooks as policy extension points**
   - Add hook phases for pre-tool / post-tool / pre-finalize checks.
   - Reason: cheap extensibility; offloads custom governance from core logic.

4. **Semantic code-intel substrate cleanup**
   - Consolidate structural edit + semantic navigation behind one Rust boundary API.
   - Reason: removes duplicated logic and improves reliability of transformations.

5. **Protocol gateway normalization**
   - Keep one MCP gateway boundary in Mentci for decisioning and result codes.
   - Reason: avoids fragmented policy logic across extensions.

## Suggestions (Actionable)

1. Create `Components/mentci-policy-engine` decision table tests first (allow/warn/block matrix).
2. Define Cap'n Proto schema for policy decisions and execution audit events.
3. Add explicit command classification (`read-only`, `mutating`, `networked`, `destructive`) and map policies by class.
4. Add deterministic reason codes in tool responses (`POLICY_BLOCK_*`, `POLICY_WARN_*`).
5. Keep VT Code feature borrowing selective: adopt safety/semantic architecture, not full behavior parity.

## Questions for Author (Intent Guidance)

1. **Priority intent:** Should Mentci prioritize safety hardening before UX parity with VT Code?
2. **Scope boundary:** Do you want only terminal-agent parity, or also editor protocol parity (ACP/MCP/A2A exposure)?
3. **Policy strictness:** Default posture for mutating/network tools: `prompt` or `deny`?
4. **Assimilation depth:** Do you want direct pattern extraction from VT Code modules, or only conceptual borrowing with fresh implementation?
5. **Roadmap sequence:** Should we schedule VT Code inspection before further superpowers assimilation work, or after policy-engine MVP?
6. **Success metric:** What is the primary KPI for this track: safety incidents reduced, edit reliability, or session throughput?

## Proposed Next Step
Create a dedicated inspection plan doc under:
`Development/high/VTCode-Inspection/`
with explicit checkpoints and subagent split.
