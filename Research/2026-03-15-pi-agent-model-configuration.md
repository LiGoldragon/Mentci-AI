# Pi Agent Model Configuration Status

## Current State

**The Pi coding agent on Prometheus is NOT using Prometheus models.**

Current configuration:
- **Provider:** `ollama` (local)
- **Model:** `qwen2.5-coder:7b` (local Ollama instance)

### Verification

```bash
# Check Pi agent settings on Prometheus
cat /home/li/.pi/agent/settings.json
# Output: {"defaultProvider": "ollama", "defaultModel": "qwen2.5-coder:7b", ...}

# Check Pi agent models on Prometheus
cat /home/li/.pi/agent/models.json
# Output: Only ollama provider configured, no prometheus provider
```

## Does Pi Need Reconfiguration?

**NO.** The Pi agent does NOT need to be reconfigured for the new models.

### Why?

1. **Pi is not using Prometheus models** - it's using local Ollama
2. **Even if it did**, the LiteLLM gateway handles model name mapping
3. **The LiteLLM router config** will be updated automatically via NixOS rebuild

## What Changes After Deployment

### Before Deployment

LiteLLM gateway (`http://localhost:11434/v1/models`):
```json
{
  "data": [
    {"id": "llama-3.2-1b-instruct", ...},
    {"id": "main-reasoning", "aliasOf": "qwen3.5-35b-a3b", ...},
    {"id": "main-sanity", "aliasOf": "llama-3.2-1b-instruct", ...}
  ]
}
```

### After Deployment

LiteLLM gateway (`http://localhost:11434/v1/models`):
```json
{
  "data": [
    {"id": "llama-3.2-1b-instruct", ...},
    {"id": "qwen3.5-35b-a3b", ...},
    {"id": "deepseek-r1-distill-llama-70b", ...}
  ]
}
```

**Old aliases will no longer work:**
- `main-sanity` → **ERROR** (no longer mapped)
- `main-reasoning` → **ERROR** (no longer mapped)

**New model names will work:**
- `llama-3.2-1b-instruct` → ✅ Works
- `qwen3.5-35b-a3b` → ✅ Works
- `deepseek-r1-distill-llama-70b` → ✅ Works

## If You Want to Use Prometheus Models with Pi

If you want to configure the Pi agent to use Prometheus models instead of local Ollama, you would need to:

1. **Update Pi agent config** (`/home/li/.pi/agent/settings.json`):
   ```json
   {
     "defaultProvider": "prometheus",
     "defaultModel": "deepseek-r1-distill-llama-70b",
     "enabledModels": [
       "prometheus/llama-3.2-1b-instruct",
       "prometheus/qwen3.5-35b-a3b",
       "prometheus/deepseek-r1-distill-llama-70b"
     ]
   }
   ```

2. **Add Prometheus provider** (`/home/li/.pi/agent/models.json`):
   ```json
   {
     "providers": {
       "prometheus": {
         "baseUrl": "http://localhost:11434/v1",
         "apiKey": "sk-no-key-required",
         "models": [
           {"id": "llama-3.2-1b-instruct", ...},
           {"id": "qwen3.5-35b-a3b", ...},
           {"id": "deepseek-r1-distill-llama-70b", ...}
         ]
       }
     }
   }
   ```

3. **Deploy the updated configs** to Prometheus

**BUT:** This is **NOT required** for the deployment to work. The Prometheus models will be available via the LiteLLM gateway regardless.

## Deployment Impact

| Component | Before | After | Disruption |
|-----------|--------|-------|------------|
| **Pi agent** | Uses local Ollama | Uses local Ollama | None |
| **LiteLLM gateway** | Serves old aliases | Serves new names | Restart required |
| **llama-server** | Serves models via old aliases | Serves models via new names | Restart required |

**Conclusion:** The Pi coding agent is unaffected by the model renaming. Only the LiteLLM gateway and llama-server services need to be restarted.

---

**Date:** 2026-03-15  
**Status:** Pi agent does NOT need reconfiguration
