# Prometheus Runtime Sanity Ladder and Llama Diagnosis Implementation Plan

> **REQUIRED SUB-SKILL:** Execute this plan using either `/skill:executing-plans` (parallel session) or `/skill:subagent-driven-development` (same-session loop).

**Goal:** Establish a known-good Prometheus LLM stack with a light local model first, then compare the heavy DeepSeek 70B path to isolate whether the runaway/no-spindown behavior is stack-wide or model-specific.

**Architecture:** Keep the current Prometheus shape (local llama.cpp server behind LiteLLM and consumed by Pi), but introduce a temporary light-model sanity lane as the first-class diagnostic surface. Use that lane to prove or falsify stack health before treating cancellation and heavy-model tuning as the main blocker.

**Tech Stack:** CriomOS Nix modules, systemd services, llama.cpp/llama-server, LiteLLM proxy, GGUF models, Pi/OpenAI-compatible provider config, JJ workflow.

---

## Operator constraints captured by this plan
- Treat runtime instability, not only cancel propagation, as the primary blocker.
- Start with the likely sane stack first.
- Install/setup a light local model on Prometheus before re-testing the heavy lane.
- After any Prometheus runtime test, explicitly check whether the node is still hot.
- If llama/LiteLLM remains hot when it should be idle, force-stop the serving stack after evidence capture.
- Preserve shared service continuity as the target design; do not make coarse process-kill the intended solution.
- Exhaust canonical upstream behavior first; if missing, prefer a principled fork/extension.
- Do not reintroduce the old `programming: hh50gb7f` footer requirement.
- Subagents should withdraw rather than over-analyze when the task is unclear.

## Current evidence baseline
- Pi cancel already reaches `AbortController.abort()` and aborts the HTTP request/stream.
- Repo pins LiteLLM `1.82.1`, newer than the documented streaming disconnect cleanup release `1.81.14`.
- LiteLLM still lacks a general guaranteed non-stream cancel-on-disconnect story.
- llama.cpp / llama-server still appears to lack a reliable canonical per-request non-process-level cancel API.
- Prometheus currently hard-wires a single heavy model in:
  - `Components/CriomOS/nix/mkCriomOS/llm.nix`
- Router/catalog exposure currently assumes the current DeepSeek 70B lane as the main local model.

## Recommended light-model target
Use **Llama-3.2-1B-Instruct GGUF** as the first local sanity model.

Reason:
- small and fast enough for safe startup/inference,
- still Llama-family, making comparison with DeepSeek-Llama distill more meaningful,
- good for tiny prompt-following smoke tests like `pong`.

Preferred quant ladder:
1. `Q4_K_M`
2. `Q8_0` fallback if quant/runtime weirdness appears

---

### Task 1: Capture exact runtime baseline before mutation

**TDD scenario:** Modifying tested operational code — capture existing runtime evidence first.

**Files:**
- Modify: none
- Evidence target: new runtime notes / research updates only after capture

**Step 1: Ask a subagent to run a bounded Prometheus runtime evidence packet**

Collect:
- current active services/listeners on `11434` and `11436`
- current model files present under Prometheus model directory
- direct llama `/health` and `/v1/models` when service is up
- LiteLLM `/v1/models` when service is up
- a tiny prompt run against the current heavy lane
- post-prompt CPU/process state 5s, 15s, and 60s later

**Step 2: Confirm the current failure mode is reproduced**

Expected evidence includes one of:
- quick response followed by sustained hot CPU,
- timeout and sustained hot CPU,
- or failure to start the service cleanly.

**Step 3: If the node remains hot after the test, stop it**

Capture evidence first, then use the existing emergency stop sequence.

**Step 4: Persist the evidence in Research**

Write a new research artifact if the runtime packet meaningfully extends current knowledge.

**Step 5: Finalize via `jj-agent`**

Ask the `jj-agent` agent to:
- establish bounded JJ state,
- finalize the current intent into the correct described revision,
- push the runtime target bookmark,
- verify local/remote bookmark alignment.
Use `jj-expert` only if the `jj-agent` lane is unavailable or misbehaving.

---

### Task 2: Add a light local Prometheus sanity model

**TDD scenario:** Modifying tested operational code — build and deploy a minimal new lane, then verify behavior before comparison.

**Files:**
- Modify: `Components/CriomOS/nix/mkCriomOS/llm.nix`
- Modify: `Components/CriomOS/nix/homeModule/min/default.nix`
- Modify: `Components/CriomOS/nix/homeModule/min/litellm-router.yaml`
- Modify: `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`
- Modify: `config/pi/prometheus-agent-settings.json`

**Step 1: Add a second local llama service or temporarily swap the heavy one**

Preferred first approach:
- add a dedicated light-model llama-server service on a separate port,
- keep the heavy service untouched but not the default diagnostic target.

If resource pressure makes dual service risky:
- temporarily swap the heavy model out and use the light model alone for diagnosis.

**Step 2: Add router exposure for the light model**

Update LiteLLM router config so the light model is addressable with a stable model id and alias.

**Step 3: Add Pi-visible metadata**

Update catalog/settings so Pi can explicitly target the light lane.

**Step 4: Build the exact CriomOS attrs before deployment**

Run bounded exact builds only:
- Prometheus OS attr
- Prometheus deploy manifest if needed for deployment

**Step 5: Deploy Prometheus through the manifest-driven CriomOS lane**

Use the existing Ygg-first deploy-manifest path.

**Step 6: Verify the light model starts and answers**

Success criteria:
- direct llama endpoint responds,
- LiteLLM lists the light model,
- a tiny completion returns quickly,
- post-request CPU settles back down.

**Step 7: If the node remains hot after testing, stop it**

Again, evidence first, then emergency stop if needed.

**Step 8: Finalize via `jj-agent`**

Ask the `jj-agent` agent to:
- establish bounded JJ state,
- finalize the current intent into the correct described revision,
- push the runtime target bookmark,
- verify local/remote bookmark alignment.
Use `jj-expert` only if the `jj-agent` lane is unavailable or misbehaving.

---

### Task 3: Compare light-model health against the heavy DeepSeek 70B lane

**TDD scenario:** Modifying tested operational code — use the now-working light lane as the baseline, then re-test heavy lane under the same protocol.

**Files:**
- Modify: `Components/CriomOS/nix/mkCriomOS/llm.nix` if temporary flags/resource controls are needed
- Modify: router/catalog files only if the default test target needs to be swapped

**Step 1: Re-enable or explicitly target the heavy model under the same observability packet**

Use the same prompt/test harness as Task 2.

**Step 2: Compare startup, response, and idle-spindown behavior**

Measure:
- startup time,
- first-token latency,
- time to idle after completion,
- CPU/GPU/memory state 5s, 15s, 60s later.

**Step 3: Decide which bucket the bug belongs to**

Bucket A:
- light model is healthy,
- heavy model runs away.

Bucket B:
- both light and heavy run away.

Bucket C:
- direct llama behaves one way, LiteLLM-routed behavior differs.

**Step 4: If the heavy lane runs away, stop it after evidence capture**

Do not leave Prometheus hot after the experiment.

**Step 5: Persist the comparison report in Research**

This report must clearly classify the bug bucket.

**Step 6: Finalize via `jj-agent`**

Ask the `jj-agent` agent to:
- establish bounded JJ state,
- finalize the current intent into the correct described revision,
- push the runtime target bookmark,
- verify local/remote bookmark alignment.
Use `jj-expert` only if the `jj-agent` lane is unavailable or misbehaving.

---

### Task 4: Harden the likely successful stack shape

**TDD scenario:** Modifying tested operational code — use the classification from Task 3 to apply the smallest correct fix.

**Files:**
- Likely modify: `Components/CriomOS/nix/mkCriomOS/llm.nix`
- Possibly modify: `Components/CriomOS/nix/homeModule/min/default.nix`
- Possibly modify: `Components/CriomOS/nix/homeModule/min/litellm-router.yaml`
- Possibly modify fork/package surfaces if upstream patching is required

**Step 1: If only the heavy lane is pathological**

Apply heavy-lane-specific mitigation first:
- model flags,
- resource limits,
- runtime containment,
- or temporary heavy-lane demotion behind a non-default alias.

**Step 2: If both models are pathological**

Treat llama.cpp runtime itself as the primary target.

Prepare a principled fork/extension plan for llama-server:
- cooperative cancel / broken-socket detection,
- slot cleanup,
- preserving shared process continuity.

**Step 3: If LiteLLM path differs from direct llama path**

Target the gateway/router layer next, not Pi.

**Step 4: Re-run the true Pi criterion**

```bash
timeout 15s pi --provider prometheus --model <target-model> --thinking off --no-session --no-tools -p 'Reply with exactly pong.'
```

**Step 5: Confirm both correctness and calmness**

A phase is not complete unless:
- `pong` is quick,
- and Prometheus settles back to idle afterward.

**Step 6: Finalize via `jj-agent`**

Ask the `jj-agent` agent to:
- establish bounded JJ state,
- finalize the current intent into the correct described revision,
- push the runtime target bookmark,
- verify local/remote bookmark alignment.
Use `jj-expert` only if the `jj-agent` lane is unavailable or misbehaving.

---

### Task 5: Persist manual operator instructions into durable guidance

**TDD scenario:** Trivial-to-configuration guidance update — run relevant checks after edits.

**Files:**
- Modify: heavily used skill/agent docs most relevant to this lane
- Modify: policy/guidance files that still imply the removed `programming:` footer requirement, if present in mutable authority surfaces
- Research note: record the documentation change rationale if needed

**Step 1: Update the most-used skills/agents with the new operator rules**

At minimum encode:
- withdraw instead of over-analyzing when unclear,
- runtime tests on Prometheus must check for post-test hot state,
- force-stop after evidence if the node stays hot,
- runtime-first sanity ladder with a light model before heavy diagnosis.

**Step 2: Remove the stale `programming: hh50gb7f` style requirement from mutable prompt surfaces**

Do not leave contradictory guidance in active repo-local instructions.

**Step 3: Run bounded verification on the touched guidance files**

Confirm no contradictory instruction remains in the touched mutable files.

**Step 4: Finalize via `jj-agent`**

Ask the `jj-agent` agent to:
- establish bounded JJ state,
- finalize the current intent into the correct described revision,
- push the runtime target bookmark,
- verify local/remote bookmark alignment.
Use `jj-expert` only if the `jj-agent` lane is unavailable or misbehaving.

---

## Success criteria by phase

### Phase 1 success
- Light local model starts quickly.
- Direct llama and LiteLLM both answer tiny prompts.
- Prometheus goes idle again after the request.
- If it does not, the node is force-stopped after evidence capture.

### Phase 2 success
- Heavy-model comparison clearly shows whether the bug is:
  - heavy-model-specific,
  - stack-wide,
  - or gateway-specific.

### Final success
- `pi ... 'Reply with exactly pong.'` returns quickly on the intended Prometheus lane.
- Prometheus does not remain hot afterward.
- Shared-process continuity is preserved in the intended design.
- Any emergency force-stop remains only a temporary operator safeguard, not the design solution.

---

## Notes on model exposure during diagnosis
Recommended default diagnostic shape:
- keep the light model explicitly exposed,
- keep the heavy model available but not the first diagnostic target,
- do **not** remove the heavy lane permanently unless the node risk is too high.

If dual exposure causes risk or confusion:
- temporarily replace the heavy lane with the light model during diagnosis,
- then restore the heavy lane for Phase 2 comparison.

---

## Two useful specialized-agent ideas (do not implement all at once)
1. **edit-nix**
   - focused on CriomOS/Nix mutations with strict bounded build verification.
2. **runtime-stack-debugger**
   - focused on live Prometheus service evidence packets, heat detection, and operator-safe stop procedures.

These are worth documenting/adding later if this lane continues to repeat.
