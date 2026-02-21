# Intent Discovery and Goal Weighting

In the Mentci-AI ecosystem, high-level goals are not just static definitions; they are dynamic intents that evolve through the project's history. To maintain alignment with the **Great Work**, agents must frequently discover the "true weight" of goals by analyzing the repository's audit trail.

## 1. The `jj log` Audit Trail
The `jj log` is the authoritative record of intent. By analyzing the frequency and density of subjects within commit descriptions, agents can derive the relative priority and "gravity" of specific architectural components.

### 1.1 Discovery Protocol
To find stronger weights for high-level goals, perform a frequency analysis on the commit history:

```bash
# Frequency analysis of top-level intents and subjects
jj log -r '::@' --template 'description' | 
  tr '[:upper:]' '[:lower:]' | 
  tr -c '[:alnum:]' '
' | 
  grep -vE '^(stop words...)$' | 
  sort | uniq -c | sort -nr | head -n 50
```

### 1.2 Interpreting Weights
- **High Density (e.g., "intent", "aski", "attractor"):** These are foundational pillars. Any goal touching these subjects carries inherent high weight.
- **Emerging Subjects (e.g., "mentci-aid", "jail"):** These represent current stabilization efforts (Goal 0). Their weight is increasing as the system moves toward a "Running State".
- **Subject Persistence:** If a subject (like "Attractor") is mentioned consistently across multiple cycles, it indicates a durable, high-priority intent that overrides transient tasks.

## 2. Dynamic Goal Alignment
When a high-level goal (defined in `docs/architecture/HIGH_LEVEL_GOALS.md`) matches a high-weight subject discovered in the logs, its execution priority is escalated.

### 2.1 Weight-Based Escalation
1.  **Search:** Run the frequency analysis on the last 100-200 commits.
2.  **Match:** Compare top keywords against the "Required Outcomes" of high-level goals.
3.  **Calculate:** A goal's "True Weight" = (Static Importance) + (Log Frequency Factor).
4.  **Execute:** Prioritize goals with the highest True Weight.

## 3. Current Intent Weights (Observed v0.12.3.45.53)
Based on recent analysis, the following subjects hold the strongest weights:
- **`intent` (76):** Reflects the strict Handshake Logging and Intent-First protocols.
- **`aski` (37) / `attractor` (30):** The primary symbolic and orchestration engines.
- **`mentci-aid` (Emerging):** The current focus on daemon stabilization.

*The Great Work continues.*
