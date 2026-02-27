#!/usr/bin/env bash
set -e
TEXT_FILE="intelligence.txt"
HASH=$(sha256sum "$TEXT_FILE" | head -c 16)
BIN_FILE="intelligence_${HASH}.bin"

if [ ! -f "$BIN_FILE" ]; then
    echo "Re-compiling $BIN_FILE..."
    # remove old
    rm -f intelligence_*.bin
    capnp encode ../schema/intelligence.capnp IntelligenceCarrier < "$TEXT_FILE" > "$BIN_FILE"
fi
echo "Synced hash: $HASH"
