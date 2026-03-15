{ pkgs, pi_with_extensions }:

pkgs.runCommand "pi-with-extensions-check" { } ''
  test -x ${pi_with_extensions}/bin/pi
  test -f ${pi_with_extensions}/lib/node_modules/pi/node_modules/@mariozechner/pi-ai/bedrock-provider.js
  test -f ${pi_with_extensions}/lib/node_modules/pi/node_modules/@mariozechner/pi-ai/bedrock-provider.d.ts
  test -f ${pi_with_extensions}/lib/node_modules/pi/node_modules/@mariozechner/pi-ai/oauth.js
  test -f ${pi_with_extensions}/lib/node_modules/pi/node_modules/@mariozechner/pi-ai/oauth.d.ts
  test -e ${pi_with_extensions}/lib/node_modules/pi/node_modules/pi-mcp-adapter/index.ts
  test -e ${pi_with_extensions}/lib/node_modules/pi/node_modules/lsp-pi/src/lsp.ts
  test -e ${pi_with_extensions}/lib/node_modules/pi/node_modules/@oh-my-pi/subagents/tools/index.ts
  test -e ${pi_with_extensions}/lib/node_modules/pi/node_modules/pi-subagents-adapter/index.ts
  grep -q "node_modules/pi-mcp-adapter" ${pi_with_extensions}/bin/pi
  grep -q "node_modules/pi-subagents-adapter" ${pi_with_extensions}/bin/pi
  grep -q "\.nix" ${pi_with_extensions}/lib/node_modules/pi/node_modules/lsp-pi/src/lsp-core.ts
  grep -q "id: \"nixd\"" ${pi_with_extensions}/lib/node_modules/pi/node_modules/lsp-pi/src/lsp-core.ts
  grep -q "async waitForEventProcessing()" ${pi_with_extensions}/lib/node_modules/pi/dist/core/agent-session.js
  grep -q "await session.waitForEventProcessing();" ${pi_with_extensions}/lib/node_modules/pi/dist/modes/print-mode.js
  grep -q "authRequired: Type.Optional(Type.Boolean())" ${pi_with_extensions}/lib/node_modules/pi/dist/core/model-registry.js
  grep -q "authOptionalProviders = new Set()" ${pi_with_extensions}/lib/node_modules/pi/dist/core/model-registry.js
  grep -q "authStorage.hasAuth(m.provider) || this.authOptionalProviders.has(m.provider)" ${pi_with_extensions}/lib/node_modules/pi/dist/core/model-registry.js
  grep -q "authless local provider" ${pi_with_extensions}/lib/node_modules/pi/dist/modes/interactive/components/model-selector.js
  grep -q "completed without final text output" ${pi_with_extensions}/lib/node_modules/pi/node_modules/@oh-my-pi/subagents/tools/index.ts

  export NODE_PATH=${pi_with_extensions}/lib/node_modules/pi/node_modules
  export PI_PACKAGE_DIR=${pi_with_extensions}/lib/node_modules/pi
  export HOME=$TMPDIR/home
  mkdir -p "$HOME/.pi/agent"
  cat > "$HOME/.pi/agent/models.json" <<'EOF'
{
  "providers": {
    "prometheus": {
      "api": "openai-completions",
      "authRequired": false,
      "apiKey": "sk-no-key-required",
      "baseUrl": "http://[202:68bc:1221:1b13:5397:2a56:4aea:d4a9]:11434/v1",
      "models": [
        {
          "contextWindow": 8192,
          "id": "llama-3.2-1b-instruct",
          "maxTokens": 2048,
          "name": "prometheus/llama-3.2-1b-instruct (Llama 3.2 1B Instruct)",
          "reasoning": false
        },
        {
          "contextWindow": 196608,
          "id": "qwen3.5-35b-a3b",
          "maxTokens": 4096,
          "name": "prometheus/qwen3.5-35b-a3b (Qwen 3.5 35B A3B)",
          "reasoning": true
        },
        {
          "contextWindow": 131072,
          "id": "deepseek-r1-distill-llama-70b",
          "maxTokens": 16384,
          "name": "prometheus/deepseek-r1-distill-llama-70b (DeepSeek R1 Distill Llama 70B)",
          "reasoning": true
        }
      ]
    }
  }
}
EOF

  cat > "$TMPDIR/check-prometheus-provider.mjs" <<'EOF'
import { AuthStorage } from "file://${pi_with_extensions}/lib/node_modules/pi/dist/core/auth-storage.js";
import { ModelRegistry } from "file://${pi_with_extensions}/lib/node_modules/pi/dist/core/model-registry.js";

const authStorage = AuthStorage.inMemory({});
const registry = new ModelRegistry(authStorage, process.env.HOME + "/.pi/agent/models.json");
const available = registry.getAvailable();
const deepseek = available.some((model) => model.provider === "prometheus" && model.id === "deepseek-r1-distill-llama-70b");
if (!deepseek) {
  throw new Error("deepseek-r1-distill-llama-70b missing from getAvailable(): " + JSON.stringify(available));
}
EOF

  ${pkgs.nodejs}/bin/node "$TMPDIR/check-prometheus-provider.mjs"
  touch "$out"
''
