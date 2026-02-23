# Aski Positioning and Homoiconicity

This document defines the canonical positioning of Aski in Mentci-AI.

## 1. Aski Exists for Text LLM Efficiency

Aski is the most efficient text-native representation of Sema objects for text-based LLM systems.
Its primary role is to improve machine comprehension quality, consistency, and token economy when reasoning in textual channels.

### 1.1 The Genius of Lisp (Whitespace Independence)
Aski adopts the Lisp-like advantage: it does not rely on newlines or indentation for semantic parsing (unlike Python or Haskell). This minimizes context space usage and increases machine-comprehension density.

### 1.2 Keyword as Object
The Aski reader recognizes keywords directly as objects. This allows the system to transpile high-level symbolic intent directly into efficient machine structures (Rust/Sema) without intermediate descriptive layers.

## 2. Mentci Uses Aski Lightly

Mentci does not center its long-term UX on text-only DSL interaction.
Mentci reuses Aski where text interfaces are beneficial, while moving primary operator interaction toward direct visual and symbolic interfaces.

### 2.1 The Binary Horizon (SEMA)
While Aski optimizes the text channel, the ultimate efficiency lies in **SEMA binary data**. By moving from text-based Aski to fully specified structured trees of enumerators, LLM cognition becomes two to three orders of magnitude more efficient than text.

## 3. Homoiconicity Contract

Everything in the system is structured data.
Text, DOT, EDN, JSON, Cap'n Proto, and UI views are projections of the same semantic structure.

Required consequence:
- Any meaningful artifact must have a structured-data representation.
- Text formats are not authority by default; schema-backed structure is authority.

## 4. Documentation Interpretation Rule

All Mentci-AI documentation should be interpreted through this model:
- Aski is an optimization layer for text-based cognition.
- Structured data is the primary semantic layer.
- Visual/symbolic interfaces are the primary long-term interaction direction for Mentci.
