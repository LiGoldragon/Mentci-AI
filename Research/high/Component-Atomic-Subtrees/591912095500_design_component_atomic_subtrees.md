# Research: Component Atomic Subtrees and Version Control for Contracts

- **Solar:** 5919.12.9.55.0
- **Subject:** `Component-Atomic-Subtrees`
- **Status:** `design-draft`
- **Weight:** `High` (Direct author intent from transcript)

## 1. Intent
To design an architecture where system components are treated as atomic, version-controlled subtrees (JJ/Git) with explicit, decoupled contracts (Cap'n Proto) that enable decentralized build logic and parallel agent work.

## 2. The "Genius" Realization: The Node is the Contract
The transcript identifies a fundamental architectural insight: by making a component's contract (Cap'n Proto specification) its own independent, versioned repository (or atomic subtree), the contract itself becomes the "node" in the system graph.

### 2.1 Benefits
- **Decentralized Builds:** If a component's implementation changes but its contract remains identical, downstream dependencies do not need to be rebuilt.
- **Parallel Workflows:** One agent can work on a "Proposal" branch of a contract while another agent works on the implementation of a separate component, coordinated by a global job controller.
- **Atomic File System:** Using Jujutsu (JJ) ensures that transitions between contract versions are atomic and inspectable.

## 3. Proposed Structure: Contracts vs Specifications
- **Name:** Rebranding "Specifications" to **Contracts**.
- **Organization:** 
    - `Contracts/Current/`: The active, verified Cap'n Proto schemas.
    - `Contracts/Proposed/`: Proposed changes pushed to branches for build-triggering and verification.
    - `Contracts/History/`: Durable record of past versions.

## 4. Automation & Job Control
A high-level **Job Controller** (managed by an "Accountant" logic for priority/cost analysis) will:
1. Detect a new "Proposed" contract branch.
2. Spin up a specialized agent to verify the proposal against existing implementations.
3. Trigger rebuilds of only the directly affected components, avoiding massive re-compilation.

## 5. Summary of Mechanical Task Automation
As noted in the transcript: "The machine is gonna be the same. Its components are gonna be organized in a certain pattern... it evolves with its psyche users." This architecture formalizes the "ligaments" of the machine, allowing intelligence to be automated where the physical structure (the contract) dictates the outcome.
