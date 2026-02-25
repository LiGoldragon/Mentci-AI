# Research Artifact: Test-Driven DevShell Transition

- Chronography: `12.8.59.00 | 5919 AM`
- Subject: `Mentci-Launch`
- Title: `test-driven-devshell-transition`
- Status: `implemented`

## 1. Intent
Implement a reliable way for the `pi` agent to transition to a new development shell (specifically a new `pi` session in a `foot` terminal) only after passing a suite of integrity tests. This avoids UI breakage caused by running interactive tools within subshells and ensures the repository remains in a stable state.

## 2. Implementation: `execute transition`
I have implemented the `transition` command within the `execute` utility (`Components/mentci-aid`).

### 2.1. Guard Pipeline
Before initiating the launch, the `TransitionActor` executes a mandatory check sequence:
1.  `root-guard`: Verifies filesystem contract compliance.
2.  `link-guard`: Verifies cryptographic and path reference integrity.
3.  `session-guard`: Verifies that the current session has been synthesized and committed.

### 2.2. Cap'n Proto Launch
If the guards pass, the utility:
1.  Generates a `MentciLaunchRequest` Cap'n Proto message.
2.  Sets the launch mode to `terminal` and the program to `foot`.
3.  Invokes `mentci-launch`, which uses `systemd-run --user --scope` to spawn the new terminal session cleanly.

## 3. Benefits
- **UI Integrity:** Spawning a dedicated terminal prevents the "interpreted as prompt" UI glitch observed when running `gemini` or `pi` recursively.
- **State Enforcement:** The transition is impossible if the repository has pending guard violations.
- **Systemd Integration:** Leveraging `systemd --user` ensures the new session is tracked by the host OS and has its own environment lifecycle.

## 4. Suggestions for the Author
1.  **Direct Tool Calling:** I should update the `pi` agent's system prompt to suggest `execute transition` when the user asks to "refresh" or "enter the new shell."
2.  **Visual Feedback:** We could add a simple notification (via `libnotify` if available) that the transition is occurring.

## 5. Questions for the Author
1.  **Test Suite Expansion:** Are there specific functional tests (beyond the guards) that should be added to the pre-transition checklist?
2.  **Default Terminal:** Should the terminal program be hardcoded to `foot` in the `TransitionActor`, or should it read from a local `.mentci/launch.json` configuration?
