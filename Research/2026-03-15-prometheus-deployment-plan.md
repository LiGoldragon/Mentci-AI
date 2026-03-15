# Prometheus LLM Model Deployment Plan

## CRITICAL: Pi Coding Agent IS RUNNING

**The Pi coding agent is currently active on Prometheus** and connected to:
- `prometheus-main-sanity` (port 11436) - Llama-3.2-1B
- `prometheus-main-reasoning` (port 11437) - Qwen3.5-35B

**When llama-server restarts:**
1. **Pi coding agent WILL crash** or lose connection
2. **In-progress work WILL be lost**
3. **You WILL be disconnected** from the active session

## Phased Deployment Strategy

### Phase 1: Non-Disruptive Changes (DEPLOY NOW) ✅

These changes can be deployed **without restarting llama-server**:

1. ✅ **DeepSeek shards added to Nix store** (already done)
2. ✅ **Hashes in `prometheus-model-lock.json` fixed** (already done)
3. ✅ **Nix build succeeds** (local files will be found)

**Status:** These are already completed and ready to use!

### Phase 2: Model Renaming (DEPLOY WITH CAUTION)

These changes require llama-server restart:

1. Update `prometheus-model-catalog.json` with new model names
2. Update `prometheus-agent-settings.json` 
3. Update `homeModule/min/default.nix`

**Impact:** llama-server will restart, Pi agent will crash

**Recommended timing:**
- Schedule during low-usage period
- Notify Pi coding agent to pause/complete work
- Have rollback ready

### Phase 3: Full Deployment (LAST STEP)

Deploy the complete NixOS configuration with all changes.

**Impact:** Full service disruption, 5-10 minutes downtime

---

## Deployment Mechanism

### How Prometheus is Deployed

From `Components/CriomOS/readme.md`:

```bash
# Build the deploy manifest
nix build .#crioZones.maisiliym.prometheus.deployManifest --no-link --print-out-paths --refresh

# Deploy via manifest-driven mechanism
execute deploy-manifest --manifest <path-to-manifest> --node prometheus
```

### What Happens During Deployment

1. **Builds** the `crioZones.maisiliym.prometheus.os` NixOS configuration
2. **Generates** systemd units including `prometheus-llama-*` services
3. **Deploys** via the Maisiliym deploy manifest mechanism
4. **Activates** the new configuration (replaces `/run/current-system`)
5. **Restarts** all services that changed (including llama-server)

## Safe Deployment Strategies

### Strategy A: Staged Rollout (Recommended)

Deploy models one at a time to minimize disruption:

1. **Deploy DeepSeek first** (new model, can be tested independently)
2. **Verify DeepSeek works** before deploying other models
3. **Deploy Qwen3.5 and Llama-3.2** together (existing models, less risk)

**Steps:**
```bash
# Step 1: Comment out non-DeepSeek models in prometheus-model-lock.json
# Step 2: Deploy only DeepSeek
# Step 3: Verify DeepSeek works
# Step 4: Re-enable all models
# Step 5: Deploy all models together
```

### Strategy B: Temporary Backup Runtime

Deploy with a temporary service running alongside:

1. **Start a temporary llama-server** on a different port
2. **Update Pi agent** to use the temporary runtime
3. **Deploy new configuration** (old runtime continues)
4. **Verify new runtime works**
5. **Switch Pi agent** to new runtime
6. **Stop temporary runtime**

**This requires:**
- Additional GPU memory (~70GB for DeepSeek + existing models)
- Promethus has ~128GB VRAM (70B Q8_0 + 35B Q8_0 + 1B Q4) = ~106GB total
- This is **borderline feasible** but risky

### Strategy C: Maintenance Window

Schedule a deployment during low-usage period:

1. **Notify all users** of maintenance window
2. **Deploy configuration** (expect 5-10 minute downtime)
3. **Verify all services** are running
4. **Resume normal operation**

### Strategy D: Hot-Reload Models (NOT SUPPORTED)

⚠️ **Warning:** llama-server does NOT support hot-reloading models. The `--model` parameter is read at startup and cannot be changed without restarting the process.

## Recommended Deployment Procedure

### Pre-Deployment Checklist

- [ ] All model files are in Nix store (`nix-store --query /nix/store/*DeepSeek*`)
- [ ] Hashes in `prometheus-model-lock.json` are correct (base32 format)
- [ ] Test derivation builds successfully locally
- [ ] GPU memory is sufficient (~106GB for all models)
- [ ] Maintenance window is scheduled (if possible)
- [ ] Backup plan is ready (temporary runtime or rollback procedure)

### Deployment Steps

#### Step 1: Build and Test Locally

```bash
cd /home/li/git/Mentci-AI--dev/Components/CriomOS

# Build the OS configuration
nix build .#crioZones.maisiliym.prometheus.os --no-link --print-out-paths

# Inspect the generated systemd units
nix-store --query /nix/store/*prometheus-llama* 2>/dev/null || echo "Not built yet"
```

#### Step 2: Test Model Loading (Local)

```bash
# Test that the model can be loaded (this will download from Nix store)
nix-build -E '
  let
    pkgs = import <nixpkgs> {};
    model = pkgs.fetchurl {
      url = "https://huggingface.co/unsloth/DeepSeek-R1-Distill-Llama-70B-GGUF/resolve/main/DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf";
      sha256 = "sha256-q2mbEa0CzwJznepf5fZrZOtRQMvfwRBS68In/bKlmsk=";
    };
  in model
'
```

#### Step 3: Deploy to Prometheus

**Option A: Using deploy manifest (preferred)**
```bash
# On your local machine
cd /home/li/git/Mentci-AI--dev
nix build .#crioZones.maisiliym.prometheus.deployManifest --no-link --print-out-paths > /tmp/deploy-manifest.json

# Deploy via Maisiliym (requires execute tool)
execute deploy-manifest --manifest /tmp/deploy-manifest.json --node prometheus
```

**Option B: Direct NixOS rebuild (simpler for testing)**
```bash
# On Prometheus
ssh li@202:68bc:1221:1b13:5397:2a56:4aea:d4a9 "
cd /home/li/git/Mentci-AI--dev
nixos-rebuild switch --flake .#crioZones.maisiliym.prometheus.os
"
```

**⚠️ WARNING:** This will restart ALL services including llama-server!

#### Step 4: Verify Services

```bash
# On Prometheus
ssh li@202:68bc:1221:1b13:5397:2a56:4aea:d4a9 "
# Check service status
systemctl status prometheus-llama-deepseek-r1-distill-llama-70b
systemctl status prometheus-llama-qwen3.5-35b-a3b
systemctl status prometheus-llama-llama-3.2-1b-instruct
systemctl status prometheus-litellm

# Check logs
journalctl -u prometheus-llama-deepseek-r1-distill-llama-70b -n 50 --no-hostname

# Test endpoints
curl http://localhost:11436/v1/models
curl http://localhost:11437/v1/models
curl http://localhost:11438/v1/models
curl http://localhost:11434/v1/models
"
```

#### Step 5: Verify Model Loading

Check that the model files are being served from Nix store:
```bash
# Check the actual model path being used
ssh li@202:68bc:1221:1b13:5397:2a56:4aea:d4a9 "
systemctl cat prometheus-llama-deepseek-r1-distill-llama-70b | grep ExecStart
"
```

Expected output should show:
```
ExecStart=/nix/store/...-llama-server --model /nix/store/...-DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf
```

#### Step 6: Test Inference

```bash
# Test DeepSeek
curl -X POST http://localhost:11438/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "deepseek-r1-distill-llama-70b",
    "messages": [{"role": "user", "content": "Hello, can you say hello back?"}]
  }'

# Test LiteLLM gateway
curl -X POST http://localhost:11434/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "deepseek-r1-distill-llama-70b",
    "messages": [{"role": "user", "content": "Hello from LiteLLM"}]
  }'
```

## Rollback Procedure

If something goes wrong:

### Immediate Rollback

```bash
# On Prometheus
ssh li@202:68bc:1221:1b13:5397:2a56:4aea:d4a9 "
# Rollback to previous configuration
nixos-rebuild switch --rollback
"
```

### Manual Rollback (if rollback unavailable)

```bash
# Revert git changes
cd /home/li/git/Mentci-AI--dev
jj undo

# Redeploy previous configuration
nixos-rebuild switch --flake .#crioZones.maisiliym.prometheus.os
```

## Post-Deployment Verification

### Service Health

```bash
# Check all services are running
systemctl list-units | grep prometheus-llama
systemctl list-units | grep prometheus-litellm

# Check for errors in logs
journalctl -u prometheus-llama-* -u prometheus-litellm --since "10 minutes ago" | grep -i error
```

### Model Loading

```bash
# Check model files are in Nix store
nix-store --query /nix/store/*DeepSeek*
nix-store --query /nix/store/*Qwen3.5*
nix-store --query /nix/store/*llama-3.2*

# Verify they're being used (check systemd units)
systemctl cat prometheus-llama-deepseek-r1-distill-llama-70b
```

### Inference Tests

```bash
# Test all models
curl -X POST http://localhost:11436/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{"model": "llama-3.2-1b-instruct", "messages": [{"role": "user", "content": "test"}]}'

curl -X POST http://localhost:11437/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{"model": "qwen3.5-35b-a3b", "messages": [{"role": "user", "content": "test"}]}'

curl -X POST http://localhost:11438/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{"model": "deepseek-r1-distill-llama-70b", "messages": [{"role": "user", "content": "test"}]}'

# Test LiteLLM gateway
curl -X POST http://localhost:11434/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{"model": "deepseek-r1-distill-llama-70b", "messages": [{"role": "user", "content": "test"}]}'
```

### Pi Agent Verification

```bash
# Test Pi agent with new model names
pi --provider prometheus --model deepseek-r1-distill-llama-70b -p "Hello"
pi --provider prometheus --model qwen3.5-35b-a3b -p "Hello"
pi --provider prometheus --model llama-3.2-1b-instruct -p "Hello"
```

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| llama-server fails to start | Low | High | Verify model files in Nix store first |
| GPU OOM crash | Medium | High | Verify VRAM requirements (~106GB for all models) |
| Service disruption | Certain | Medium | Schedule maintenance window, notify users |
| Model not loading from Nix store | Low | Medium | Verify hashes match before deployment |
| Rollback needed | Low | Medium | Test locally first, have rollback ready |

## Conclusion

**The deployment WILL restart llama-server and cause service disruption.**

**Recommended approach:**
1. **Schedule a maintenance window** (low-usage period)
2. **Deploy all models together** (single restart)
3. **Have rollback ready** (nixos-rebuild --rollback)
4. **Notify users** of the downtime

**Alternative (if GPU memory allows):**
1. **Start temporary runtime** alongside existing services
2. **Deploy new configuration** (old runtime continues)
3. **Test new runtime**
4. **Switch Pi agent** to new runtime
5. **Stop temporary runtime**

**Not recommended:**
- Hot-reloading models (not supported by llama-server)
- Deploying during peak usage (causes unnecessary disruption)
- Deploying without testing (high risk of failure)

---

**Last Updated:** 2026-03-15  
**Status:** Ready for deployment (pending maintenance window)
