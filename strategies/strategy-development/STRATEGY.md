# Strategy: Improving Strategy-Development

**Linked Goal:** `Goal 0: mentci-aid Stabilization` (Process Hardening)

## 1. Goal
To automate the tool discovery and packaging phases of the `STRATEGY_DEVELOPMENT.md` program.

## 2. Methodology
- **Automated Crawling:** Develop a `scripts/tool_discoverer.clj` that searches `nixpkgs`, `crates.io`, and `clojars` for keywords matching the strategy's subject.
- **Recursive Jails:** Use nested Nix Jails to test package isolation without polluting the project root.
- **Semantic Mapping:** Map the discovered tools back to the Attractor DOT nodes.

## 3. Implementation trials
- [ ] Trial 1: Use `scripts/prefetch_orchestrator.py` to automate the acquisition of a candidate tool for the `mentci-rfs` strategy.

*The Great Work continues.*
