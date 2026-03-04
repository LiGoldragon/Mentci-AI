# Agent Operational Init (VersionOne Representation)

This representation defines operational constants that agents must not hardcode in logic or guidance text.

Primary use-case: repository-local text often hardcodes values like `dev`, `main`, and `origin`. In VersionOne, these become data from a supervising repo/process through a single init input.

## Intent

- Make repo+lane operational policy parameterized.
- Keep agent behavior correct without embedding literal bookmark names in logic.
- Allow a separate authority repo to set runtime policy and pass it as one object.

## Canonical Entry

- **Canonical bootstrap filename:** `AI-init.cozo`
- AI CLI behavior:
  - `ai <file>`: use the file directly.
  - `ai <repo_dir>`: resolve `<repo_dir>/AI-init.cozo`.

## Object: `AgentOperationalIntent` (Cozo-first)

```cozo
:create session_identity {
  run_id: String,
  operator_id: String,
  launch_ts: String,
  authority_hash: String
}

:create agent_operational_intent {
  primary_bookmark: String,
  primary_remote: String,
  integration_bookmark: String,
  bookmark_moves_allowed_by: String,
  end_push_remote: String,
  end_push_bookmark: String,
  verify_local_revset: String,
  verify_remote_revset: String
}

:create agent_capability_profile {
  capability: String,
  allowed: Bool,
  notes: String
}

:put session_identity {
  ["run-001", "li", "5919.12.15", "b3:placeholder"]
}

:put agent_operational_intent {
  ["dev", "origin", "main", "user", "origin", "dev", "bookmarks(dev)", "remote_bookmarks(\"dev@origin\")"]
}

:put agent_capability_profile {
  ["bookmark_move", true, "allowed only on explicit operator direction"],
  ["cross_repo_mutation", false, "requires dedicated capability grant"]
}
```

## Transfer / Authority

- **Authority source:** a lane-governor repository (or equivalent).
- **Transport:** one init input object through `AI-init.cozo` (Cap'n Proto envelope can be added later).
- **Local behavior:** this object overrides repo-local prose when deciding:
  - which bookmark to base on,
  - which bookmark to push,
  - whether and how rebases are allowed.

## Current mentci-ai operational truth

- `primary_bookmark = "dev"`
- `bookmark_moves_allowed_by = "user"`
- no assumption that another tree moves `dev` implicitly.
