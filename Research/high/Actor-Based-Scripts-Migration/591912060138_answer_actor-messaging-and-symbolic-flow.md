# Research Artifact: Actor Messaging and Symbolic Flow

- **Solar:** ♓︎ 6° 1' 38" | 5919 AM
- **Subject:** `Actor-Based-Scripts-Migration`
- **Title:** `actor-messaging-and-symbolic-flow`
- **Status:** `proposed`

## 1. Intent
Define the communication protocol for the new Rust-based script actors in the `execute` component. The protocol must adhere to the "Single Object In/Out" rule and utilize Aski-compatible symbol mapping.

## 2. Message Schema
All actors will communicate using a unified **Symbolic Message** structure.

### 2.1 The Noun-Action Pattern
Messages are defined as Nouns (Objects) containing an Action (Enum).

```rust
struct ActorMessage<T> {
    id: MessageId,
    timestamp: SolarTimestamp,
    sender: ActorId,
    payload: T,
}

enum ScriptAction {
    Validate(RootGuardConfig),
    Finalize(SessionConfig),
    Unify(SubjectUnifierConfig),
    GetVersion(ProgramVersionConfig),
}
```

### 2.2 Serde-Aski Bridge
The `payload` will be serializable to/from Aski syntax using the `aski-lib` reader currently under development. This ensures that actor state and messages can be audited in human-readable text.

## 3. Actor Hierarchy (Russian Doll)
The `execute` orchestrator will follow a supervisor-worker pattern:

1.  **Orchestrator (Root Actor):** Ingests CLI args, resolves the target "script" actor.
2.  **Worker Actor (Leaf):** Executes the specific guard or tool logic.
3.  **Result Actor:** Collects the outcome and formats the final CLI response.

## 4. Obsolete Aski Integration
Earlier "Aski" concepts that relied on implicit whitespace or procedural macros are marked as obsolete. The new actor flow treats **Aski as a Data Substrate** where keywords are first-class objects in the actor's memory.

## 5. Next Implementation Slice
Develop the `MessageId` and `SolarTimestamp` objects in `mentci-box-lib` or a new `mentci-actors` shared component.
