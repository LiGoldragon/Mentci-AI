# Aski Projector: Two-Way EDN <-> Cap'n Proto Conversion

- **Solar:** 5919.12.9.54.15
- **Subject:** `Aski-Projector`
- **Status:** `design-draft`
- **Weight:** `High`

## 1. Intent
To implement a "Universal Projector" (likely as `aski-lib` and `aski-cli`) that provides a lossless, two-way conversion between **Aski (EDN-superset)** and **Cap'n Proto (Binary/Text)**. This closes the loop for the "Aski is the text surface for Sema" mandate.

## 2. Motivation
Currently, Mentci-AI suffers from a "Format Membrane" gap:
- **Authoring:** Humans and LLMs prefer Aski/EDN for its conciseness and Lisp-like genius.
- **Logic:** Mentci Rust components speak Sema Objects (Cap'n Proto).
- **Tooling:** We often rely on `capnp encode` which requires a specific, non-standard text format (Capnp-txt).

A two-way projector eliminates the need for agents to manually construct Capnp-txt payloads or ad-hoc JSON wrappers. It allows the system to treat Aski as the primary source-of-truth for *data*, which then projects into Sema for *execution*.

## 3. Core Functionality

### 3.1. Aski -> Sema (Projection)
- **Input:** Aski/EDN data + Cap'n Proto Schema.
- **Process:** Parses Aski into a semantic tree, maps keys to Cap'n Proto field ordinals/names, and emits a packed binary message or a standard Capnp-txt string.
- **Benefit:** LLMs can write configuration in pure Aski; the projector handles the "mechanical task" of binary compilation.

### 3.2. Sema -> Aski (Introspection)
- **Input:** Cap'n Proto binary message + Schema.
- **Process:** Decodes the binary and projects it into a clean, well-formatted Aski/EDN document.
- **Benefit:** When an agent reads a `.bin` message, it can see a human-readable Aski view instead of a hex dump or noisy JSON.

## 4. Architectural Placement
This logic belongs in **`Components/aski-lib`**, which is currently an empty placeholder.
- `aski-lib`: The core Rust library for parsing/rendering.
- `aski-cli`: The terminal interface for manual/scripted projections.
- `mentci-mcp`: The agent-facing interface (via `capnp_encode_message` and `capnp_decode_message` tools).

## 5. Technical Strategy (Rust)
1.  **Crate:** Use `edn-rs` or `serde_edn` for the Aski parser.
2.  **Schema Awareness:** Leverage the `capnp` and `capnpc` crates to dynamically load schemas (or use pre-generated code for core Mentci types).
3.  **Intermediate IR:** Define a "Sema-IR" that can represent the common denominator between Aski and Cap'n Proto (enumerators, lists, structs, unions).

## 6. Suggestions
- **No JSON Fallback:** Avoid using JSON as a middle-layer if possible; go directly from Aski to the Cap'n Proto message builder to preserve semantic richness (e.g., specific bit-widths or keywords).
- **Aski-Sajban Integration:** Ensure the projector understands the `aski-sajban` self-loading property (i.e., it can use a SEMA binary as the "dictionary" for the projection).

## 7. Questions for Author
1.  **Component Naming:** Should we keep the names `aski-lib`/`aski-cli`, or rename them to something more projection-focused like `mentci-aski-projector`?
2.  **Schema Versioning:** How should the projector handle version mismatches between an Aski document and a changing Cap'n Proto contract?
3.  **Tool Priority:** Should this projector be the first thing we implement for the new `mentci-dig` component to help it process the ChatGPT export lineage?
