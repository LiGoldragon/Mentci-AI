# Research Report: Pi API Specification & Cap'n Proto Bridge

## 1. Objective
Formalize the Pi Extension API (`ExtensionAPI` and `ExtensionContext`) into a Cap'n Proto contract. This allows implementing extension logic in Rust (or any Sema-grade language) and provides a stable, multi-harness channel compatible with `vtcode`.

## 2. Analysis of Pi Extension API Surface
Based on `pi` source definitions (`types.d.ts`), the primary interaction points are:

### A. Lifecycle Events (`on`)
- `session_start`, `session_switch`, `session_fork`, `session_shutdown`, `session_compact`, `session_tree`.
- `agent_start`, `agent_end`, `turn_start`, `turn_end`.
- `message_start`, `message_update`, `message_end`.
- `tool_call`, `tool_result`.

### B. Tooling & Command Registration
- `registerTool`: Defines tool name, description, schema, and implementation.
- `registerCommand`: Defines slash commands.
- `registerShortcut`: Defines keyboard shortcuts.

### C. UI & Context Interaction
- `ui`: `notify`, `setStatus`, `setWidget`, `pasteToEditor`, `setEditorText`, `getEditorText`.
- `sendMessage`, `sendUserMessage`, `appendEntry`.
- `exec`: Running shell commands.
- `getContextUsage`: Retrieving token/window stats.

## 3. Proposed Cap'n Proto Contract (`pi_api.capnp`)

The contract should follow the **Single Object In/Out** rule from `SEMA_RUST_GUIDELINES.md`.

```capnp
@0x...;

interface PiExtensionBridge {
  # The host (Pi/VTCode) calls the extension (Rust)
  
  handleEvent @0 (event :PiEvent) -> (response :PiEventResponse);
  
  executeTool @1 (request :ToolRequest) -> (result :ToolResult);
  
  executeCommand @2 (request :CommandRequest) -> (response :CommandResponse);
}

interface PiHost {
  # The extension (Rust) calls back to the host (Pi/VTCode)
  
  sendMessage @0 (message :Message) -> ();
  uiAction @1 (action :UiAction) -> ();
  execShell @2 (command :Text, args :List(Text)) -> (result :ShellResult);
}

struct PiEvent {
  union {
    sessionStart @0 :Void;
    agentStart @1 :Void;
    turnEnd @2 :TurnEndDetails;
    # ... other lifecycle events
  }
}

# ... full schema modeling all Pi types
```

## 4. Reusing Sema Principles (Philosophy of Intent)

### A. Embedding Full-Specified Logic
Rust implementation will use the `tractor` actor model. Each capability of the Pi API will be handled by a specific actor, ensuring state sovereignty and supervised recovery.

### B. Logical Abstraction (Aski/Lojix)
While the Cap'n Proto contract provides the "wire durability," we will expose a **Lojix interface** for easy authoring.
- The Rust component will parse Lojix-specified behavior.
- The TS wrapper remains a "dumb proxy" that simply pipes binary Capnp messages.

## 5. Implementation Roadmap

1.  **Contract Definition:** Create `Components/mentci-bridge/schema/pi_api.capnp`.
2.  **TS Proxy Layer:** Develop `.pi/extensions/mentci-rust-bridge.ts` using `capnp-ts`.
3.  **Rust Implementation:** Create `Components/mentci-bridge-rs` using `capnp-rpc` or a simple stdio-based message loop.
4.  **VTCode Integration:** Use the same `pi_api.capnp` to implement a bridge in `vtcode`'s backend, allowing the same Rust logic to drive both UIs.

## 6. Advantage: Faster UI & Portability
- **Speed:** Implementation logic moves from interpreted JS (in the extension host) to compiled Rust.
- **Portability:** The "Mental Map" of how an agent interacts with a harness is now a fixed binary contract. Switching from Pi to VTCode only requires implementing the `PiHost` interface on the VTCode side.
