# Research Analysis: Attractor Ecosystem & Related Works

**Date:** ♓︎3°57'32" | 5919 AM
**Subject:** Analysis of StrongDM and Brynary (Bryan Helmkamp) repositories for assimilation insights.

## 1. StrongDM / Comply
*   **Source:** `/nix/store/yc5hbqb03s5hmlq854mz4bxigamywlca-comply`
*   **Type:** Compliance Automation Framework (SOC2).
*   **Language:** Go.
*   **Architecture:**
    *   **Document Pipeline:** Markdown-to-PDF/HTML via Pandoc.
    *   **Ticketing Integration:** Syncs compliance controls with Jira/GitHub issues.
    *   **State Management:** Local file-based state (`comply.yml`, `todo` lists) synchronized with remote systems.
*   **Relevance:**
    *   Demonstrates a "Markdown-as-Database" approach similar to Mentci's `core/` mandates.
    *   The "Ticketing Integration" logic is a strong reference for `mentci-aid`'s future task synchronization capabilities.

## 2. StrongDM / CXDB
*   **Source:** `/nix/store/1mh6xafsvphf6y999hh4w3z0dil6sk8y-cxdb`
*   **Type:** AI Context Store.
*   **Language:** Rust (Server), Go (Gateway/SDK), TypeScript (Frontend).
*   **Architecture:**
    *   **Turn DAG:** Immutable, directed acyclic graph of conversation turns.
    *   **Content-Addressed Storage (CAS):** BLAKE3 hashing for payload deduplication.
    *   **Type Registry:** Forward-compatible schema evolution.
*   **Relevance:**
    *   **High Impact:** This is architecturally very similar to Mentci's goals (Immutable DAG, CAS, Typed Schemas).
    *   **Assimilation Target:** CXDB's "Turn DAG" logic should be studied for `mentci-aid`'s session management.

## 3. ActiveAdmin (Bryan Helmkamp)
*   **Source:** `/nix/store/acl7459g852v2fdpy9r3wj5nlm87328s-activeadmin`
*   **Type:** Ruby on Rails Administration Framework.
*   **Language:** Ruby.
*   **Architecture:**
    *   **DSL-First:** Defines admin interfaces via a Ruby DSL.
    *   **Meta-Programming:** Heavily relies on Rails meta-programming to generate UIs.
*   **Relevance:**
    *   **Philosophy:** Bryan Helmkamp's work emphasizes *DSL design* for high-level abstraction. This aligns with `Aski` (Text-Native DSL).
    *   **Attractor Lineage:** `attractor` (Brynary) likely inherits this DSL-first philosophy, favoring readable specs over boilerplate code.

## 4. Synthesis
*   **Attractor's DNA:** A fusion of StrongDM's rigorous infrastructure/compliance engineering (Go/Rust) and Brynary's DSL-centric design (Ruby).
*   **Assimilation Strategy:**
    *   Adopt CXDB's **Immutable DAG** concepts for `mentci-aid`'s internal state.
    *   Adopt Comply's **Markdown-as-Truth** pattern for compliance/audit trails.
    *   Adopt ActiveAdmin's **DSL Power** for the `Aski` layer.

*The Great Work continues.*
