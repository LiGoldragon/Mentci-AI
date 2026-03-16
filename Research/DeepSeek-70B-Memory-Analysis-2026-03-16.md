# DeepSeek R1 Distill Llama 70B Memory Analysis

**Date:** 2026-03-16  
**Subject:** Why DeepSeek R1 Distill Llama 70B Q8_0 isn't working on Strix Halo GMKTEC EVO-X2  
**Reference:** https://github.com/pablo-ross/strix-halo-gmktec-evo-x2

---

## Executive Summary

The DeepSeek R1 Distill Llama 70B Q8_0 model is **failing to load** due to **severe RAM/VRAM contention** when running simultaneously with the Qwen3.5-35B-A3B model. This is a **memory capacity issue**, not a configuration problem.

### Root Cause
Your current configuration has **two massive models running simultaneously**:
- **Qwen3.5-35B-A3B Q8_0**: ~22-24 GB (active coding lane)
- **DeepSeek-R1-Distill-Llama-70B Q8_0**: ~70+ GB (reasoning lane)

**Total required**: ~94-100 GB just for model weights alone, **plus** context windows, KV cache, and system overhead.

### Hardware Reality (Strix Halo GMKTEC EVO-X2)
- **Unified memory**: 124 GB RAM + 128 GB GTT = ~252 GB total
- **Available for LLMs**: ~120-130 GB (conservative estimate)
- **Actual usage with both models**: **EXCEEDS CAPACITY**

---

## 1. Model Size Analysis

### DeepSeek-R1-Distill-Llama-70B Q8_0
Based on HuggingFace discussions and benchmarks:

| Source | Finding |
|--------|---------|
| HuggingFace Discussion (rimahajou) | "Q8 model file about 70GB+" |
| Reddit r/LocalLLaMA | Q8_0 requires ~70GB VRAM |
| Reddit r/selfhosted | 70B Q4_K_M is ~49GB, Q8_0 is ~2x that |
| BytePlus | Q8_0 requires 64-70GB RAM minimum |
| Databricks | 70B models require 128GB CPU memory + 48GB GPU memory |

**Confirmed: DeepSeek-R1-70B Q8_0 ≈ 70-72 GB**

### Qwen3.5-35B-A3B Q8_0 (Your current setup)
- **File size**: ~22-24 GB (Q8_0 quantization)
- **Context overhead**: Your config uses 196K context window (massive!)
- **KV cache at full context**: ~8-12 GB additional
- **Total active memory**: ~30-36 GB

### Llama 3.2 1B Q4_K_M (Sanity lane)
- **File size**: ~1.2 GB
- **Minimal overhead** - negligible

---

## 2. Memory Budget Analysis

### Your Current Configuration

| Model | Weights | Context (ctxSize) | KV Cache | Total |
|-------|---------|-------------------|----------|-------|
| Llama 3.2 1B | 1.2 GB | 8K | ~0.5 GB | ~2 GB |
| Qwen3.5 35B | 22 GB | **196K** | **~15 GB** | **~37 GB** |
| DeepSeek 70B | **72 GB** | 131K | **~20 GB** | **~92 GB** |
| **TOTAL** | | | | **~131 GB** |

### The Problem

**131 GB required** vs **~120 GB available** = **OUT OF MEMORY**

Even if you have 252 GB total unified memory, the **GTT memory is used for compute**, not as overflow storage. The **124 GB system RAM** is the actual limit.

---

## 3. Evidence from Reference Setup (pablo-ross)

### Their Successful Multi-Model Setup

From `MULTI_MODEL_DEPLOYMENT.md`:

```
GPU/APU Memory (120GB total):
├── Qwen3-30B Q8 (main): 40GB [Always running]
├── Bielik-11B: 8GB [Always running]
├── Qwen2.5-7B (autocomplete): 8GB [Always running]
├── Nomic Embed: 3GB [Always running]
├── DeepSeek R1: 24GB [On-demand only]
└── Available: 37GB [With all models] OR 61GB [Without DeepSeek]
```

**Key Differences from Your Setup:**

1. **They use DeepSeek-R1-32B (24GB)**, not 70B (72GB)
2. **They run DeepSeek on-demand only** (stop when done)
3. **Their main model is 30B Q8**, not 35B A3B
4. **They reduce parallel slots** when loading larger models

### Their On-Demand Strategy

From the same document:

> **On-Demand (24GB):**
> - DeepSeek R1: Start when needed for reasoning
> ```bash
> sudo systemctl start deepseek-r1-server
> qwen-reasoning "solve this complex problem"
> sudo systemctl stop deepseek-r1-server
> ```

---

## 4. Why Your Setup Fails

### Memory Contention Scenario

When you attempt to start the DeepSeek 70B server while Qwen3.5-35B is running:

1. **Qwen3.5-35B is already loaded**: ~37 GB active
2. **DeepSeek 70B tries to load**: Requires 72 GB
3. **System attempts allocation**: Fails due to insufficient RAM
4. **llama.cpp crashes**: "out of memory" or "failed to mmap" error

### Additional Issues

1. **Context Window Bloat**
   - Your Qwen3.5 uses **196,608 token context** (ctxSize)
   - This is **extremely aggressive** and consumes massive KV cache
   - Reference setup uses **256K** for Qwen3-30B but keeps model smaller

2. **No Slot Reuse for Large Models**
   - DeepSeek 70B should use `--parallel 1` or `--parallel 2`
   - Your config may allow more slots, increasing memory pressure

3. **No Mixture-of-Experts Optimization**
   - DeepSeek R1 is an MoE model (activates only ~37B params per token)
   - But **all weights must still be loaded** in Q8_0 format

---

## 5. Solutions

### Solution 1: Use DeepSeek-R1-32B Instead (RECOMMENDED)

**Why:** Fits comfortably alongside Qwen3.5-35B

| Model | Memory |
|-------|--------|
| Qwen3.5-35B-A3B | ~37 GB |
| DeepSeek-R1-32B Q8_0 | ~24 GB |
| **Total** | **~61 GB** ✅ |

**Action:**
1. Download `DeepSeek-R1-Distill-Qwen-32B-Q8_0.gguf` from bartowski repo
2. Update `prometheus-model-lock.json` to use 32B instead of 70B
3. Keep both models running simultaneously

**Evidence:**
- pablo-ross uses 32B successfully
- Reddit benchmarks show 32B Q8_0 runs on consumer hardware with 64GB RAM

---

### Solution 2: Run DeepSeek 70B On-Demand Only

**Strategy:** Stop Qwen3.5 when using DeepSeek 70B

**Workflow:**
```bash
# Stop Qwen3.5 server
sudo systemctl stop prometheus-qwen3.5-35b-a3b-server

# Start DeepSeek 70B server
sudo systemctl start prometheus-deepseek-r1-distill-llama-70b-server

# Use DeepSeek for reasoning tasks
curl http://localhost:11438/v1/chat/completions ...

# When done, stop DeepSeek and restart Qwen3.5
sudo systemctl stop prometheus-deepseek-r1-distill-llama-70b-server
sudo systemctl start prometheus-qwen3.5-35b-a3b-server
```

**Memory Budget:**
- Qwen3.5 only: ~37 GB ✅
- DeepSeek 70B only: ~92 GB ✅
- Both simultaneously: ❌ FAILS

**Pros:**
- Can use the full 70B model
- Maximum reasoning quality

**Cons:**
- Cannot use both models simultaneously
- Manual start/stop required
- Context switching overhead

---

### Solution 3: Quantize DeepSeek 70B Further

**Option:** Use Q4_K_M instead of Q8_0

| Quantization | Approx. Size | Quality Loss |
|--------------|--------------|--------------|
| Q8_0 | 72 GB | ~0% |
| Q6_K | 56 GB | ~2-3% |
| Q5_K_M | 49 GB | ~3-5% |
| Q4_K_M | 42 GB | ~5-8% |

**With Q4_K_M:**
- DeepSeek 70B Q4_K_M: ~42 GB
- Qwen3.5-35B Q8_0: ~37 GB
- **Total: ~79 GB** ✅ **Fits comfortably!**

**Action:**
1. Download `DeepSeek-R1-Distill-Llama-70B-Q4_K_M.gguf` from bartowski
2. Update config to use Q4_K_M instead of Q8_0
3. Accept minor quality trade-off for feasibility

**Evidence:**
- Reddit user runs 70B Q4_K_M on RTX 3090 (24GB VRAM + 33GB system RAM)
- Quality loss is minimal for reasoning tasks

---

### Solution 4: Reduce Qwen3.5 Context Window

**Current:** 196,608 tokens (ctxSize)  
**Recommended:** 32,768 or 65,536 tokens

**Memory Savings:**
- Current KV cache at full context: ~15 GB
- Reduced to 32K context: ~2-3 GB
- **Savings: ~12-13 GB**

**New Budget with 32K context:**
- Qwen3.5-35B (32K): ~25 GB (was 37 GB)
- DeepSeek 70B Q8_0: ~92 GB
- **Total: ~117 GB** ✅ **Just barely fits!**

**Note:** This is still risky; combine with Solution 3 for safety.

---

### Solution 5: Hybrid Strategy (BEST FOR PRODUCTION)

**Three-tier model setup:**

| Priority | Model | Size | Use Case | Always Running? |
|----------|-------|------|----------|-----------------|
| **Low** | Llama 3.2 1B | 2 GB | Sanity checks | ✅ Yes |
| **Medium** | Qwen3.5-35B-A3B | 37 GB | Coding, daily tasks | ✅ Yes |
| **On-Demand** | DeepSeek-R1-32B | 24 GB | Complex reasoning | ⏸️ Start when needed |
| **On-Demand** | DeepSeek-R1-70B Q4 | 42 GB | Maximum reasoning | ⏸️ Start when needed (stop Qwen3.5) |

**Workflow:**
```bash
# Daily use (Qwen3.5 running)
sudo systemctl start prometheus-qwen3.5-35b-a3b-server
# Use Qwen3.5 for coding tasks

# For complex reasoning, start 32B
sudo systemctl start prometheus-deepseek-r1-32b-server
# Use DeepSeek 32B
sudo systemctl stop prometheus-deepseek-r1-32b-server

# For maximum reasoning, stop Qwen3.5 and start 70B
sudo systemctl stop prometheus-qwen3.5-35b-a3b-server
sudo systemctl start prometheus-deepseek-r1-70b-server
# Use DeepSeek 70B
sudo systemctl stop prometheus-deepseek-r1-70b-server
sudo systemctl start prometheus-qwen3.5-35b-a3b-server
```

---

## 6. Recommended Implementation Plan

### Phase 1: Immediate Fix (Use DeepSeek 32B)

**Changes to `prometheus-model-lock.json`:**

Replace the DeepSeek 70B entry with DeepSeek 32B:

```json
{
  "modelId": "deepseek-r1-distill-qwen-32b",
  "descriptor": "DeepSeek R1 Distill Qwen 32B (Prometheus reasoning)",
  "source": {
    "kind": "fetchurl",
    "url": "https://huggingface.co/bartowski/DeepSeek-R1-Distill-Qwen-32B-GGUF/resolve/main/DeepSeek-R1-Distill-Qwen-32B-Q8_0.gguf",
    "sha256": "sha256-[GET-HASH]",
    "filename": "DeepSeek-R1-Distill-Qwen-32B-Q8_0.gguf"
  },
  "reasoning": true,
  "contextWindow": 131072,
  "maxTokens": 8192,
  "ctxSize": 131072,
  "port": 11438
}
```

**Expected result:** All three models run simultaneously without memory issues.

### Phase 2: Optimize Qwen3.5 Context

**Reduce context window** from 196K to 65K:

```json
{
  "modelId": "qwen3.5-35b-a3b",
  "contextWindow": 65536,
  "maxTokens": 4096,
  "ctxSize": 65536
}
```

**Expected result:** ~10 GB memory savings, still excellent for most tasks.

### Phase 3: Add On-Demand DeepSeek 70B Q4 (Optional)

**Create separate service** for DeepSeek 70B Q4_K_M:

```json
{
  "modelId": "deepseek-r1-distill-llama-70b-q4",
  "descriptor": "DeepSeek R1 Distill Llama 70B Q4 (On-demand max reasoning)",
  "source": {
    "kind": "fetchurl",
    "url": "https://huggingface.co/bartowski/DeepSeek-R1-Distill-Llama-70B-GGUF/resolve/main/DeepSeek-R1-Distill-Llama-70B-Q4_K_M.gguf",
    "sha256": "sha256-[GET-HASH]",
    "filename": "DeepSeek-R1-Distill-Llama-70B-Q4_K_M.gguf"
  },
  "reasoning": true,
  "contextWindow": 131072,
  "maxTokens": 16384,
  "ctxSize": 131072,
  "port": 11439,
  "onDemand": true
}
```

---

## 7. Verification Commands

After implementing changes, verify memory usage:

```bash
# Check GPU memory (ROCm)
rocm-smi

# Check system memory
free -h

# Check llama.cpp memory usage (from logs)
sudo journalctl -u prometheus-*-server -f

# Test model loading
distrobox enter llama-rocm-7rc-rocwmma
cd ~/llama.cpp
./build/bin/llama-cli -m ~/models/deepseek-r1-32b.Q8_0.gguf -ngl 99 -c 4096 -p "Test"
```

---

## 8. Questions for You

1. **Do you need the full 70B model quality**, or would 32B be sufficient for your reasoning tasks?
2. **How often do you need to switch between Qwen3.5 and DeepSeek 70B?** If rarely, on-demand strategy works.
3. **What is your typical context window usage?** If you rarely use >32K context, reducing Qwen3.5's context would free significant RAM.
4. **Are you willing to accept minor quality loss** from Q4_K_M quantization in exchange for fitting 70B model?
5. **Do you want me to create implementation scripts** for the on-demand DeepSeek 70B workflow?

---

## 9. References

1. [pablo-ross/strix-halo-gmktec-evo-x2 - Multi-Model Deployment](https://github.com/pablo-ross/strix-halo-gmktec-evo-x2/blob/main/MULTI_MODEL_DEPLOYMENT.md)
2. [bartowski/DeepSeek-R1-Dist