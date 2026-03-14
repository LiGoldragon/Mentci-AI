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
  grep -q "node_modules/lsp-pi" ${pi_with_extensions}/bin/pi
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
    "selfhosted-local": {
      "api": "openai-completions",
      "authRequired": false,
      "baseUrl": "http://127.0.0.1:11435/v1",
      "models": [
        {
          "id": "main",
          "name": "selfhosted-main"
        }
      ]
    }
  }
}
EOF

  cat > "$TMPDIR/check-authless-provider.mjs" <<'EOF'
import { AuthStorage } from "file://${pi_with_extensions}/lib/node_modules/pi/dist/core/auth-storage.js";
import { ModelRegistry } from "file://${pi_with_extensions}/lib/node_modules/pi/dist/core/model-registry.js";

const authStorage = AuthStorage.inMemory({});
const registry = new ModelRegistry(authStorage, process.env.HOME + "/.pi/agent/models.json");
const available = registry.getAvailable();
const visible = available.some((model) => model.provider === "selfhosted-local" && model.id === "main");
if (!visible) {
  throw new Error("authless local provider model missing from getAvailable(): " + JSON.stringify(available));
}
EOF

  ${pkgs.nodejs}/bin/node "$TMPDIR/check-authless-provider.mjs"
  touch "$out"
''
