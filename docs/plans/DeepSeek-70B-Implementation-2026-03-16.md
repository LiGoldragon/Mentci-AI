# DeepSeek 70B Implementation Plan

**Date:** 2026-03-16  
**Status:** Ready for Implementation  
**Priority:** High

---

## Problem Statement

The DeepSeek-R1-Distill-Llama-70B Q8_0 model fails to load in your Prometheus setup due to RAM/VRAM contention with the Qwen3.5-35B-A3B model. Both models cannot run simultaneously on your Strix Halo GMKTEC EVO-X2 (124 GB unified memory).

---

## Recommended Solution: Three-Phase Approach

### Phase 1: Immediate Fix - Use DeepSeek-R1-32B (1-2 hours)

**Goal:** Get all three models running simultaneously immediately.

#### Changes Required

1. **Update `prometheus-model-lock.json`**
   - Replace DeepSeek 70B entry with DeepSeek-R1-32B
   - Remove multi-shard configuration (32B is single file)

2. **Download new model**
   ```bash
   distrobox enter llama-rocm-7rc-rocwmma
   huggingface-cli download bartowski/DeepSeek-R1-Distill-Qwen-32B-GGUF \
     DeepSeek-R1-Distill-Qwen-32B-Q8_0.gguf \
     --local-dir ~/models
   ```

3. **Restart services**
   ```bash
   sudo systemctl restart prometheus-deepseek-r1-distill-llama-32b-server
   ```

#### Expected Outcome

- ✅ All three models running simultaneously
- ✅ Total memory: ~61 GB (well under 124 GB limit)
- ✅ No service conflicts

#### Pros & Cons

| Pros | Cons |
|------|------|
| Immediate solution | Slightly less reasoning quality than 70B |
| All models always available | May not match 70B on complex benchmarks |
| No manual intervention needed |  |

---

### Phase 2: Memory Optimization - Reduce Qwen3.5 Context (1 hour)

**Goal:** Free up ~10-15 GB by reducing Qwen3.5's context window.

#### Changes Required

1. **Update `prometheus-model-lock.json`**
   ```json
   {
     "modelId": "qwen3.5-35b-a3b",
     "contextWindow": 65536,
     "maxTokens": 4096,
     "ctxSize": 65536
   }
   ```

2. **Restart Qwen3.5 service**
   ```bash
   sudo systemctl restart prometheus-qwen3.5-35b-a3b-server
   ```

#### Expected Outcome

- ✅ Memory freed: ~10-15 GB
- ✅ Still excellent for most coding tasks
- ✅ More headroom for future additions

#### Pros & Cons

| Pros | Cons |
|------|------|
| Significant memory savings | May limit extremely long context use cases |
| Future-proofing |  |
| Better multi-model stability |  |

---

### Phase 3: On-Demand DeepSeek 70B Q4 (2-3 hours)

**Goal:** Add DeepSeek 70B Q4_K_M as an on-demand option for maximum reasoning.

#### Architecture

```
Always Running (61 GB):
├── Llama 3.2 1B: 2 GB
├── Qwen3.5-35B: 37 GB
└── DeepSeek-R1-32B: 24 GB

On-Demand (42 GB):
└── DeepSeek-R1-70B Q4_K_M: 42 GB
    (Requires stopping Qwen3.5 first)
```

#### Changes Required

1. **Create new model entry in `prometheus-model-lock.json`**
   ```json
   {
     "modelId": "deepseek-r1-distill-llama-70b-q4",
     "canonicalId": "deepseek-r1-distill-llama-70b-q4",
     "alias": "prometheus-deepseek-r1-70b-q4",
     "primaryAlias": "deepseek-r1-70b-q4",
     "serviceSuffix": "deepseek-r1-70b-q4",
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

2. **Create systemd service wrapper**
   - Create `/etc/systemd/system/prometheus-deepseek-r1-70b-q4-server.service`
   - Use `Restart=no` (on-demand only)
   - Include stop logic for Qwen3.5

3. **Create helper scripts**
   - `~/.local/bin/prometheus-start-70b.sh` - Starts 70B, stops Qwen3.5
   - `~/.local/bin/prometheus-stop-70b.sh` - Stops 70B, restarts Qwen3.5

4. **Update nginx/config routing** (if using API gateway)

#### Workflow Script

```bash
#!/bin/bash
# Start DeepSeek 70B for maximum reasoning

echo "🔄 Stopping Qwen3.5-35B to free memory..."
sudo systemctl stop prometheus-qwen3.5-35b-a3b-server

echo "🚀 Starting DeepSeek-R1-70B Q4..."
sudo systemctl start prometheus-deepseek-r1-70b-q4-server

echo "✅ DeepSeek 70B ready on port 11439"
echo "   Use: curl http://localhost:11439/v1/chat/completions ..."

# Optional: Set default model for qwen CLI
export OPENAI_BASE_URL="http://127.0.0.1:11439/v1"
echo "📝 Set OPENAI_BASE_URL for this session"
```

```bash
#!/bin/bash
# Stop DeepSeek 70B and restore Qwen3.5

echo "🔄 Stopping DeepSeek-R1-70B..."
sudo systemctl stop prometheus-deepseek-r1-70b-q4-server

echo "🚀 Restoring Qwen3.5-35B..."
sudo systemctl start prometheus-qwen3.5-35b-a3b-server

echo "✅ Qwen3.5-35B back on port 11437"
echo "   Use: curl http://localhost:11437/v1/chat/completions ..."

unset OPENAI_BASE_URL
echo "📝 Reset OPENAI_BASE_URL"
```

#### Expected Outcome

- ✅ Access to 70B model for complex reasoning
- ✅ Automatic cleanup when done
- ✅ Qwen3.5 restored for daily tasks
- ✅ Memory management handled automatically

#### Pros & Cons

| Pros | Cons |
|------|------|
| Maximum reasoning quality available | Manual start/stop required |
| Minor quality loss with Q4 quantization | Context switching overhead |
| Memory-efficient workflow | Not for simultaneous use |

---

## Implementation Checklist

### Phase 1: DeepSeek 32B

- [ ] Get SHA256 hash of DeepSeek-R1-32B-Q8_0.gguf
- [ ] Update `prometheus-model-lock.json`
- [ ] Download model to `Components/CriomOS/data/models/`
- [ ] Create systemd service file
- [ ] Test model loading
- [ ] Verify all three models running
- [ ] Update documentation

### Phase 2: Context Optimization

- [ ] Update Qwen3.5 context window to 65536
- [ ] Restart Qwen3.5 service
- [ ] Verify memory usage reduced
- [ ] Test long-context tasks still work

### Phase 3: On-Demand 70B

- [ ] Get SHA256 hash of DeepSeek-R1-70B-Q4_K_M.gguf
- [ ] Add new model entry to `prometheus-model-lock.json`
- [ ] Download model (large file, ~42 GB)
- [ ] Create systemd service file
- [ ] Create helper scripts
- [ ] Test start/stop workflow
- [ ] Update documentation

---

## Verification Plan

### Memory Monitoring

```bash
# Before starting any model
free -h
rocm-smi

# After starting each model
sudo systemctl status prometheus-*-server
sudo journalctl -u prometheus-*-server -n 20

# Check actual memory usage
ps aux | grep llama-server
```

### Functional Testing

```bash
# Test Llama 3.2 1B
curl http://localhost:11436/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "llama-3.2-1b-instruct",
    "messages": [{"role": "user", "content": "Hello"}],
    "max_tokens": 50
  }'

# Test Qwen3.5-35B
curl http://localhost:11437/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "qwen3.5-35b-a3b",
    "messages": [{"role": "user", "content": "Write a Python function"}],
    "max_tokens": 200
  }'

# Test DeepSeek 32B
curl http://localhost:11438/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "deepseek-r1-distill-qwen-32b",
    "messages": [{"role": "user", "content": "Solve this logic puzzle"}],
    "max_tokens": 500
  }'

# Test DeepSeek 70B (when on-demand service active)
curl http://localhost:11439/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "deepseek-r1-distill-llama-70b-q4",
    "messages": [{"role": "user", "content": "Complex reasoning task"}],
    "max_tokens": 1000
  }'
```

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| DeepSeek 32B not sufficient quality | Low | Medium | Can always add 70B on-demand later |
| Context reduction breaks workflows | Low | Medium | Can increase back to 131K if needed |
| On-demand workflow fails | Medium | Low | Scripts include error handling |
| Model download fails | Low | Low | Resume-capable tools available |
| Service startup issues | Medium | Medium | Journal logs for debugging |

---

## Rollback Plan

If any phase causes issues:

```bash
# Rollback Phase 3
sudo systemctl stop prometheus-deepseek-r1-70b-q4-server
sudo systemctl start prometheus-qwen3.5-35b-a3b-server

# Rollback Phase 2
git checkout Components/CriomOS/data/config/pi/prometheus-model-lock.json

# Rollback Phase 1
# Revert to original 70B configuration (requires fixing memory issue first)
```

---

## Timeline

| Phase | Estimated Time | Dependencies |
|-------|----------------|--------------|
| Phase 1 | 1-2 hours | None |
| Phase 2 | 1 hour | Phase 1 complete |
| Phase 3 | 2-3 hours | Phase 1 & 2 complete |
| **Total** | **4-6 hours** | Sequential |

---

## Next Steps

1. **Review this plan** and confirm which phases to implement
2. **Answer the questions** in the Research report about your usage patterns
3. **Choose implementation strategy:**
   - Option A: Phase 1 only (quick fix, 32B model)
   - Option B: Phase 1 + 2 (optimized 32B setup)
   - Option C: Full three-phase approach (maximum flexibility)
4. **I will then:**
   - Create the actual file edits
   - Generate the helper scripts
   - Provide exact commands to execute

---

## Questions for You

1. **Which implementation option do you prefer?** (A, B, or C)
2. **Do you have enough disk space** for all models (~110 GB total)?
3. **Are you comfortable with manual start/stop** for the 70B model, or do you need all models always available?
4. **How critical is maximum reasoning quality** vs convenience of always-available models?
5. **Should I proceed with creating the actual implementation files** now, or do you need more discussion first?
