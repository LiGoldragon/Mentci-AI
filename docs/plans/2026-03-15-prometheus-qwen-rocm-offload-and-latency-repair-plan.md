# Prometheus Qwen ROCm Offload and Latency Repair Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Restore real GPU offload for the Prometheus Qwen reasoning lane, verify that the live `llama-server` stops falling back to CPU, re-measure the lane’s latency under the intended hardware path, and only then decide whether any remaining timeout/cancel work is a separate runtime bug.

**Architecture:** Treat this as a runtime-debugging and deployment-repair lane, not a model-selection or Pi-config lane. The first-order problem is that the live reasoning service is configured to request GPU offload but the runtime logs show ROCm initialization failure and CPU-only buffers. The work therefore proceeds in four phases: host/device visibility truth, service/runtime truth, redeploy under corrected GPU conditions, then latency verification. Do not mix this with broader model-fleet work or cancel/spindown implementation until GPU offload is proven working again.

**Tech Stack:** CriomOS/Nix, repo-local `llama-cpp` package override, ROCm/AMD GPU runtime, systemd services on Prometheus, LiteLLM gateway, Pi OpenAI-compatible client path, bounded SSH diagnostics.

---

### Task 1: Freeze the current failure evidence before any mutation

**TDD scenario:** Trivial change — evidence capture before repair.

**Files:**
- Read: `Research/medium/CriomOS/591912250210_report_prometheus-qwen_timeout_investigation_pi-timeout-surface_vs_upstream-runtime-behavior.md`
- Read: `Research/medium/CriomOS/591912251020_report_prometheus-qwen_rocm-offload-failure_runtime-evidence_and_fix-lane.md`
- Update if needed: a bounded follow-up Research note for extra host evidence

**Step 1: Preserve the exact runtime failure signature**

Re-capture the live `prometheus-llama-reasoning.service` log lines showing:
- `failed to initialize ROCm`
- `no usable GPU found`
- `--gpu-layers option will be ignored`
- CPU model/KV buffer lines

**Step 2: Preserve the exact running command line**

Confirm the live process still runs with:
- `--n-gpu-layers 99`
- `--ctx-size 196608`

This proves the problem is runtime/device visibility, not missing intent in service flags.

**Step 3: Preserve one CPU/heat snapshot**

Capture one bounded `ps` snapshot so later comparisons can distinguish CPU-bound fallback from repaired GPU-offloaded behavior.

**Step 4: Finalize via `jj-agent`**

If new evidence is written, commit/push that evidence intent separately before repair work proceeds.

### Task 2: Verify whether Prometheus can currently see any usable ROCm device at all

**TDD scenario:** Debugging task — no fixes before root-cause evidence.

**Files:**
- Read/inspect on node: runtime host state only
- Read: relevant CriomOS node/service docs if needed

**Step 1: Inspect device nodes and permissions**

On Prometheus, capture:
- `/dev/kfd`
- `/dev/dri/render*`
- ownership/group of the GPU-related device nodes

**Step 2: Inspect service-user access path**

Verify whether the service user (`li`) is in the groups needed to access ROCm/render nodes, or whether systemd service settings otherwise grant/deny the required access.

**Step 3: Inspect GPU discovery tools**

Run whichever bounded tools are available on Prometheus:
- `rocm-smi`
- `amd-smi`
- `rocminfo`
- any equivalent bounded hardware probe

The aim is not broad hardware enumeration; it is simply to answer:
- does the host runtime detect a ROCm-capable device right now?

**Step 4: Inspect kernel/module/runtime state**

Capture bounded evidence for ROCm-related modules/drivers and any obvious runtime errors.

**Step 5: Stop and classify the failure**

At the end of this task, classify the issue as one of:
- host cannot see GPU at all
- host sees GPU but service user cannot use it
- runtime tools see GPU but llama.cpp still cannot initialize ROCm

**Step 6: Finalize via `jj-agent`**

If this produces a new Research artifact, finalize/push it separately.

### Task 3: Verify the live `llama.cpp` binary/backend assumptions

**TDD scenario:** Debugging task — build/runtime truth check.

**Files:**
- Read: `Components/CriomOS/nix/llama-cpp-prometheus.nix`
- Read: `Components/CriomOS/nix/mkCriomOS/llm.nix`
- Read: any prior runtime-success research linked from current artifacts

**Step 1: Verify the exact package path and binary**

Confirm which `llama-server` binary path the live service is using.

**Step 2: Confirm intended backend support from packaging**

Verify whether the repo-local package and resulting Prometheus build still intend ROCm support, and whether there has been any drift from the earlier successful Qwen runtime state.

**Step 3: Compare package intent with host runtime reality**

If packaging still targets ROCm but runtime says “no ROCm-capable device is detected,” classify this as a host/runtime environment failure rather than a missing service flag.

**Step 4: Do not guess from `--n-gpu-layers` alone**

Treat runtime logs as authoritative over service flags. The flag is only intent; the startup logs prove whether offload is actually happening.

**Step 5: Finalize via `jj-agent`**

Commit/push only if any documentation/research artifact is produced.

### Task 4: Implement the smallest host/service correction that restores ROCm visibility

**TDD scenario:** Bugfix under systematic debugging — one fix class at a time.

**Files:**
- Modify only the minimal runtime/service/host wiring required by the diagnosis from Tasks 2–3
- Likely touch points may include:
  - `Components/CriomOS/nix/mkCriomOS/llm.nix`
  - host/user/group/device-access wiring under CriomOS modules
  - package/runtime environment only if the diagnosis proves it necessary

**Step 1: Choose exactly one fix class**

Examples of valid single-fix classes:
- device-node access / group-membership repair
- missing ROCm runtime environment wiring
- wrong live package/backend selection
- service environment not inheriting needed GPU runtime paths

Do **not** mix multiple speculative fixes in one step.

**Step 2: Apply only that one correction**

Keep the mutation minimal and tightly scoped to the proven root cause.

**Step 3: Rebuild and redeploy Prometheus only**

Use the exact attr/deploy-manifest flow for Prometheus.

**Step 4: Re-check startup logs immediately**

The success criterion is not “service starts.”
The success criterion is that the logs now show real GPU offload instead of CPU fallback.

**Step 5: Finalize via `jj-agent`**

Commit/push only the single-fix intent once it is verified.

### Task 5: Verify that the repaired lane is genuinely using GPU offload

**TDD scenario:** Verification task.

**Files:**
- Update: Research evidence note if needed

**Step 1: Capture the startup offload lines**

Look for positive runtime indicators such as:
- layers offloaded to GPU
- ROCm model buffer size
- non-zero GPU allocation indicators

**Step 2: Capture one CPU snapshot and one GPU snapshot during inference**

The goal is not “CPU becomes idle.”
It is to prove the runtime is no longer fully CPU fallback.

**Step 3: Confirm CPU-only buffer lines are no longer the dominant story**

If logs still show only CPU model/KV buffers and no GPU offload lines, the repair failed.

**Step 4: Run one tiny direct completion probe**

Use a tiny prompt first to confirm the repaired service returns a response under the intended hardware path.

**Step 5: Finalize via `jj-agent`**

Commit/push only if this verification is being preserved as a research artifact.

### Task 6: Re-measure Qwen latency only after GPU offload is proven

**TDD scenario:** Verification task.

**Files:**
- Update: Research evidence note if needed

**Step 1: Use the runtime-first sanity ladder**

First confirm the Prometheus stack is healthy with the light/sanity lane, then test Qwen.

**Step 2: Measure direct API latency first**

Run:
- direct metadata probe
- direct tiny completion probe
- direct longer completion probe if needed

This separates gateway reachability from inference latency.

**Step 3: Measure Pi path second**

Only after direct API sanity, run the bounded `pi --provider prometheus --model main-reasoning ...` probe.

**Step 4: Compare first-token and total latency**

Classify the repaired lane as one of:
- healthy enough for interactive use
- still too slow but no longer broken
- still pathological even with GPU offload

**Step 5: Finalize via `jj-agent`**

If evidence is saved, commit/push it separately.

### Task 7: Only after GPU repair, revisit the remaining timeout/cancel questions

**TDD scenario:** Architectural debugging follow-up.

**Files:**
- Read: existing cancel/spindown research and timeout research artifacts

**Step 1: Re-test whether the lane still exceeds practical client patience**

If the repaired lane is still slow for normal prompts, then investigate:
- prompt/session size
- cache reuse effectiveness
- compaction behavior

**Step 2: Keep cancellation as a separate bug lane**

Even if latency improves, the earlier research still indicates disconnect/cancel propagation gaps.

**Step 3: Only then decide whether a Pi timeout override is useful**

A timeout tweak is secondary. It should be considered only after the runtime is proven healthy on GPU.

**Step 4: Finalize via `jj-agent`**

Commit/push any follow-up plan or evidence separately.

### Task 8: Context-compaction timing guard

**TDD scenario:** Operational sequencing guard.

**Files:**
- No code changes required unless a separate compaction lane is later chosen

**Step 1: Do not treat compaction as the first fix**

Compaction may help later, but it must not be used to hide the GPU-offload failure.

**Step 2: Compact only after GPU repair is validated**

Otherwise the operator risks masking the true performance problem and losing a clear before/after comparison.

**Step 3: After GPU repair, use compaction as a second-order latency optimization**

Once offload is working, revisit whether the ~61k-token prompt/session state is still too large for comfortable interactive use.

**Step 4: Finalize via `jj-agent`**

If a separate compaction plan is authored later, keep it distinct from the GPU-repair lane.
