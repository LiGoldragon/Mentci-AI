# Nix Fixed-Output Derivations for GGUF Models

**Date:** 2026-03-15  
**Purpose:** Research on making GGUF model management pure and reproducible in Nix

---

## Executive Summary

**Yes, it is absolutely possible to make GGUF models pure via fixed-output derivations in Nix.** This is the recommended approach for CriomOS to maintain system purity.

### Key Insights from Research

1. **Fixed-output derivations (FOD)** are designed exactly for this use case
2. They allow network access in the sandbox while enforcing hash verification
3. The workflow: experiment → download → promote to FOD → push to cache → share across machines
4. Multi-file models (sharded GGUFs) can each have their own FOD

---

## How Fixed-Output Derivations Work

### What is a Fixed-Output Derivation?

A fixed-output derivation is a Nix derivation where:
- The output path is determined **solely by the content hash**
- The builder can access the network
- The hash must match what was declared

This makes it "impure" in terms of network access, but **pure in terms of reproducibility** because:
- Any machine that builds it must produce the exact same hash
- If the hash doesn't match, the build fails
- The output is content-addressed

### Basic Pattern

```nix
{ fetchurl, stdenv, sha256 }:

stdenv.mkDerivation {
  name = "DeepSeek-R1-Distill-Llama-70B-Q8_0";
  
  # Download from HuggingFace
  src = fetchurl {
    url = "https://huggingface.co/.../model.gguf";
    sha256 = "sha256-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX=";
  };
  
  # Copy to output
  installPhase = ''
    mkdir -p $out
    cp $src $out/
  '';
}
```

**Key point:** The `sha256` field tells Nix what hash to expect. If the downloaded file doesn't match, the build fails.

---

## Multi-File Model Pattern (Sharded GGUFs)

Your DeepSeek model has 2 shards. Each needs its own FOD:

```nix
{ pkgs, lib }:

let
  # First shard
  deepseek-shard1 = pkgs.fetchurl {
    url = "https://huggingface.co/unsloth/DeepSeek-R1-Distill-Llama-70B-GGUF/resolve/main/DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf";
    sha256 = "sha256-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX=";
  };
  
  # Second shard
  deepseek-shard2 = pkgs.fetchurl {
    url = "https://huggingface.co/unsloth/DeepSeek-R1-Distill-Llama-70B-GGUF/resolve/main/DeepSeek-R1-Distill-Llama-70B-Q8_0-00002-of-00002.gguf";
    sha256 = "sha256-YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY=";
  };
  
  # Combine into a derivation that provides both files
  deepseek-model = pkgs.stdenv.mkDerivation {
    name = "DeepSeek-R1-Distill-Llama-70B-Q8_0";
    
    buildInputs = [ deepseek-shard1 deepseek-shard2 ];
    
    installPhase = ''
      mkdir -p $out
      cp ${deepseek-shard1} $out/DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf
      cp ${deepseek-shard2} $out/DeepSeek-R1-Distill-Llama-70B-Q8_0-00002-of-00002.gguf
    '';
  };
  
in deepseek-model
```

---

## The Promotion Workflow (from qmx's dotfiles)

### Step 1: Experiment Phase
Download model from HuggingFace (impure, but fast):
```nix
models = {
  "DeepSeek-R1-70B" = {
    hf = "unsloth/DeepSeek-R1-Distill-Llama-70B-GGUF:Q8_K_XL";
    ctxSize = 131072;
  };
};
```

This uses llama.cpp's `-hf` flag to download on demand.

### Step 2: Promotion Script
When ready to make permanent:
```bash
# Script that:
# 1. Finds downloaded file
# 2. Computes SHA256 hash
# 3. Adds to local Nix store
# 4. Signs with cache key
# 5. Pushes to cache server
# 6. Outputs catalog entry
nixify-model "unsloth/DeepSeek-R1-Distill-Llama-70B-GGUF:Q8_K_XL"
```

### Step 3: Add to Catalog (Pure)
```nix
ggufs = {
  "unsloth/DeepSeek-R1-Distill-Llama-70B-GGUF:Q8_K_XL" = {
    files = [
      { 
        name = "DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf";
        sha256 = "sha256-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX=";
      }
      { 
        name = "DeepSeek-R1-Distill-Llama-70B-Q8_0-00002-of-00002.gguf";
        sha256 = "sha256-YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY=";
      }
    ];
  };
};
```

### Step 4: Fetch with Integrity Check
```nix
# In Home Manager module
fetchGGUF = hfId:
  if builtins.hasAttr hfId config.models.ggufs
  then
    let
      gguf = config.models.ggufs.${hfId};
    in
    pkgs.runCommand "gguf-${gguf.file}"
      { nativeBuildInputs = [ pkgs.coreutils ]; }
      ''
        # Copy from Nix store (integrity already verified)
        mkdir -p $out
        ${lib.concatMapStrings (f: "cp ${f.path} $out/" gguf.files)}
      ''
  else null;
```

---

## Benefits for CriomOS

### 1. **System Purity**
- All model files are in the Nix store
- Content-addressed paths: `/nix/store/sha256-xxx-DeepSeek-R1-Distill-Llama-70B-Q8_0/`
- No files in `~/.local/share/` or other impure locations

### 2. **Reproducibility**
- Any machine building the system gets the exact same files
- Hash verification prevents corrupted or tampered models
- Build on machine A = Build on machine B

### 3. **Cache Sharing**
- Once pushed to your Nix cache, all machines download from cache
- LAN speed (~2 min for 70GB) vs HuggingFace speed (~20 min)
- No redundant downloads across nodes

### 4. **Declarative Configuration**
- Model catalog is the single source of truth
- Promoted models appear in `prometheus-model-lock.json` with hashes
- Easy to audit: "What models does Prometheus run?"

---

## Implementation in CriomOS

### Current State (Impure)
From `prometheus-model-lock.json`:
```json
{
  "source": {
    "kind": "fetchurl",
    "url": "https://huggingface.co/unsloth/Qwen3.5-35B-A3B-GGUF/resolve/main/Qwen3.5-35B-A3B-Q8_0.gguf",
    "sha256": "3808866c016ab02b4adb26b873f7008a2cdd2c0704a39704050119ab0631db46"
  }
}
```

This is already using `fetchurl` with hash, which IS a fixed-output derivation pattern!

### Problem: DeepSeek Files Are Local
Your DeepSeek files are currently at:
```
/home/li/.local/share/prometheus-llama/models/DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf
```

These are NOT in the Nix store and NOT declared with hashes.

### Solution: Create FODs for DeepSeek Shards

1. **Get the SHA256 hashes of existing files**
2. **Add to `prometheus-model-lock.json`**
3. **Update CriomOS to fetch from Nix store instead of local path**
4. **Push to Nix cache so other machines can use them**

---

## Next Steps

1. **Compute SHA256 of DeepSeek shards on Prometheus**
   ```bash
   nix-hash --type sha256 --flat /path/to/shard1.gguf
   nix-hash --type sha256 --flat /path/to/shard2.gguf
   ```

2. **Add to `prometheus-model-lock.json`**:
   ```json
   {
     "modelId": "deepseek-r1-distill-llama-70b",
     "canonicalId": "deepseek-r1-distill-llama-70b",
     "alias": "prometheus-deepseek",
     "primaryAlias": "main-reasoning",
     "source": {
       "kind": "fetchurl",
       "url": "https://huggingface.co/unsloth/DeepSeek-R1-Distill-Llama-70B-GGUF/resolve/main/DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf",
       "sha256": "sha256-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX="
     },
     "reasoning": true,
     "contextWindow": 131072,
     "maxTokens": 16384,
     "ctxSize": 131072,
     "port": 11438
   }
   ```

3. **Update `llm.nix`** to handle multi-file models

4. **Push to Nix cache** so the derivation is shareable

---

## Sources

1. [Nixifying Local LLMs: Promoting GGUF Models to Nix Derivations](https://random.qmx.me/posts/2026/01/08/nixifying-local-llms/) - Complete workflow example
2. [Nix manual: Fixed-Output Derivations](https://nixos.org/manual/nix/stable/expressions/derivations.html#fixed-output-derivations)
3. [NixOS Discourse: Fixed-output derivations](https://discourse.nixos.org/t/using-fixed-output-paths-for-a-derivation/6338)
4. [NixOS issue #2270: Restrict fixed-output derivations](https://github.com/NixOS/nix/issues/2270)

---

## Conclusion

**Yes, your goal is achievable and is the recommended approach.** Fixed-output derivations are designed for exactly this use case: large binary files that need to be reproducible and shareable.

The current `prometheus-model-lock.json` already uses the `fetchurl` + `sha256` pattern for Qwen3.5. You just need to:
1. Get the hashes of your DeepSeek files
2. Add them to the lock file
3. Push to your Nix cache
4. Update CriomOS to use the Nix store paths

This will make your Prometheus model serving **100% pure and reproducible**.
