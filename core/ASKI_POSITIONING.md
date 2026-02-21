# Aski Positioning and Homoiconicity

This document defines the canonical positioning of Aski in Mentci-AI.

## 1. Aski Exists for Text LLM Efficiency

Aski is the most efficient text-native representation of Sema objects for text-based LLM systems.
Its primary role is to improve machine comprehension quality, consistency, and token economy when reasoning in textual channels.

## 2. Mentci Uses Aski Lightly

Mentci does not center its long-term UX on text-only DSL interaction.
Mentci reuses Aski where text interfaces are beneficial, while moving primary operator interaction toward direct visual and symbolic interfaces.

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
