# Cap'n Proto Tooling Interface (MCP)

- **Solar:** 5919.12.9.53.15
- **Subject:** `Capnp-Tooling-Interface`
- **Status:** `design-draft`
- **Weight:** `High`

## 1. Intent
To strictly enforce the **Rust-Only Mandate** by deprecating ad-hoc `sync.sh` bash scripts used for compiling Cap'n Proto messages. Instead, we propose a native Rust MCP interface (`mentci-mcp-capnp` or an extension to `mentci-mcp`) that provides multiple, highly specific tools to eliminate agent guesswork when dealing with Cap'n Proto schemas and binary messages.

## 2. Motivation
Previously, agents relied on generating `sync.sh` scripts to hash text files, invoke the `capnp` CLI, and manage symlinks for prebuilt binary messages. This violates:
1.  **No Shell Scripts:** All new logic must be Sema-grade Rust.
2.  **Agent Guesswork:** Agents frequently guess the wrong `capnp encode` flags (e.g., packed vs. unpacked) or fail to correctly implement the `name_<hash>.bin` synchronization rule.

Providing *multiple, highly specific tools* is superior to providing one generic "run shell command" tool with a long list of complex instructions.

## 3. Proposed Rust MCP Interface

We will expose a suite of dedicated MCP tools specifically designed for Cap'n Proto operations. This removes the burden of execution mechanics from the agent's context.

### Tool 1: `capnp_inspect_schema`
**Purpose:** Eliminates the need to guess root struct names or manually grep `.capnp` files.
- **Input:** `schema_path` (String)
- **Output:** JSON list of available structs, enums, and interfaces within the schema.

### Tool 2: `capnp_encode_message`
**Purpose:** Safely compiles a text-based format (JSON/EDN/Aski) into a Cap'n Proto binary without the agent constructing shell pipelines.
- **Inputs:** 
  - `schema_path` (String)
  - `root_struct` (String)
  - `text_payload` (String)
  - `packed` (Bool, default `true`)
- **Output:** Returns a base64-encoded binary string or writes to a specified temporary path.

### Tool 3: `capnp_decode_message`
**Purpose:** Allows the agent to read and verify an existing binary message.
- **Inputs:**
  - `schema_path` (String)
  - `root_struct` (String)
  - `binary_path` (String)
- **Output:** The decoded text payload (JSON or Aski).

### Tool 4: `capnp_sync_protocol` (The High-Level Workhorse)
**Purpose:** A dedicated, idiot-proof tool that executes the Mentci "Hash-Synced Message" protocol automatically.
- **Inputs:**
  - `schema_path` (String) - e.g., `Components/mentci-stt/schema/stt.capnp`
  - `root_struct` (String) - e.g., `SttRequest`
  - `text_source_path` (String) - e.g., `Components/mentci-stt/data/default_request.txt`
  - `output_dir` (String) - e.g., `Components/mentci-stt/data/`
  - `base_name` (String) - e.g., `default_request`
- **Execution Logic (in Rust):**
  1. Reads `text_source_path`.
  2. Computes the SHA-256 hash (truncated to 16 chars).
  3. Uses the `capnpc` Rust crate (or spawns the `capnp` CLI internally) to encode the message.
  4. Writes the output to `output_dir/base_name_<hash>.bin`.
  5. Updates the `output_dir/base_name.bin` symlink to point to the new hash.
  6. Cleans up older `<hash>.bin` files matching the `base_name`.
- **Output:** Success message confirming the generated hash and file paths.

## 4. Implementation Plan
1. **Remove Legacy Scripts:** Delete any existing `sync.sh` files (Completed).
2. **Scaffold Component:** Add a Cap'n Proto module to the existing `Components/mentci-mcp` server.
3. **Register MCP Tools:** Map the four tools defined above to the MCP registry.
4. **Update Agent Instructions:** Instruct agents to strictly use `capnp_sync_protocol` whenever modifying text-version configurations.
