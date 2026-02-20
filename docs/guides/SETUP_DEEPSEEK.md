# Guide: Setting Up DeepSeek API for Advanced Reasoning

> **Canonical Aski framing:** Aski is a text-native optimization for LLM cognition; Mentci uses Aski lightly while moving toward visual/symbolic interfaces; everything is structured data (homoiconicity). Authority: `docs/architecture/ASKI_POSITIONING.md`.

This guide outlines the process for creating a DeepSeek account, purchasing API credits, and configuring the environment to use the most advanced reasoning models (e.g., DeepSeek-R1).

## 1. Account Creation
1.  **Visit the Portal**: Go to [platform.deepseek.com](https://platform.deepseek.com/).
2.  **Sign Up**: Register using your email or a supported OAuth provider (GitHub, Google).
3.  **Verification**: Complete any required email or phone verification to activate the account.

## 2. Purchasing Credits
DeepSeek operates on a **Prepaid (Pay-as-you-go)** model.
1.  **Navigate to Top-up**: Go to the **Billing** or **Top-up** section in the dashboard.
2.  **Choose Amount**: Select the amount of credits you wish to purchase (e.g., $5, $10, $50).
3.  **Payment Method**: Complete the transaction using a credit card or other supported payment methods.
4.  **Auto-topup**: It is recommended to enable auto-topup to prevent service interruption during long-running agent tasks.

## 3. Obtaining an API Key
1.  **API Keys Section**: Navigate to the **API Keys** tab on the sidebar.
2.  **Create Key**: Click "Create new API key".
3.  **Secure Storage**: Copy the key immediately. **DeepSeek will not show it again.**
4.  **Local Environment**: Add the key to your shell profile (e.g., `~/.bashrc` or `~/.zshrc`):
    ```bash
    export DEEPSEEK_API_KEY="your_key_here"
    ```

## 4. Selecting the Advanced Model
To utilize the Level 5 reasoning and architectural capabilities required for Mentci-AI, choose between the Reasoner or the latest V4 flagship.

| Model Name | API ID | Best For |
| :--- | :--- | :--- |
| **DeepSeek-V4** | `deepseek-v4` | **Most advanced general/coding model. 1M+ context, repo-level analysis, extreme efficiency.** |
| **DeepSeek-R1** | `deepseek-reasoner` | **Complex logic, deep chain-of-thought, mathematical proofs.** |
| **DeepSeek-V3** | `deepseek-chat` | Routine tasks, fast boilerplate. |

### Why DeepSeek-V4?
V4 introduces **Engram Conditional Memory** and **Tiered KV Cache**, allowing it to reason across entire repositories (1M+ tokens) with 40% less memory usage. It is the definitive model for Level 5 "Dark Factory" implementation.

## 5. Implementation (OpenAI Compatibility)
DeepSeek is 100% compatible with the OpenAI API format.

### Example: Python
```python
from openai import OpenAI

client = OpenAI(
    api_key=os.environ.get("DEEPSEEK_API_KEY"),
    base_url="https://api.deepseek.com"
)

# Using V4 for repository-level reasoning
response = client.chat.completions.create(
    model="deepseek-v4",
    messages=[{"role": "user", "content": "Trace all dead code in this workspace..."}]
)
```

### Example: cURL
```bash
curl https://api.deepseek.com/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $DEEPSEEK_API_KEY" \
  -d 
    {
        "model": "deepseek-reasoner",
        "messages": [{"role": "user", "content": "Explain Level 5 AI."}]
      }
```

## 6. Optimization & Cost Control
- **Context Caching**: DeepSeek automatically caches common prefixes. Using a consistent system prompt across calls can reduce costs by up to 74%.
- **Reasoning Tokens**: `deepseek-reasoner` outputs "thinking" tokens. These are billed, but essential for Level 5 accuracy.
- **Max Tokens**: Always set a `max_tokens` limit in your agent configuration to prevent runaway costs on recursive loops.

---
*Mentci-AI Level 5 Infrastructure | ♓︎ 1° 28' 44" | 5919 AM (Feb 19, 2026)*
