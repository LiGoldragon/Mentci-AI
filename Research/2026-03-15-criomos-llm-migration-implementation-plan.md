# CriomOS LLM Migration Implementation Plan

**Date:** 2026-03-15  
**Status:** Ready for Implementation  
**Constraint:** WE ONLY MUTATE SYSTEMS BY PUSHING CHANGES ON CRIOMOS - PURE NIX CALLS ONLY

---

## Executive Summary

This plan migrates all LLM models on Prometheus to Nix store fixed-output derivations and renames all model aliases from generic names to actual model names throughout the codebase.

### Goals

1. ✅ Make all models (DeepSeek, Qwen3.5, Llama-3.2) pure Nix derivations
2. ✅ Add all models to `prometheus-model-lock.json` with proper FODs
3. ✅ Rename aliases: `main-reasoning` → `deepseek-r1-distill-llama-70b`, etc.
4. ✅ Update Pi agent configs with new model names
5. ✅ Ensure CriomOS serves all models from Nix store

---

## Current State Analysis

### Files to Modify

| File | Purpose | Current State |
|------|---------|---------------|
| `prometheus-model-lock.json` | Model lock file with fetchurl + sha256 | Has Qwen3.5, missing DeepSeek |
| `prometheus-model-catalog.json` | Runtime model catalog | Has Qwen3.5 as main-reasoning |
| `Components/CriomOS/nix/mkCriomOS/llm.nix` | Model serving Nix module | References local paths for legacy |
| `config/pi/prometheus-agent-settings.json` | Pi agent configuration | Uses generic aliases |
| `Components/CriomOS/nix/homeModule/min/default.nix` | HomeManager config | Has LiteLLM router config |

### Current Model Configuration

**prometheus-model-lock.json:**
```json
{
  "servedModels": [
    {
      "modelId": "llama-3.2-1b-instruct",
      "primaryAlias": "main-sanity",
      "port": 11436
    },
    {
      "modelId": "qwen3.5-35b-a3b",
      "primaryAlias": "main-reasoning",
      "port": 11437
    }
  ]
}
```

**DeepSeek Status:**
- ✅ Files exist locally on Prometheus
- ❌ NOT in prometheus-model-lock.json
- ❌ NOT in prometheus-model-catalog.json as active model
- ❌ NOT served by llama-server

---

## Phase 1: Compute SHA256 Hashes

**Goal:** Get integrity hashes for all model files.

### Steps

1. **SSH to Prometheus and compute hashes:**
   ```bash
   ssh li@202:68bc:1221:1b13:5397:2a56:4aea:d4a9 "
   echo 'DeepSeek Shard 1:'
   nix-hash --type sha256 --flat /home/li/.local/share/prometheus-llama/models/DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf
   
   echo 'DeepSeek Shard 2:'
   nix-hash --type sha256 --flat /home/li/.local/share/prometheus-llama/models/DeepSeek-R1-Distill-Llama-70B-Q8_0-00002-of-00002.gguf
   
   echo 'Qwen3.5:'
   nix-hash --type sha256 --flat ~/.local/share/prometheus-llama/models/Qwen3.5-35B-A3B-Q8_0.gguf
   
   echo 'Llama-3.2-1B:'
   nix-hash --type sha256 --flat ~/.local/share/prometheus-llama/models/llama-3.2-1b-instruct-q4_k_m.gguf
   "
   ```

2. **Alternative: Use HuggingFace URLs** (if files are available online):
   - DeepSeek: `https://huggingface.co/unsloth/DeepSeek-R1-Distill-Llama-70B-GGUF`
   - Qwen3.5: `https://huggingface.co/unsloth/Qwen3.5-35B-A3B-GGUF`
   - Llama-3.2: `https://huggingface.co/hugging-quants/Llama-3.2-1B-Instruct-Q4_K_M-GGUF`

### Verification

- All hashes must be valid base64-encoded SHA256 (nix-hash format)
- For multi-shard models, compute hash for each shard separately

---

## Phase 2: Update prometheus-model-lock.json

**Goal:** Add all models with proper FOD configuration.

### New Configuration

```json
{
  "servedModels": [
    {
      "modelId": "llama-3.2-1b-instruct",
      "canonicalId": "llama-3.2-1b-instruct",
      "alias": "prometheus-llama-3.2-1b-instruct",
      "primaryAlias": "llama-3.2-1b-instruct",
      "serviceSuffix": "llama-3.2-1b-instruct",
      "descriptor": "Llama 3.2 1B Instruct (Prometheus sanity lane)",
      "source": {
        "kind": "fetchurl",
        "url": "https://huggingface.co/hugging-quants/Llama-3.2-1B-Instruct-Q4_K_M-GGUF/resolve/main/llama-3.2-1b-instruct-q4_k_m.gguf",
        "sha256": "<COMPUTE_HASH>"
      },
      "reasoning": false,
      "contextWindow": 8192,
      "maxTokens": 2048,
      "ctxSize": 8192,
      "port": 11436
    },
    {
      "modelId": "qwen3.5-35b-a3b",
      "canonicalId": "qwen3.5-35b-a3b",
      "alias": "prometheus-qwen3.5-35b-a3b",
      "primaryAlias": "qwen3.5-35b-a3b",
      "serviceSuffix": "qwen3.5-35b-a3b",
      "descriptor": "Qwen 3.5 35B A3B Q8_0 (Prometheus coding lane)",
      "source": {
        "kind": "fetchurl",
        "url": "https://huggingface.co/unsloth/Qwen3.5-35B-A3B-GGUF/resolve/main/Qwen3.5-35B-A3B-Q8_0.gguf",
        "sha256": "3808866c016ab02b4adb26b873f7008a2cdd2c0704a39704050119ab0631db46"
      },
      "reasoning": true,
      "contextWindow": 196608,
      "maxTokens": 4096,
      "ctxSize": 196608,
      "port": 11437
    },
    {
      "modelId": "deepseek-r1-distill-llama-70b",
      "canonicalId": "deepseek-r1-distill-llama-70b",
      "alias": "prometheus-deepseek-r1-distill-llama-70b",
      "primaryAlias": "deepseek-r1-distill-llama-70b",
      "serviceSuffix": "deepseek-r1-distill-llama-70b",
      "descriptor": "DeepSeek R1 Distill Llama 70B (Prometheus main reasoning)",
      "source": {
        "kind": "fetchurl",
        "url": "https://huggingface.co/unsloth/DeepSeek-R1-Distill-Llama-70B-GGUF/resolve/main/DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf",
        "sha256": "<COMPUTE_HASH>"
      },
      "reasoning": true,
      "contextWindow": 131072,
      "maxTokens": 16384,
      "ctxSize": 131072,
      "port": 11438
    }
  ]
}
```

### Notes

- DeepSeek has 2 shards - you'll need to handle multi-file models in `llm.nix`
- Ports: 11436 (Llama), 11437 (Qwen), 11438 (DeepSeek)
- Aliases changed from generic (`main-reasoning`) to actual model names

---

## Phase 3: Update CriomOS Nix Modules

### 3a: Update llm.nix for Multi-File Models

**File:** `Components/CriomOS/nix/mkCriomOS/llm.nix`

**Changes needed:**

1. **Add multi-file support:**
   ```nix
   mkRuntimeModel = index: spec:
     let
       source = if hasAttr "source" spec then spec.source else {
         kind = "fetchurl";
         url = spec.artifact.url;
         sha256 = spec.artifact.sha256;
         filename = if hasAttr "filename" spec.artifact then spec.artifact.filename else null;
       };
       
       # Handle single file or multiple files
       modelPath =
         if source.kind == "fetchurl"
         then
           # Check if this is a multi-file model
           if hasAttr "shards" spec then
             # Return derivation that combines shards
             mkMultiShardModel source.shards
           else
             pkgs.fetchurl {
               url = source.url;
               sha256 = source.sha256;
             }
         else source.path;
   ```

2. **Add mkMultiShardModel function:**
   ```nix
   mkMultiShardModel = shards:
     pkgs.runCommand "multi-shard-model"
       {
         nativeBuildInputs = [ pkgs.coreutils ];
       }
       (builtins.concatStringsSep "\n" (
         builtins.map (shard:
           ''
             mkdir -p $out
             cp ${pkgs.fetchurl {
               url = shard.url;
               sha256 = shard.sha256;
             }} $out/$(basename ${shard.url})
           ''
         ) shards
       ))
   ```

### 3b: Update homeModule/min/default.nix

**File:** `Components/CriomOS/nix/homeModule/min/default.nix`

**Changes needed:**

1. **Update LiteLLM router config:**
   ```nix
   litellmRouterYaml = ''
     ---
     model_list:
       - model_name: llama-3.2-1b-instruct
         litellm_params:
           model: openai/prometheus-llama-3.2-1b-instruct
           api_base: http://${prometheusLlamaUpstreamHost}:11436/v1
           api_key: ${prometheusLlamaApiKey}
         order: 1
       - model_name: qwen3.5-35b-a3b
         litellm_params:
           model: openai/prometheus-qwen3.5-35b-a3b
           api_base: http://${prometheusLlamaUpstreamHost}:11437/v1
           api_key: ${prometheusLlamaApiKey}
         order: 2
       - model_name: deepseek-r1-distill-llama-70b
         litellm_params:
           model: openai/prometheus-deepseek-r1-distill-llama-70b
           api_base: http://${prometheusLlamaUpstreamHost}:11438/v1
           api_key: ${prometheusLlamaApiKey}
         order: 3
     router_settings:
       enable_pre_call_checks: true
       model_group_alias:
         llama-3.2-1b-instruct: llama-3.2-1b-instruct
         qwen3.5-35b-a3b: qwen3.5-35b-a3b
         deepseek-r1-distill-llama-70b: deepseek-r1-distill-llama-70b
   ```

2. **Update default model settings:**
   ```nix
   defaultModel =
     if builtins.hasAttr "defaultModel" prometheusModelCatalog
     then prometheusModelCatalog.defaultModel
     else "deepseek-r1-distill-llama-70b";  # Changed from main-reasoning
   ```

---

## Phase 4: Update prometheus-model-catalog.json

**File:** `Components/CriomOS/data/config/pi/prometheus-model-catalog.json`

```json
{
  "defaultProvider": "prometheus",
  "defaultModel": "deepseek-r1-distill-llama-70b",
  "provider": "prometheus",
  "serviceEndpoints": {
    "canonical": "http://[202:68bc:1221:1b13:5397:2a56:4aea:d4a9]:11434/v1",
    "backup": "http://[202:68bc:1221:1b13:5397:2a56:4aea:d4a9]:11437/v1"
  },
  "models": [
    {
      "id": "llama-3.2-1b-instruct",
      "descriptor": "Llama 3.2 1B Instruct (Prometheus sanity lane)",
      "alias": "prometheus/llama-3.2-1b-instruct",
      "contextWindow": 8192,
      "maxTokens": 2048,
      "reasoning": false
    },
    {
      "id": "qwen3.5-35b-a3b",
      "descriptor": "Qwen 3.5 35B A3B Q8_0 (Prometheus coding lane)",
      "alias": "prometheus/qwen3.5-35b-a3b",
      "contextWindow": 196608,
      "maxTokens": 4096,
      "reasoning": true
    },
    {
      "id": "deepseek-r1-distill-llama-70b",
      "descriptor": "DeepSeek R1 Distill Llama 70B (Prometheus main reasoning)",
      "alias": "prometheus/deepseek-r1-distill-llama-70b",
      "contextWindow": 131072,
      "maxTokens": 16384,
      "reasoning": true
    }
  ],
  "aliasTargets": {
    "llama-3.2-1b-instruct": "llama-3.2-1b-instruct",
    "qwen3.5-35b-a3b": "qwen3.5-35b-a3b",
    "deepseek-r1-distill-llama-70b": "deepseek-r1-distill-llama-70b"
  },
  "enabledAliases": [
    "prometheus/llama-3.2-1b-instruct",
    "prometheus/qwen3.5-35b-a3b",
    "prometheus/deepseek-r1-distill-llama-70b"
  ],
  "declaredModelMenu": [
    {
      "id": "llama-3.2-1b-instruct",
      "descriptor": "Llama 3.2 1B Instruct",
      "sizeClass": "tiny",
      "reasoning": false
    },
    {
      "id": "qwen3.5-35b-a3b",
      "descriptor": "Qwen 3.5 35B A3B",
      "sizeClass": "72gb-vram-class",
      "reasoning": true
    },
    {
      "id": "deepseek-r1-distill-llama-70b",
      "descriptor": "DeepSeek R1 Distill Llama 70B",
      "sizeClass": "128gb-vram-class",
      "reasoning": true
    }
  ]
}
```

---

## Phase 5: Update All Code References

**Goal:** Rename all aliases from generic names to actual model names throughout the codebase.

### Search and Replace Commands

Run these grep/find commands to locate all references:

```bash
# Find all references to old aliases
cd /home/li/git/Mentci-AI--dev
grep -r "main-reasoning" --include="*.nix" --include="*.json" --include="*.md" .
grep -r "main-sanity" --include="*.nix" --include="*.json" --include="*.md" .
grep -r "main-deepseek" --include="*.nix" --include="*.json" --include="*.md" .
grep -r "\"fast\"" --include="*.nix" --include="*.json" --include="*.md" .
```

### Files to Update

1. **config/pi/prometheus-agent-settings.json**
   - Update `defaultModel` from `main-reasoning` to `deepseek-r1-distill-llama-70b`
   - Update `enabledModels` array

2. **Components/CriomOS/nix/homeModule/min/default.nix**
   - Update `defaultModel` assignment
   - Update any hardcoded references to `main-reasoning`

3. **Documentation files:**
   - `docs/research/prometheus-llama-history.md`
   - All plan files in `docs/plans/` that reference old aliases
   - `Components/CriomOS/readme.md`

4. **Research files:**
   - Any research notes referencing old aliases

### Replacement Pattern

```
OLD                          → NEW
main-reasoning              → deepseek-r1-distill-llama-70b
main-sanity                 → llama-3.2-1b-instruct
main-deepseek               → deepseek-r1-distill-llama-70b
fast                        → qwen3.5-35b-a3b (or keep as is if it's a separate alias)
prometheus/main-reasoning   → prometheus/deepseek-r1-distill-llama-70b
prometheus/main-sanity      → prometheus/llama-3.2-1b-instruct
```

---

## Phase 6: Build and Test

### 6a: Build CriomOS Configuration

```bash
# Build the Prometheus OS configuration
cd /home/li/git/Mentci-AI--dev/Components/CriomOS
nix-build -A metalSystem -I nixpkgs=channel:nixos-25.05

# Or use the attrs system
nix build .#metalSystem
```

### 6b: Verify Model Configuration

```bash
# Check that all models are in the Nix store
nix-store --query --references result | grep -i "llama\|qwen\|deepseek"

# Check the litellm-router.yaml
cat result/etc/litellm-router.yaml

# Verify model ports
grep "port" result/etc/litellm-router.yaml
```

### 6c: Test on Prometheus (if accessible)

```bash
# SSH to Prometheus
ssh li@202:68bc:1221:1b13:5397:2a56:4aea:d4a9

# Check current services
systemctl list-units | grep llama

# Check model files
ls -lh ~/.local/share/prometheus-llama/models/

# Test llama-server endpoints
curl http://localhost:11436/v1/models
curl http://localhost:11437/v1/models
curl http://localhost:11438/v1/models

# Test LiteLLM gateway
curl http://localhost:11434/v1/models
```

### 6d: Test Pi Agent

```bash
# Test with new model names
pi --provider prometheus --model deepseek-r1-distill-llama-70b -p 'Say hello'
pi --provider prometheus --model qwen3.5-35b-a3b -p 'Say hello'
pi --provider prometheus --model llama-3.2-1b-instruct -p 'Say hello'

# Test with old names (should fail after migration)
pi --provider prometheus --model main-reasoning -p 'Say hello'
```

---

## Phase 7: Deploy to Prometheus

### 7a: Commit Changes

```bash
cd /home/li/git/Mentci-AI--dev
jj commit -m "Migrate LLM models to Nix store and rename aliases

- Add DeepSeek-R1-Distill-Llama-70B to prometheus-model-lock.json
- Migrate all models to fixed-output derivations
- Rename aliases: main-reasoning → deepseek-r1-distill-llama-70b
- Update Pi agent configs with new model names
- Update documentation"
```

### 7b: Push to Origin

```bash
jj git push
```

### 7c: Deploy to Prometheus

```bash
# Use the existing CriomOS deployment mechanism
cd /home/li/git/Mentci-AI--dev/Components/CriomOS
just deploy-prometheus  # or whatever deployment command you use

# Or use nixos-rebuild if applicable
nixos-rebuild switch --flake .#prometheus
```

### 7d: Verify Deployment

```bash
# SSH to Prometheus and verify
ssh li@202:68bc:1221:1b13:5397:2a56:4aea:d4a9

# Check services
systemctl status prometheus-llama-llama-3.2-1b-instruct
systemctl status prometheus-llama-qwen3.5-35b-a3b
systemctl status prometheus-llama-deepseek-r1-distill-llama-70b

# Check LiteLLM
systemctl status prometheus-litellm

# Test endpoints
curl http://localhost:11438/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{"model": "deepseek-r1-distill-llama-70b", "messages": [{"role": "user", "content": "Hello"}]}'
```

---

## Phase 8: Rollback Plan

### If Something Goes Wrong

1. **Revert the commit:**
   ```bash
   jj undo
   ```

2. **Or revert to previous bookmark:**
   ```bash
   jj git pull --rebase
   jj checkout main~1
   ```

3. **Restore old configuration:**
   - Revert `prometheus-model-lock.json` to previous state
   - Restore old aliases in all config files

4. **Redeploy old configuration:**
   ```bash
   nixos-rebuild switch --flake .#prometheus
   ```

---

## Success Criteria

- [ ] All models served from Nix store (verified via `nix-store --query`)
- [ ] All model aliases renamed to actual model names (verified via grep)
- [ ] Prometheus OS update successfully deploys new configuration
- [ ] Pi agent can use new model names (`pi --model deepseek-r1-distill-llama-70b`)
- [ ] No breaking changes to existing functionality
- [ ] DeepSeek-R1-Distill-Llama-70B is the new `defaultModel`
- [ ] All three models (Llama, Qwen, DeepSeek) are running and responding

---

## Files Modified Summary

| File | Changes |
|------|---------|
| `Components/CriomOS/data/config/pi/prometheus-model-lock.json` | Add DeepSeek, update all models with FODs |
| `Components/CriomOS/data/config/pi/prometheus-model-catalog.json` | Rename aliases, add DeepSeek |
| `Components/CriomOS/nix/mkCriomOS/llm.nix` | Add multi-file model support |
| `Components/CriomOS/nix/homeModule/min/default.nix` | Update LiteLLM router, default model |
| `config/pi/prometheus-agent-settings.json` | Update defaultModel, enabledModels |
| `docs/research/prometheus-llama-history.md` | Update documentation |
| All plan files in `docs/plans/` | Update references to old aliases |

---

## Notes

1. **Multi-file model handling:** DeepSeek has 2 shards that must be handled together. The `llm.nix` module needs to be updated to support this.

2. **Hash computation:** You'll need to compute SHA256 hashes for DeepSeek shards. Use either:
   - Local files: `nix-hash --type sha256 --flat /path/to/file`
   - HuggingFace: `fetchurl` with known hash (if available online)

3. **VRAM requirements:** DeepSeek-R1-Distill-Llama-70B Q8_0 requires ~35-40GB VRAM. Ensure Prometheus has sufficient GPU memory.

4. **Testing:** Test each model individually before enabling all three simultaneously.

5. **Documentation:** Update any external documentation or runbooks that reference the old model names.

---

**Status:** Plan ready for implementation.  
**Next Step:** Compute SHA256 hashes and begin Phase 2.

