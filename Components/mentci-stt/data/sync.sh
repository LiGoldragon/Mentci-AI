#!/usr/bin/env bash
set -e
TEXT_FILE="default_request.txt"
HASH=$(sha256sum "$TEXT_FILE" | head -c 16)
BIN_FILE="default_request_${HASH}.bin"

if [ ! -f "$BIN_FILE" ]; then
    echo "Re-compiling $BIN_FILE..."
    # remove old
    rm -f default_request_*.bin
    # Note: mentci-stt main.rs uses read_message (stream), NOT read_message_packed
    # But wait, looking at my previous 'cat' of main.rs:
    # let message_reader = capnp::serialize_packed::read_message(&mut slice, capnp::message::ReaderOptions::new())?;
    # It DOES use packed.
    capnp encode --packed ../../schema/mentci.capnp SttRequest < "$TEXT_FILE" > "$BIN_FILE"
    ln -sf "$BIN_FILE" default_request.bin
fi
echo "Synced hash: $HASH"
