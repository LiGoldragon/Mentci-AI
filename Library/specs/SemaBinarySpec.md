# SEMA Binary Format Specification

- **Status:** Research / Canonical Intent
- **Weight:** High (Author Directed)

## 1. Objective
Define a fully specified binary data format for high-efficiency symbolic manipulation and LLM cognition.

## 2. Format Characteristics
SEMA is a machine-native format designed to bypass the token inefficiencies of text.

### 2.1 Structured Trees
*   The primary data structure is a structured tree of enumerators and vectors.
*   It does not use string identifiers; symbolic meaning is resolved via specification.

### 2.2 Namespace Efficiency
*   Namespaces are kept as small as possible.
*   A single byte (covering 256 enumerators) is the default for most local semantic scopes.

### 2.3 LLM Cognition
*   Direct SEMA ingestion by LLMs is targeted to be **two to three orders of magnitude** more efficient than text-based reasoning.

## 3. Precursors
SEMA is the successor to `Sajban` ("language of knowledge").
