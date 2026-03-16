#!/usr/bin/env bash
#
# nix-prefetch-deepseek-r1-70b.sh
# 
# Prefetches DeepSeek-R1-Distill-Llama-70B GGUF model files to the Nix store
# using nix-prefetch-url, which keeps the files permanently.
#
# Usage:
#   ./nix-prefetch-deepseek-r1-70b.sh [--quant Q8_0|Q4_K_M|...]
#

set -euo pipefail

# HuggingFace model configuration
HF_BASE_URL="https://huggingface.co/unsloth/DeepSeek-R1-Distill-Llama-70B-GGUF/resolve/main"

# Available quantizations and their files
declare -A QUANT_FILES=(
    ["Q8_0"]="DeepSeek-R1-Distill-Llama-70B-Q8_0/DeepSeek-R1-Distill-Llama-70B-Q8_0-00001-of-00002.gguf:DeepSeek-R1-Distill-Llama-70B-Q8_0/DeepSeek-R1-Distill-Llama-70B-Q8_0-00002-of-00002.gguf"
    ["Q4_K_M"]="DeepSeek-R1-Distill-Llama-70B-Q4_K_M.gguf"
    ["Q4_K_S"]="DeepSeek-R1-Distill-Llama-70B-Q4_K_S.gguf"
    ["Q5_K_M"]="DeepSeek-R1-Distill-Llama-70B-Q5_K_M.gguf"
    ["Q6_K"]="DeepSeek-R1-Distill-Llama-70B-Q6_K.gguf"
    ["F16"]="DeepSeek-R1-Distill-Llama-70B-F16/DeepSeek-R1-Distill-Llama-70B-F16.gguf"
)

# Output options
QUANTIZATION="Q8_0"

# Parse arguments
while [[ $# -gt 0 ]]; do
    case "$1" in
        --quant)
            QUANTIZATION="$2"
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Validate quantization
if [[ -z "${QUANT_FILES[$QUANTIZATION]:-}" ]]; then
    echo "Error: Unknown quantization '$QUANTIZATION'"
    echo "Available quantizations: ${!QUANT_FILES[@]}"
    exit 1
fi

# Get file list
IFS=':' read -ra FILES <<< "${QUANT_FILES[$QUANTIZATION]}"

echo "=== DeepSeek-R1-Distill-Llama-70B Prefetch ==="
echo "Model: unsloth/DeepSeek-R1-Distill-Llama-70B-GGUF"
echo "Quantization: $QUANTIZATION"
echo "Files: ${#FILES[@]}"
echo ""

# Prefetch each file using nix-prefetch-url
for FILE_PATH in "${FILES[@]}"; do
    FILE_NAME=$(basename "$FILE_PATH")
    DOWNLOAD_URL="${HF_BASE_URL}/${FILE_PATH}"
    
    echo "Prefetching: $FILE_NAME"
    echo "  URL: $DOWNLOAD_URL"
    
    # Prefetch to Nix store (keeps file permanently)
    STORE_PATH=$(nix-prefetch-url "$DOWNLOAD_URL" --print-path)
    
    echo "  Store path: $STORE_PATH"
    echo ""
done

echo "=== Done ==="
echo "Files are now in the Nix store and will persist."
