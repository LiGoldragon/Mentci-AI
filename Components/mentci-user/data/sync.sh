#!/usr/bin/env bash
set -e
TEXT_FILE="setup.txt"
HASH=$(sha256sum "$TEXT_FILE" | head -c 16)
BIN_FILE="setup_${HASH}.bin"

if [ ! -f "$BIN_FILE" ]; then
    echo "Re-compiling $BIN_FILE..."
    # remove old
    rm -f setup_*.bin
    capnp encode ../schema/mentci_user.capnp UserSetupConfig < "$TEXT_FILE" > "$BIN_FILE"
fi
echo "Synced hash: $HASH"
