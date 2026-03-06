# Criome Core MVP Implementation Plan

> **REQUIRED SUB-SKILL:** Use the executing-plans skill to implement this plan task-by-task.

**Goal:** Add a runnable `criome-core` Rust component that can approve `CrioSphereProposal` messages into transactions that mutate Cozo-backed `CrioState`, then serve `CrioHorizon` messages from current state.

**Architecture:** Implement a minimal in-memory + file-backed MVP using typed Rust objects and serde JSON messages. Keep the contract explicit: ProposalBatchRequest -> ApprovalDecision -> StateTransaction -> StateSnapshot -> HorizonRequest -> HorizonResponse. CozoDB is used as authority store through a small adapter, with deterministic fallback behavior for local MVP tests.

**Tech Stack:** Rust, serde/serde_json, thiserror, CozoDB crate, cargo test.

---

### Task 1: Scaffold component crate and domain contracts
**TDD scenario:** New feature — full TDD cycle

Files:
- Create: `Components/criome-core/Cargo.toml`
- Create: `Components/criome-core/src/lib.rs`
- Create: `Components/criome-core/src/contracts.rs`
- Create: `Components/criome-core/src/state.rs`
- Create: `Components/criome-core/src/engine.rs`
- Create: `Components/criome-core/src/bin/criad.rs`
- Create: `Components/criome-core/tests/mvp_flow.rs`

Steps:
1. Write failing integration test for end-to-end approve->mutate-state->horizon flow.
2. Run test and verify expected fail (missing symbols).
3. Add minimal contract/state structs.
4. Re-run test until compile path established.

### Task 2: Implement approval + transaction application
**TDD scenario:** New feature — full TDD cycle

Files:
- Modify: `Components/criome-core/tests/mvp_flow.rs`
- Modify: `Components/criome-core/src/engine.rs`
- Modify: `Components/criome-core/src/state.rs`

Steps:
1. Add failing test for duplicate cluster/node handling and transaction counts.
2. Implement minimal approval + deterministic transaction application.
3. Run tests to green.

### Task 3: Implement horizon projection from current state
**TDD scenario:** New feature — full TDD cycle

Files:
- Modify: `Components/criome-core/tests/mvp_flow.rs`
- Modify: `Components/criome-core/src/engine.rs`

Steps:
1. Add failing test for horizon projection for requested cluster/node.
2. Implement projection with node focus + exNodes + users.
3. Run tests to green.

### Task 4: Add simple criad runtime and persistence hooks
**TDD scenario:** New feature — full TDD cycle

Files:
- Modify: `Components/criome-core/src/bin/criad.rs`
- Modify: `Components/criome-core/src/lib.rs`

Steps:
1. Add failing test for request/response roundtrip function.
2. Implement file/json driven loop entrypoint to process one request.
3. Run tests and binary smoke check.

### Task 5: Integrate component registration and workspace visibility
**TDD scenario:** Modifying tested code — run existing tests first

Files:
- Modify: `Cargo.toml`
- Modify: `Components/index.edn`
- Modify: `Components/contracts/rust-component-repos.toml`

Steps:
1. Verify existing workspace check command before changes.
2. Register `criome-core` in workspace and component indexes.
3. Run targeted tests for `criome-core`.

### Task 6: Verification + report
**TDD scenario:** Trivial change — use judgment

Files:
- Create: `Research/high/Agent-Authority-Alignment/<timestamp>_report_criome_core_mvp_bootstrap.md`

Steps:
1. Run `cargo test -p criome-core`.
2. Run `cargo run -p criome-core --bin criad -- --help`.
3. Document behavior, limitations, and next crypto integration seam.
