# Session Report

- Kind: `strategy`
- Subject: `Component-Dependency-Flow`
- Title: `component-dependency-flow`

## Prompt
strategy for components to dynamically receive the location of their required external components (which live in other Componenets/ directories) through a structured data flow wich a centrally-controlled data source which also lives in Components/ (obviously!)

## Outcome
Created a dedicated strategy for centrally managed component-location resolution:
- Canonical registry in `Components/registry/component_locations.edn`
- Validation + normalization entrypoint (`execute` proposed)
- Structured runtime injection (`jailConfig` + env/path)
- Consumer-side ID-based resolution APIs (Clojure + Rust)
- Migration phases, enforcement rules, and acceptance criteria
