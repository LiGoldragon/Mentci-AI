# Adding Local Files to Nix Store Without Re-Downloading

## Problem Statement

You have files already downloaded on Prometheus at `/home/li/.local/share/prometheus-llama/models/`. Can you add these to the Nix store so that Nix evaluations don't re-download them?

## Short Answer

**Yes! Absolutely.** You can add local files to the Nix store using `nix-store --add` or `nix-prefetch-file`, and then reference them directly in Nix derivations. This avoids any network downloads.

## Methods to Add Local Files

### Method 1: `nix-store --add` (Simplest)

This command copies a local file directly into the Nix store and returns the store path.

**Command:**
```bash
nix-store --add /home/li/.local/share/prometheus-llama/models/DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf
```

**Output:**
```
/nix/store/abc123def456...-DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf
```

**Usage in Nix:**
```nix
let
  deepseekShard1 = /nix/store/abc123def456...-DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf;
  deepseekShard2 = /nix/store/def456abc789...-DeepSeek-R1-Distill-Llama-70B-Q8_0-00002-of-00002.gguf;
in pkgs.runCommand "merged-deepseek"
  { nativeBuildInputs = [ pkgs.coreutils ]; }
  ''
    cat ${deepseekShard1} ${deepseekShard2} > $out/merged.gguf
  ''
```

**Pros:**
- Simple, one command
- Direct store path reference (no hash needed)
- No network access required

**Cons:**
- Store path is machine-specific (won't work on other machines)
- Need to hardcode store paths in Nix expressions

---

### Method 2: `nix-prefetch-file` (Hash + Store)

This command computes the hash AND adds the file to the store in one step.

**Command:**
```bash
nix-prefetch-file /home/li/.local/share/prometheus-llama/models/DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf
```

**Output:**
```
/nix/store/abc123def456...-DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf
sha256-abc123def456...
```

**Usage in Nix:**
```nix
let
  deepseekShard1 = /nix/store/abc123def456...-DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf;
in deepseekShard1
```

**Pros:**
- Gets both store path AND hash in one command
- Hash can be used for FOD derivations
- Works on same machine

**Cons:**
- Store path still machine-specific

---

### Method 3: `nix-hash` + Manual Store Add (Most Control)

Compute the hash first, then add to store.

**Step 1 - Compute hash:**
```bash
nix-hash --type sha256 --to-base32 /home/li/.local/share/prometheus-llama/models/file.gguf
# Output: abc123def456...
```

**Step 2 - Add to store:**
```bash
nix-store --add /home/li/.local/share/prometheus-llama/models/file.gguf
# Output: /nix/store/abc123def456...-file.gguf
```

**Pros:**
- Full control over hash computation
- Hash can be committed to Nix expressions

**Cons:**
- Two separate steps

---

## Recommended Approach for Prometheus Models

Given your use case (files already on Prometheus, need to avoid re-downloading), I recommend:

### Option A: Direct Store Path References (Simplest for Prometheus-only)

Add all model files to the Nix store on Prometheus, then use those store paths directly in your Nix expressions.

**Step 1: Add all model files to the store**
```bash
ssh li@202:68bc:1221:1b13:5397:2a56:4aea:d4a9 "
echo '=== Adding DeepSeek shards ==='
DEEPSHARD1=\$(nix-store --add /home/li/.local/share/prometheus-llama/models/DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf)
DEEPSHARD2=\$(nix-store --add /home/li/.local/share/prometheus-llama/models/DeepSeek-R1-Distill-Llama-70B-Q8_0-00002-of-00002.gguf)

echo '=== Adding Qwen3.5 ==='
QWEN35=\$(nix-store --add /home/li/.local/share/prometheus-llama/models/Qwen3.5-35B-A3B-Q8_0.gguf)

echo '=== Adding Llama-3.2-1B ==='
LLAMA1B=\$(nix-store --add /home/li/.local/share/prometheus-llama/models/llama-3.2-1b-instruct-q4_k_m.gguf)

# Output all store paths for use in Nix expressions
echo '{'
echo '  deepseekShard1 = "'\$DEEPSHARD1'";'
echo '  deepseekShard2 = "'\$DEEPSHARD2'";'
echo '  qwen35 = "'\$QWEN35'";'
echo '  llama1b = "'\$LLAMA1B'";'
echo '}'
"
```

**Step 2: Update `llm.nix` to use these store paths**
```nix
# At the top of llm.nix, define the store paths
let
  prometheusModelPaths = import /path/to/prometheus-models.nix { };
  
  # For single-file models
  llama1bPath = prometheusModelPaths.llama1b;
  qwen35Path = prometheusModelPaths.qwen35;
  
  # For multi-shard models, merge the shards
  deepseekMerged = pkgs.runCommand "deepseek-merged"
    { nativeBuildInputs = [ pkgs.coreutils ]; }
    ''
      cat ${prometheusModelPaths.deepseekShard1} ${prometheusModelPaths.deepseekShard2} > $out/merged.gguf
    '';
in { ... }
```

**Pros:**
- Zero network downloads
- Simplest Nix expressions
- Fastest builds on Prometheus

**Cons:**
- Store paths are machine-specific (won't work on other machines)
- Need to regenerate store paths if files change

### Step 3: Update llm.nix

```nix
# At the top of llm.nix, import the store paths
let
  prometheusModelPaths = import ../../data/config/pi/prometheus-model-store-paths.nix;
  
  # Use these paths in your model specifications
  modelSpecs = [
    {
      modelId = "llama-3.2-1b-instruct";
      source = {
        kind = "local";
        path = prometheusModelPaths.llama1b;
      };
      # ... other fields
    }
    {
      modelId = "qwen3.5-35b-a3b";
      source = {
        kind = "local";
        path = prometheusModelPaths.qwen35;
      };
      # ... other fields
    }
    {
      modelId = "deepseek-r1-distill-llama-70b";
      source = {
        kind = "local-merged";
        shard1 = prometheusModelPaths.deepseekShard1;
        shard2 = prometheusModelPaths.deepseekShard2;
      };
      # ... other fields
    }
  ];
in { ... }
```

---

### Option B: FOD with Local File URLs (Portable but Complex)

Add files to store, compute hashes, and use `fetchurl` with `file://` URLs.

**Step 1: Add to store and get hashes**
```bash
nix-prefetch-file /home/li/.local/share/prometheus-llama/models/file.gguf
# Output: /nix/store/... and sha256-...
```

**Step 2: Use in Nix with file:// URL**
```nix
let
  deepseekShard1 = fetchurl {
    url = "file:///home/li/.local/share/prometheus-llama/models/DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf";
    sha256 = "sha256-abc123...";
  };
in deepseekShard1
```

**Pros:**
- Hash can be committed to Nix expressions
- More portable than raw store paths

**Cons:**
- Still uses local file paths (won't work on other machines)
- More complex Nix expressions
- Build sandbox may not have access to local paths

---

### Option C: Use Both Local and Remote (Best of Both Worlds)

This is the most practical approach:

1. **For local development on Prometheus:** Use `nix-store --add` for immediate availability
2. **For remote builds:** Use `fetchurl` with HuggingFace URLs as fallback
3. **For binary cache:** Add the merged files to your binary cache

**Implementation:**
```nix
# llm.nix - check if local files exist, otherwise fetch from HF
let
  localDeepseekShard1 = if builtins.pathExists "/home/li/.local/share/prometheus-llama/models/DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf"
    then fetchurl {
      url = "file:///home/li/.local/share/prometheus-llama/models/DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf";
      sha256 = "sha256-abc123...";  # Compute once with nix-prefetch-file
    }
    else fetchurl {
      url = "https://huggingface.co/unsloth/DeepSeek-R1-Distill-Llama-70B-GGUF/resolve/main/DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf";
      sha256 = "sha256-abc123...";
    };
in localDeepseekShard1
```

**Pros:**
- Works on Prometheus (uses local files)
- Works on other machines (downloads from HF)
- Can push merged files to binary cache for fast distribution

**Cons:**
- More complex Nix expressions
- Need to maintain both local and remote hashes

---

## Recommended Solution: Hybrid Approach

For your specific case (Prometheus-focused deployment), I recommend:

### Step 1: Add Local Files to Store on Prometheus

Run this on Prometheus to add all model files to the Nix store:

```bash
ssh li@202:68bc:1221:1b13:5397:2a56:4aea:d4a9 "
cd /home/li/.local/share/prometheus-llama/models/

echo '=== Adding all model files to Nix store ==='

# DeepSeek shards
echo 'DeepSeek Shard 1:'
nix-store --add DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf

echo 'DeepSeek Shard 2:'
nix-store --add DeepSeek-R1-Distill-Llama-70B-Q8_0-00002-of-00002.gguf

# Qwen2.5-72B shards (if they exist)
for shard in qwen2.5-72b-instruct-q5_k_m-000*.gguf; do
  echo \"Adding \$shard:\"
  nix-store --add \"\$shard\"
done

# Qwen3.5-35B
echo 'Qwen3.5-35B:'
nix-store --add Qwen3.5-35B-A3B-Q8_0.gguf

# Llama-3.2-1B
echo 'Llama-3.2-1B:'
nix-store --add llama-3.2-1b-instruct-q4_k_m.gguf

echo '=== All files added ==='
"

echo "=== Mapping saved to Components/CriomOS/data/config/pi/prometheus-model-store-paths.nix ==="
echo "=== Now update llm.nix to import this file and use the store paths ==="
```

### Step 2: Create a Mapping File

Generate a file that maps model names to their store paths:

```bash
ssh li@202:68bc:1221:1b13:5397:2a56:4aea:d4a9 "
nix-prefetch-file /home/li/.local/share/prometheus-llama/models/DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf | grep 'sha256-' | head -1
" > /tmp/deepseek-shard1-hash.txt
```

### Step 3: Update `llm.nix` to Use Local Files

```nix
# Add to the top of llm.nix
let
  # Local model paths on Prometheus (computed once with nix-store --add)
  prometheusModelPaths = {
    deepseekShard1 = /nix/store/abc123...-DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf;
    deepseekShard2 = /nix/store/def456...-DeepSeek-R1-Distill-Llama-70B-Q8_0-00002-of-00002.gguf;
    qwen35 = /nix/store/ghi789...-Qwen3.5-35B-A3B-Q8_0.gguf;
    llama1b = /nix/store/jkl012...-llama-3.2-1b-instruct-q4_k_m.gguf;
  };
  
  # Merge DeepSeek shards
  deepseekMerged = pkgs.runCommand "deepseek-merged"
    { nativeBuildInputs = [ pkgs.coreutils ]; }
    ''
      cat ${prometheusModelPaths.deepseekShard1} ${prometheusModelPaths.deepseekShard2} > $out/merged.gguf
    '';
in
# Use these in your model specifications
{
  # ... rest of your Nix module
}
```

### Step 4: Commit the Mapping to Your Repository

Create a file `Components/CriomOS/data/config/pi/prometheus-model-store-paths.nix`:

```nix
# Auto-generated: Run `nix-store --add` on Prometheus and copy output here
{
  deepseekShard1 = "/nix/store/abc123def456789...-DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf";
  deepseekShard2 = "/nix/store/def456abc789012...-DeepSeek-R1-Distill-Llama-70B-Q8_0-00002-of-00002.gguf";
  qwen35 = "/nix/store/ghi789jkl012345...-Qwen3.5-35B-A3B-Q8_0.gguf";
  llama1b = "/nix/store/jkl012mno345678...-llama-3.2-1b-instruct-q4_k_m.gguf";
}
```

Then in `llm.nix`:
```nix
let
  prometheusModelPaths = import ../../data/config/pi/prometheus-model-store-paths.nix;
  # ... use these paths
in { ... }
```

**Warning:** This approach is **machine-specific**. The store paths on Prometheus won't work on other machines. If you want portability, see Option C above.

---

## Complete Workflow Script

Here's a complete script to add all models to the Nix store and generate a mapping file:

```bash
#!/bin/bash
# add-prometheus-models-to-store.sh

PROMETHEUS_HOST="li@202:68bc:1221:1b13:5397:2a56:4aea:d4a9"
MODELS_DIR="/home/li/.local/share/prometheus-llama/models"

echo "=== Adding Prometheus models to Nix store ==="

# SSH to Prometheus and add files
ssh $PROMETHEUS_HOST "
models=(
  'DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf'
  'DeepSeek-R1-Distill-Llama-70B-Q8_0-00002-of-00002.gguf'
  'Qwen3.5-35B-A3B-Q8_0.gguf'
  'llama-3.2-1b-instruct-q4_k_m.gguf'
)

echo '{'

for model in \"\${models[@]}\"; do
  full_path=\"\$MODELS_DIR/\$model\"
  if [ -f \"\$full_path\" ]; then
    echo \"  \\\"$(echo \$model | sed 's/\\.gguf$//' | sed 's/-[0-9]*-of-[0-9]*\\.gguf$//' | tr '[:upper:]' '[:lower:]' | tr '-' '_')\\\" = \\\"\$(nix-store --add \\\"\$full_path\\\")\\\",\"
  else
    echo \"  # \\\"$model\\\" not found at \$full_path\"
  fi