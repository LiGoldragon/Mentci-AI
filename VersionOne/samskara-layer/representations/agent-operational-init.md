# Agent Operational Init (VersionOne Representation)

This representation defines *operational constants* that agents must not hardcode in logic or guidance text.

Primary use-case: the repository-local text currently hardcodes values like `dev`, `main`, `origin`, and the end-of-turn push target. In VersionOne, these are treated as **data** provided by a supervising repo/process (the lane governor / launcher) as part of an initialization envelope.

## Intent

- Make repo+lane operational policy **parameterized**.
- Keep docs and agent skills *structurally correct* without embedding literal bookmark names.
- Allow a different repository (governor) to set the operational policy and pass it to the agent in a single object.

## Object: `AgentOperationalIntent`

A minimal EDN form (Cap'n Proto equivalent should be introduced later):

```edn
{:version 1
 :message :AgentOperationalIntent

 ;; Where the agent should consider "authoritative head" for its work.
 :primaryBookmark "dev"

 ;; Where to push and where to look for remote-tracking revisions.
 :primaryRemote "origin"

 ;; Integration target for rebases / merge-forward operations.
 :integrationBookmark "main"

 ;; Who is permitted to move the primary bookmark.
 ;; Typical values: :user | :agent | :lane-governor
 :bookmarkMovesAllowedBy :user

 ;; Default end-of-turn push target.
 :endOfTurnPushTarget {:remote "origin" :bookmark "dev"}

 ;; Optional: a canonical verification revset to assert alignment.
 ;; (Keep as data; tooling may render it to `jj log` commands.)
 :alignmentVerification
 {:local "bookmarks(dev)"
  :remote "remote_bookmarks(dev@origin)"}}
```

## Transfer / Authority

- **Authority source:** a lane-governor repository (or equivalent) produces this object.
- **Transport:** delivered to the agent as part of a single init envelope (ideally Cap'n Proto; EDN acceptable during transition).
- **Local behavior:** the agent treats this object as higher authority than repo-local prose when deciding:
  - which bookmark to base work on,
  - which bookmark to advance/push,
  - when (and whether) rebases are permitted.

## Current repo operational truth (for `mentci-ai`, March 2026)

- `:primaryBookmark` is `"dev"`.
- `:bookmarkMovesAllowedBy` is `:user` (the agent should not assume another tree moves `dev`; `dev` is moved only when explicitly requested by the human operator).
