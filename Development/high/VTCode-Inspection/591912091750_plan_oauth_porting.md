# Plan: Porting OAuth Flows to Generic VTCode Auth Framework

- **Solar:** 5919.12.9.17.50
- **Subject:** `VTCode-Inspection`
- **Status:** `planning`
- **Weight:** `High`

## 1. Objective
Refactor VTCode's authentication system into a provider-independent framework and port specific OAuth flows (Antigravity, Gemini, ChatGPT, Claude Code).

## 2. Dependencies
- **Patched VTCode:** `github:LiGoldragon/vtcode/mentci-gemini-json-fix`
- **Mentci-User:** Handling local secrets for Client IDs/Secrets.

## 3. Tasks

### Task 1: Core Framework Refactoring
- [ ] Implement `OAuthProvider` trait in `vtcode-core/src/auth/traits.rs`.
- [ ] Refactor `vtcode-config` to support multi-provider tokens (`Map<String, OAuthToken>`).
- [ ] Migrate `openrouter_oauth.rs` to the new trait.

### Task 2: Gemini & Antigravity Porting
- [ ] **Discovery Logic:** Port `loadCodeAssist` logic from `pi-ai` to Rust.
- [ ] **Antigravity Flow:** Implement Google Cloud-specific scopes and project ID extraction.
- [ ] **Gemini Flow:** Separate standard Google API Key/OAuth from the Antigravity "Pro" flow.

### Task 3: ChatGPT & Claude Code Porting
- [ ] **ChatGPT OAuth:** Research and implement the flow used by `chatgpt-cli`.
- [ ] **Claude Code:** Implement the Anthropic-native OAuth flow (if available/documented).

### Task 4: UI Integration
- [ ] Update `/login` command to accept `<provider>` argument.
- [ ] Implement dynamic callback server port selection.

## 4. Checkpoints
- **[ ] CP1:** `vtcode-core` builds with generic auth traits.
- **[ ] CP2:** OpenRouter OAuth remains functional on the new framework.
- **[ ] CP3:** `/login gemini` successfully discovers project ID and enables Antigravity models.

## 5. Next Step
Start Refactoring `vtcode-core/src/auth/` to introduce the provider trait.
