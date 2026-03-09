# CozoScript Examples — Agent Questions in a Saṃskāra Environment

These examples show how an agent request can be projected into CozoScript inside `samskarad`.

## 1) What are the currently accepted lane rules?
```cozoscript
?[lane, rule, value] :=
    *lane_policy{lane, rule, value}
:order lane, rule
```

## 2) Show selected logical components in VersionOne
```cozoscript
?[component, status] :=
    *statement{
      subj: component,
      pred: "status",
      obj: status,
      plane: "versionone",
      state: "accepted"
    },
    status = "selected"
```

## 3) Which statements came from LLM inference and are still unresolved?
```cozoscript
?[id, subj, pred, obj, confidence] :=
    *statement{
      id, subj, pred, obj,
      source: "llm-inference",
      state: "proposed",
      confidence
    }
:order -confidence
```

## 4) What authority does agent `dev` have right now?
```cozoscript
?[role] := *agent_role{agent: "dev", role}
```

## 5) Why was a lane rewrite denied?
```cozoscript
?[tx, reason, issuer, ts] :=
    *tx_log{tx, reason, issuer, ts},
    *statement{tx, pred: "decision", obj: "deny"}
:order -ts
```

## 6) Show all facts about entity `lane-governor`
```cozoscript
?[pred, obj, state, source] :=
    *statement{
      subj: "lane-governor",
      pred, obj, state, source
    }
:order pred
```

---

## 7) MVP Agent Message Substrate (Bootstrap CozoScript)

This extends query examples into a minimal **agent-to-agent message protocol**.
Use this for logical constraints and execution governance, while keeping human prose for narrative summaries.

### 7.1 Relations

```cozoscript
:create agent_msg {
  msg_id: String,
  thread_id: String,
  from_agent: String,
  to_agent: String,
  kind: String,
  status: String,
  ts: String
}

:create agent_constraint {
  msg_id: String,
  field: String,
  op: String,
  value: String
}

:create agent_evidence {
  msg_id: String,
  kind: String,
  ref: String,
  snippet: String
}

:create agent_decision {
  msg_id: String,
  decision: String,
  reason: String
}
```

MVP `kind` values:
- `delegate`
- `review`
- `evidence`
- `blocked`
- `complete`

MVP constraint operators:
- `eq`, `neq`, `contains`, `not_contains`, `exists`

### 7.2 Delegation message (common logical-constraint case)

```cozoscript
:put agent_msg {
  ["msg-001", "th-001", "planner", "task", "delegate", "open", "5919.12.20.349"]
}

:put agent_constraint {
  ["msg-001", "targetBookmark", "eq", "MENTCI_TARGET_BOOKMARK"],
  ["msg-001", "jjRevset", "not_contains", "all()"],
  ["msg-001", "verification", "contains", "cargo test -p mentci-cozo"]
}
```

### 7.3 Reviewer issue message

```cozoscript
:put agent_msg {
  ["msg-002", "th-001", "reviewer", "task", "review", "changes-requested", "5919.12.20.349"]
}

:put agent_decision {
  ["msg-002", "request_changes", "missing bounded workspace-diagnostics evidence"]
}
```

### 7.4 Evidence packet message

```cozoscript
:put agent_msg {
  ["msg-003", "th-001", "task", "planner", "evidence", "ok", "5919.12.20.349"]
}

:put agent_evidence {
  ["msg-003", "test", "cargo test -p mentci-cozo", "2 passed, 0 failed"],
  ["msg-003", "lsp", "workspace-diagnostics", "0 errors, 0 warnings"]
}
```

### 7.5 Blocked status message

```cozoscript
:put agent_msg {
  ["msg-004", "th-001", "task", "planner", "blocked", "blocked", "5919.12.20.349"]
}

:put agent_decision {
  ["msg-004", "blocked", "No API key found for provider lane"]
}
```

### 7.6 Completion message

```cozoscript
:put agent_msg {
  ["msg-005", "th-001", "task", "planner", "complete", "done", "5919.12.20.349"]
}

:put agent_decision {
  ["msg-005", "accept", "MVP constraints satisfied and verified"]
}
```

### 7.7 Query: open delegated tasks with constraints

```cozoscript
?[msg_id, from_agent, to_agent, field, op, value] :=
  *agent_msg{msg_id, from_agent, to_agent, kind: "delegate", status: "open"},
  *agent_constraint{msg_id, field, op, value}
:order msg_id, field
```

## 8) Message-First Intent Contract (UI-visible first)

This shape mirrors the markdown-style sections users and agents read first.
Technical protocol facts can be attached later without requiring immediate UI exposure.

### 8.1 Relations

```cozoscript
:create message_goal {
  message_id: String,
  goal: String
}

:create message_restraint {
  message_id: String,
  restraint: String
}

:create message_scope {
  message_id: String,
  path: String
}

:create message_out_of_scope {
  message_id: String,
  item: String
}

:create message_verify {
  message_id: String,
  cmd: String
}
```

### 8.2 Complete message-first example

```cozoscript
:put message_goal {
  ["msg-ui-001", "stabilize parallel subagent orchestration"],
  ["msg-ui-001", "preserve deterministic evidence packets"]
}

:put message_restraint {
  ["msg-ui-001", "avoid unbounded jj revsets such as all()"],
  ["msg-ui-001", "do not mutate extension runtime internals during investigation"]
}

:put message_scope {
  ["msg-ui-001", "/home/li/git/Mentci-AI--dev/.pi/skills/subagent-driven-development/SKILL.md"]
}

:put message_out_of_scope {
  ["msg-ui-001", "provider API key rotation"]
}

:put message_verify {
  ["msg-ui-001", "cargo test -p mentci-cozo"]
}
```

### 8.3 Deferred technical attachment

Technical constraints can be attached later through `agent_constraint` or `handoff_constraint`
when machine-checkable enforcement is needed, without requiring those low-level fields to be
visible in the primary UI markdown view.
