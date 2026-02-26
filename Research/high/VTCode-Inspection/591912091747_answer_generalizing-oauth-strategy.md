# VT Code: Generalizing OAuth for Gemini Antigravity and Other Providers

- **Solar:** 5919.12.9.17.47
- **Subject:** `VTCode-Inspection`
- **Status:** `design-draft`
- **Weight:** `High`

## 1. Intent
To design a provider-independent OAuth framework for `vtcode`, enabling support for Gemini Antigravity (Google Cloud-based OAuth) and future-proofing for other providers like OpenAI or Microsoft Azure.

## 2. Current State Analysis
VT Code has a functional OAuth implementation (PKCE-based) but it is tightly coupled to OpenRouter:
- **Component:** `vtcode-config/src/auth/openrouter_oauth.rs`
- **Limitation:** Hardcoded URLs, fixed token structures, and no support for provider-specific discovery phases (required by Google).

## 3. The Gemini Antigravity Requirement
Based on reverse-engineering the `pi-ai` implementation, a compliant Gemini Antigravity flow requires:
1. **Specific Scopes:** `cloud-platform`, `userinfo.email`, `cclog`, `experimentsandconfigs`.
2. **Project Discovery:** After obtaining an access token, the agent MUST call `https://cloudcode-pa.googleapis.com/v1internal:loadCodeAssist` to discover the associated Google Cloud `projectId`.
3. **Token Usage:** The `projectId` must be sent alongside the `access_token` in subsequent LLM requests.

## 4. Proposed Architecture: `GenericOAuthProvider`

To support this, VT Code should transition to a trait-based auth model.

### 4.1 `OAuthProvider` Trait
Defined in `vtcode-core/src/auth/traits.rs`:
```rust
#[async_trait]
pub trait OAuthProvider: Send + Sync {
    fn name(&self) -> &str;
    fn auth_url(&self, pkce: &PkceChallenge) -> String;
    fn token_url(&self) -> &str;
    fn scopes(&self) -> Vec<String>;
    
    async fn exchange_code(&self, code: &str, verifier: &str) -> Result<OAuthToken>;
    async fn refresh_token(&self, refresh_token: &str) -> Result<OAuthToken>;
    
    // Hook for provider-specific discovery (e.g. Google Project ID)
    async fn post_auth_discovery(&self, token: &mut OAuthToken) -> Result<()> {
        Ok(())
    }
}
```

### 4.2 Generic `OAuthToken`
Replace `OpenRouterToken` with a flexible structure in `vtcode-config`:
```rust
pub struct OAuthToken {
    pub access_token: String,
    pub refresh_token: Option<String>,
    pub expires_at: Option<u64>,
    pub provider: String,
    pub metadata: HashMap<String, String>, // Stores "projectId", etc.
}
```

## 5. Provider Implementations

### 5.1 `GeminiAntigravityProvider`
- **Auth URL:** Google Accounts OAuth2 v2.
- **Discovery:** Implements `post_auth_discovery` to run the `loadCodeAssist` call and store `projectId` in metadata.
- **LLM Integration:** Updates `GeminiProvider` to read `projectId` from the token metadata and include it in the `X-Goog-User-Project` header or request body.

### 5.2 `OpenRouterOauthProvider`
- Moves current `openrouter_oauth.rs` logic into the new trait implementation.

### 5.3 `OpenAIOauthProvider` (Future)
- Would support OpenAI's "Connect" flow or Azure AD authentication for enterprise users.

## 6. Implementation Strategy
1. **Refactor Storage:** Update `vtcode-config` to store a `Map<String, OAuthToken>` instead of a single optional token.
2. **Core Trait:** Implement the `OAuthProvider` trait in `vtcode-core`.
3. **Provider Registry:** Create a registry that returns the correct `OAuthProvider` based on the agent configuration.
4. **TUI Command:** Update `/login <provider>` to lookup the provider in the registry and launch the generic `run_oauth_callback_server`.

## 7. Difficulty Assessment
- **Core Refactoring:** Low (Rename/Move).
- **Gemini Discovery Logic:** Moderate (Requires new HTTP client calls).
- **LLM Provider Updates:** Moderate (Header injection logic).
- **Total Work:** ~15-20 hours of Rust implementation.

## 8. Why hasn't VT Code done this?
The focus has been on "Semantic Correctness" and standard API keys which cover 90% of user needs. Advanced OAuth flows like Antigravity are niche and require maintaining specific Client IDs and Project IDs that are often tied to specific distributions (like `pi`).
