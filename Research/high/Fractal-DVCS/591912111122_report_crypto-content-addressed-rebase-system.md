
### Recommendation: The CAR (Content Addressed Archive) Format
For the ancestor archiving mechanism, research into modern content-addressed systems (IPFS, IPLD, and Nix experimental features) points to the **CAR (Content Addressed Archive)** format as the next-gen standard.

- **Merkle DAG Native**: CAR files are designed to store a collection of IPLD blocks, which perfectly map to Git/JJ commit trees.
- **Efficient Verification**: They allow for incremental, cryptographic verification of the DAG without unpacking the entire archive.
- **Streamable**: Ideal for the "intercept everything" vision, as they can be fetched and indexed block-by-block.
- **Mentci-AI Choice**: We will adopt CAR as the standard container for "linked ancestor trees" in the MentciCommit schema.

### Intent Redirection: Saṃskāra Programmatic Editing
To move beyond the inefficiency of spawning nested agent TUIs for editing, we are implementing a **Saṃskāra Editor Bridge**.

- **Mechanism**: Set `EDITOR=samskara-editor`.
- **Redirection**: When `jj` calls the editor, the `editor_bridge` binary intercepts the raw text.
- **Symbolic Refinement**: Instead of manual typing, Saṃskāra uses its symbolic understanding of the active "Flow" (defined in `samskara_jj.capnp`) to automatically synthesize a human-information-rich message.
- **Visual Richness**: The bridge will eventually trigger a beautiful TUI renderer (via `pi-tui` or Saṃskāra's own display logic) that presents the commit not as raw text, but as a structured "Intent Symbol."

### Saṃskāra Daemon (Saṃskarad) & Two-Way UI Portability
The core of the symbolic refinement loop lives in **Saṃskarad**, a long-running daemon that manages the transition of raw intent into structured, content-addressed messages.

- **Two-Way Structured Data**: Saṃskāra doesn't just produce text; it produces **Structured UI Intent** (via `samskara_editor.capnp`). This data allows any Mentci-AI compatible frontend (Pi TUI, VTCode, or mentci-flutter) to render the information using intelligent, platform-native components.
- **Subflow Orchestration**: Instead of forcing automatic metadata injection, the editor bridge allows for **explicit subflows**. An agent can take a refinement job and, if context is missing, trigger a specified tool/contract flow to fetch it. This can involve spawning specialized, long-lived sub-agents.
- **Portability**: By separating the symbolic logic (Rust/Saṃskarad) from the display logic, we ensure the same "Commit Intent" looks rich and intuitive whether viewed in a terminal or a mobile app.
