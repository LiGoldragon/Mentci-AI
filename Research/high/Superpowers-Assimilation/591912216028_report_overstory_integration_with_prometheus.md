# Overstory Integration with Mentci-AI and Prometheus

## 1. Context & Goal
We are evaluating how to integrate `jayminwest/overstory` (a multi-agent orchestrator) into Mentci-AI, specifically to leverage the upcoming `prometheus` `largeAI` node (GMKtec EVO-X2 running Ollama). We must approach this integration with Sema Object Style rigor (Logic/Data separation, Cap'n Proto specifications).

## 2. Findings from Subagent Discovery
Using `gemini-3.1-flash-lite` (and fallbacks), we successfully orchestrated parallel subagents to extract Overstory's architecture and the current state of the `prometheus` node.

### 2.1 Overstory Architecture & Adapter Model
*   **Git Worktree Isolation:** Overstory isolates each agent in its own git worktree managed by `tmux`.
*   **SQLite Coordination:** Agents communicate via a low-latency SQLite database acting as a mail system (using WAL mode).
*   **Adapter Pattern:** Overstory relies on standard environment variables to override standard endpoints. It proxies agent requests by configuring `ANTHROPIC_BASE_URL` (or similar endpoint vars) to intercept traffic, acting as a gateway.

### 2.2 The `prometheus` Node (Local LargeAI)
*   **Self-Contained Inference:** The `prometheus` node is a CriomOS `largeAI` node dedicated strictly to local LLM inference.
*   **Network Binding:** By design, the Ollama service on `prometheus` is bound exclusively to `127.0.0.1:11434`. It has no external API gateway or auth layer currently specified.

### 2.3 Overstory STEELMAN Guardrails
Overstory's own maintainers warn of severe risks in swarm orchestration:
*   **Compounding Errors & Context Loss:** Fragmented reasoning and lost architectural "chain of thought."
*   **Merge/Conflict Complexity:** Simultaneous multi-agent work in separate worktrees guarantees difficult textual and semantic merge conflicts.
*   **Cost vs. Output:** High dashboard activity ("coordination theater") that masks low actual productivity compared to a single focused agent.
*   **Blast Radius:** Multiple autonomous agents with filesystem/bash access increase the security and instability surface area.

## 3. Sema-Programmer Architecture Proposal
To safely integrate Overstory with Mentci-AI and the `prometheus` node without violating our architectural invariants, we propose a strict Logic/Data separation model using Cap'n Proto.

### 3.1 Data/Schema Lane (Cap'n Proto)
*   Instead of letting Overstory arbitrarily mutate files and manage its own opaque SQLite mail DB without schema oversight, we must specify the **Worker Coordination Protocol** in Cap'n Proto.
*   The `MentciCommit` root must be expanded to include `WorkerTask` and `WorkerMailbox` definitions. Overstory's SQLite database should be considered a local materialized cache, while the authoritative truth is serialized via Cap'n Proto.

### 3.2 Network Adapter Lane (Prometheus Gateway)
*   Since `prometheus` binds Ollama strictly to `localhost:11434`, an Overstory orchestrator running on a *different* node cannot simply point `ANTHROPIC_BASE_URL` to the `prometheus` IP.
*   **Solution:** Introduce an explicit SSH Tunnel / Socks proxy as a formalized Mentci-AI service that exposes `prometheus` Ollama to the local `tmux` session hosting Overstory, OR run Overstory directly inside a `pi` session on the `prometheus` node itself.

### 3.3 Logic Lane (Execution Guards)
*   Adhere strictly to Overstory's STEELMAN warnings: **Do not prematurely decompose tasks.** Overstory should only be invoked for "embarrassingly parallel" or "truly independent" read-heavy tasks (e.g., broad cross-repo audits) to minimize merge conflicts.
*   Implement a bounded retry logic at the Mentci adapter level to prevent runaway resource consumption from Overstory's swarms.

## 4. Subagent Defect Report
*   **Defect:** Initial calls using `gemini-2.5-flash-lite` failed because the `web-search` agent encountered "Unknown agent" errors during prompt/tooling startup.
*   **Resolution:** Switch to `gemini-3.1-flash-lite` (with `3.0` and generic `gemini` fallbacks) bypassed the immediate adapter failure and successfully fetched both the repo architecture and local file state.

## 5. Next Practical Steps
1.  **Prometheus Reachability Test:** Once `prometheus` is booted, verify we can successfully bridge its `localhost:11434` into a standard Mentci-AI Pi session.
2.  **Cap'n Proto Spec:** Draft the `OverstoryMailbox.capnp` schema to map the SQLite messaging queues into Mentci-AI's semantic substrate.
3.  **Experimental Run:** Launch a tightly bounded Overstory swarm *strictly* for read-only exploration before trusting it with `jj` commits.