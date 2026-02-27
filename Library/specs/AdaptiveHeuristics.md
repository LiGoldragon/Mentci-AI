# Adaptive Heuristics Specification (Tier 3)

## Purpose
This specification defines the schema for Tier 3 Adaptive Heuristics, which are dynamically generated EDN files (`runtime.edn`) maintained by the AI agent through the `update_skill_heuristics` MCP tool. 
These files store learned contextual knowledge and structural workarounds, ensuring they survive `/compact` operations and are folded into the "Absolute Rules" digest (Tier 2).

## File Location
`.pi/skills/<skill_name>/runtime.edn`

## EDN Schema Structure
The `runtime.edn` file is an appending list or a map of learned rules. To simplify parsing and appending, it represents a vector of heuristic maps:

```clojure
[
  {:id "UUID-or-timestamp"
   :condition "Description of the edge case or boundary encountered"
   :resolution "The actionable workaround or rule to apply"
   :context "Additional context on why this rule exists"}
]
```

## Example `runtime.edn`
```clojure
[
  {:id "591912100618"
   :condition "structural_edit fails on Rust module macros like include!"
   :resolution "Use precise text replacement to insert attributes like #[allow(...)] before the macro."
   :context "ast-grep struggles to match macro expansions inside module declarations"}
]
```

## Protocol Rules
1. **Append-Only:** The `update_skill_heuristics` tool appends a new map to the vector (or creates the vector if the file does not exist).
2. **Promotion:** When the Policy Engine (Tier 2 Compiler) runs, it reads this vector, synthesizes the "Absolute Rules", and optionally clears or archives the processed entries.
