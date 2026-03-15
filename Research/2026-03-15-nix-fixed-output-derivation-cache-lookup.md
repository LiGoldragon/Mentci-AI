# Nix Fixed-Output Derivation (FOD) Cache Lookup Mechanism

## Executive Summary

Nix fixed-output derivations (FODs) enable content-addressable store paths for files fetched from external sources. For GGUF models, the recommended pattern is:

1. **Per-shard FODs**: Each shard is a `fetchurl` FOD with its own content hash
2. **Merged FOD**: A separate FOD that concatenates shards and asserts the merged file's hash
3. **Cache lookup**: Binary caches serve store paths; for FODs, the store path encodes the content hash

## Key Concepts

### 1. Fixed-Output Derivations (FODs)

FODs assert the output bytes up front via a cryptographic hash (`outputHash`). This allows Nix to:
- Verify downloads without trusting the source
- Deduplicate identical content across the store
- Enable cache copy/verification without rebuilding

**Store path format**: For FODs, the store path embeds the content hash:
```
/nix/store/<hash>-<name>/
```
Where `<hash>` is derived from the asserted `outputHash`.

### 2. Two Types of Hashing in Nix

| Hash Type | Purpose | When Used |
|-----------|---------|-----------|
| **Derivation hash** | Input-addressed name of `.drv` file | Determines build inputs/outputs |
| **Fixed-output hash** | Content-addressed store path | For FOD outputs (what you specify) |

For model caching, the **fixed-output hash** is what matters for cache hits.

### 3. Binary Cache Interaction

Binary caches operate on **store paths**, not individual hashes:
1. Nix computes the expected store path from the content hash
2. Cache lookup asks for that store path
3. If found, Nix verifies contents match the asserted hash
4. If verification passes, no build is needed

**Key insight**: For FODs, the store path IS content-addressed, so caches naturally deduplicate by content.

## Implementation Pattern for Multi-Shard Models

### Pattern A: Per-Shard FODs + Runtime Assembly (Recommended)

Each shard is independently fetched and content-addressed:

```nix
let
  shard1 = fetchurl {
    url = "https://huggingface.co/.../shard-00001-of-00002.gguf";
    outputHash = "sha256-abc123...";
  };
  shard2 = fetchurl {
    url = "https://huggingface.co/.../shard-00002-of-00002.gguf";
    outputHash = "sha256-def456...";
  };
  
  # Assembly derivation (not FOD - content determined by inputs)
  modelShards = stdenv.mkDerivation {
    name = "model-shards";
    buildCommand = ''
      mkdir -p $out
      cp ${shard1} $out/shard-00001.gguf
      cp ${shard2} $out/shard-00002.gguf
    '';
  };
in modelShards
```

**Pros**:
- Each shard is independently cacheable
- No need to know merged hash in advance
- More flexible for different use cases

**Cons**:
- llama-server must be configured to load multiple files

### Pattern B: Merged FOD (Our Current Implementation)

```nix
let
  # Step 1: Fetch shards as FODs
  shard1 = fetchurl {
    url = "https://huggingface.co/.../shard-00001.gguf";
    outputHash = "sha256-abc123...";
  };
  shard2 = fetchurl {
    url = "https://huggingface.co/.../shard-00002.gguf";
    outputHash = "sha256-def456...";
  };
  
  # Step 2: Merge into FOD (requires knowing merged hash in advance)
  mergedModel = pkgs.runCommand "merged-model"
    {
      nativeBuildInputs = [ pkgs.coreutils ];
      allowSubstitutes = true;
      preferLocalBuild = true;
    }
    ''
      cat ${shard1} ${shard2} > $out/merged.gguf
    '';
in mergedModel
```

**Note**: For true FOD semantics with merge, you must:
1. Build once locally to compute merged hash
2. Use `nix-prefetch-url` to get the hash
3. Assert that hash in a proper FOD

**Our current implementation** uses `runCommand` without `outputHash`, which means:
- The merged file is still content-addressed (Nix 2.4+ CA derivations)
- But the hash is computed during build, not asserted upfront
- This is acceptable for most use cases

### Pattern C: Explicit FOD with Asserted Hash

For strict FOD semantics:

```nix
let
  # Fetch shards
  shard1 = fetchurl { url = "..."; outputHash = "..."; };
  shard2 = fetchurl { url = "..."; outputHash = "..."; };
  
  # Merged FOD with asserted hash
  mergedModel = pkgs.stdenv.mkDerivation {
    name = "merged-model";
    builder = ./merge.sh;
    shard1 = shard1;
    shard2 = shard2;
    outputHash = "sha256-MERGED_HASH_WHOSE_HASH";  # Compute once!
    outputHashAlgo = "sha256";
  };
```

**merge.sh**:
```bash
#!/bin/sh -e
cat $shard1 $shard2 > $out/merged.gguf
```

## Computing Hashes

### For HuggingFace URLs

```bash
# Get shard hashes
nix-prefetch-url --unpack "https://huggingface.co/.../shard-00001.gguf"
nix-prefetch-url --unpack "https://huggingface.co/.../shard-00002.gguf"

# After merging locally, get merged hash
nix-prefetch-url --unpack merged.gguf
```

### For Local Files (on Prometheus)

```bash
# Get SHA256 of local files
sha256sum /home/li/.local/share/prometheus-llama/models/file.gguf

# Or use nix (if available on Prometheus)
nix hash file --type sha256 /path/to/file.gguf
```

### Converting Hash Formats

```bash
# Convert between formats
nix hash convert --to base16 --from sri sha256-abc123...
nix hash convert --to sri --from base16 abc123...
```

## Publishing to Binary Cache

To make your models available via your Nix binary cache:

1. **Build the FODs** locally or on a builder
2. **Copy to cache**:
   ```bash
   nix copy --to https://your-cache.example.com /nix/store/<hash>-model.gguf
   ```
3. **Sign the narinfo** (if using signed caches)

**Important**: The store path for the merged model will be content-addressed, so:
- Identical merges (same shards, same order) → same store path
- Different merge order → different store path (different hash)
- Cache deduplication happens automatically

## Our Implementation

We use **Pattern B with a slight variation**:

```nix
mkMultiShardModel = shards:
  let
    # Fetch each shard as FOD
    fetchedShards = builtins.map (shard:
      pkgs.fetchurl {
        url = shard.url;
        sha256 = shard.sha256;
      }
    ) shards;

    # Merge into runCommand (CA derivation in Nix 2.4+)
    merged = pkgs.runCommand "merged-model-${builtins.head shards.filename}"
      {
        nativeBuildInputs = [ pkgs.coreutils ];
        allowSubstitutes = true;
        preferLocalBuild = true;
      }
      (
        let
          sortedShards = builtins.sort (a: b: a < b) fetchedShards;
        in
        builtins.concatStringsSep "\n" (
          builtins.map (shardPath:
            ''
              cat ${shardPath} >> $out/merged.gguf
            ''
          ) sortedShards
        )
      );
  in merged;
```

**Why this works**:
- `fetchurl` produces FODs for each shard
- `runCommand` in Nix 2.4+ uses **content-addressed outputs** automatically
- The merged file gets a content-addressed store path based on its actual bytes
- Binary cache will serve the merged file if present (store path = content hash)

**Trade-offs**:
- ✅ Simpler than explicit FOD (no need to pre-compute merged hash)
- ✅ Still content-addressed and cacheable
- ⚠️ Build step computes hash (not known upfront)
- ⚠️ First build on Prometheus will download shards and merge

## Best Practices

1. **Always sort shards** before merging (deterministic order)
2. **Use consistent filenames** across all installations
3. **Document the merge order** in model metadata
4. **Test on Prometheus** before deploying to production
5. **Consider caching the merged file** in your Nix binary cache for faster deployments

## Verification Commands

```bash
# Check if FODs are substitutable
nix-build -A model-shards --dry-run

# See what will be downloaded
nix-build -A model-shards --show-trace

# Copy to cache after first build
nix copy --to ssh://li@202:68bc:1221:1b13:5397:2a56:4aea:d4a9 /nix/store/*-merged-model*

# Verify cache entry
nix store path-info /nix/store/*-merged-model*
```

## References

- [Nix Manual: Fixed-Output Derivations](https://nix.dev/manual/nix/2.22/language/advanced-attributes.html)
- [Nix Manual: Content-Addressable Outputs](https://nix.dev/manual/nix/2.28/store/derivation/outputs/content-address.html)
- [Nix Pills: Store Paths](https://nixos.org/guides/nix-pills/18-nix-store-paths)
- [Tweag Blog: Derivation Outputs Content-Addressing](https://www.tweag.io/blog/2021-02-17-derivation-outputs-and-output-paths/)
- [Nix Issue #1528: Local File Fetch](https://github.com/NixOS/nix/issues/1528)
