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
