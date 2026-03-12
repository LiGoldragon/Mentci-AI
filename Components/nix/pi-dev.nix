{ pkgs, src, lib }:

pkgs.buildNpmPackage {
  pname = "pi-dev";
  version = "0.57.1";
  src = src;

  npmDepsHash = lib.fakeHash;

  npmBuildScript = "build";

  postPatch = ''
    substituteInPlace packages/ai/src/models.ts \
      --replace 'TModelId extends keyof (typeof MODELS)[TProvider],' 'TModelId extends string,' \
      --replace '> = (typeof MODELS)[TProvider][TModelId] extends { api: infer TApi } ? (TApi extends Api ? TApi : never) : never;' '> = Api;' \
      --replace 'export function getModel<TProvider extends KnownProvider, TModelId extends keyof (typeof MODELS)[TProvider]>(' 'export function getModel<TProvider extends KnownProvider, TModelId extends string>(' \
      --replace '): Model<ModelApi<TProvider, TModelId>> {' '): Model<Api> {' \
      --replace 'return providerModels?.get(modelId as string) as Model<ModelApi<TProvider, TModelId>>;' 'return providerModels?.get(modelId as string) as Model<Api>;' \
      --replace '): Model<ModelApi<TProvider, keyof (typeof MODELS)[TProvider]>>[] {' '): Model<Api>[] {' \
      --replace 'return models ? (Array.from(models.values()) as Model<ModelApi<TProvider, keyof (typeof MODELS)[TProvider]>>[]) : [];' 'return models ? (Array.from(models.values()) as Model<Api>[]) : [];'

    substituteInPlace packages/ai/package.json \
      --replace '"build": "npm run generate-models && tsgo -p tsconfig.build.json"' '"build": "tsgo -p tsconfig.build.json"'

    substituteInPlace packages/ai/src/models.generated.ts \
      --replace '"gemini-3-pro-high"' '"gemini-3.1-pro-high"' \
      --replace '"gemini-3-pro-low"' '"gemini-3.1-pro-low"' \
      --replace 'Gemini 3 Pro High (Antigravity)' 'Gemini 3.1 Pro High (Antigravity)' \
      --replace 'Gemini 3 Pro Low (Antigravity)' 'Gemini 3.1 Pro Low (Antigravity)' \
      --replace '"google": {' '"google": { "gemini-3.1-pro-preview": { id: "gemini-3.1-pro-preview", name: "Gemini 3.1 Pro (Preview)", api: "google-generative-ai", provider: "google", baseUrl: "https://generativelanguage.googleapis.com/v1beta", reasoning: true, input: ["text", "image"], cost: { input: 0, output: 0, cacheRead: 0, cacheWrite: 0 }, contextWindow: 1048576, maxTokens: 65536 }, "gemini-3.1-flash": { id: "gemini-3.1-flash", name: "Gemini 3.1 Flash", api: "google-generative-ai", provider: "google", baseUrl: "https://generativelanguage.googleapis.com/v1beta", reasoning: true, input: ["text", "image"], cost: { input: 0, output: 0, cacheRead: 0, cacheWrite: 0 }, contextWindow: 1048576, maxTokens: 8192 }, "gemini-3.1-flash-lite": { id: "gemini-3.1-flash-lite", name: "Gemini 3.1 Flash-Lite", api: "google-generative-ai", provider: "google", baseUrl: "https://generativelanguage.googleapis.com/v1beta", reasoning: true, input: ["text", "image"], cost: { input: 0, output: 0, cacheRead: 0, cacheWrite: 0 }, contextWindow: 1048576, maxTokens: 8192 }, "gemini-3-flash-preview": { id: "gemini-3-flash-preview", name: "Gemini 3 Flash (Preview)", api: "google-generative-ai", provider: "google", baseUrl: "https://generativelanguage.googleapis.com/v1beta", reasoning: true, input: ["text", "image"], cost: { input: 0, output: 0, cacheRead: 0, cacheWrite: 0 }, contextWindow: 1000000, maxTokens: 8192 },'

    substituteInPlace packages/coding-agent/src/core/model-resolver.ts \
      --replace '"google-antigravity": "gemini-3-pro-high"' '"google-antigravity": "gemini-3.1-pro-high"'
  '';

  nativeBuildInputs = [ pkgs.pkg-config pkgs.makeWrapper ];
  buildInputs = [ pkgs.vips pkgs.pixman pkgs.cairo pkgs.pango pkgs.libjpeg pkgs.giflib pkgs.librsvg ];

  installPhase = ''
    runHook preInstall
    mkdir -p $out/bin $out/lib/node_modules/pi
    cp -r packages/coding-agent/dist $out/lib/node_modules/pi/
    cp -r packages/coding-agent/package.json $out/lib/node_modules/pi/
    cp -r node_modules $out/lib/node_modules/pi/

    mkdir -p $out/lib/node_modules/pi/node_modules/@mariozechner
    rm -rf $out/lib/node_modules/pi/node_modules/@mariozechner/pi-ai
    mkdir -p $out/lib/node_modules/pi/node_modules/@mariozechner/pi-ai
    cp -r packages/ai/dist $out/lib/node_modules/pi/node_modules/@mariozechner/pi-ai/
    cp -r packages/ai/dist/. $out/lib/node_modules/pi/node_modules/@mariozechner/pi-ai/
    cp packages/ai/package.json $out/lib/node_modules/pi/node_modules/@mariozechner/pi-ai/

    rm -rf $out/lib/node_modules/pi/node_modules/@mariozechner/pi-agent-core
    mkdir -p $out/lib/node_modules/pi/node_modules/@mariozechner/pi-agent-core
    cp -r packages/agent/dist $out/lib/node_modules/pi/node_modules/@mariozechner/pi-agent-core/
    cp packages/agent/package.json $out/lib/node_modules/pi/node_modules/@mariozechner/pi-agent-core/

    rm -rf $out/lib/node_modules/pi/node_modules/@mariozechner/pi-tui
    mkdir -p $out/lib/node_modules/pi/node_modules/@mariozechner/pi-tui
    cp -r packages/tui/dist $out/lib/node_modules/pi/node_modules/@mariozechner/pi-tui/
    cp packages/tui/package.json $out/lib/node_modules/pi/node_modules/@mariozechner/pi-tui/

    find $out -type l -xtype l -delete

    substituteInPlace $out/lib/node_modules/pi/dist/core/agent-session.js \
      --replace-fail '    get resourceLoader() {
        return this._resourceLoader;
    }
' '    get resourceLoader() {
        return this._resourceLoader;
    }
    /**
     * Wait for queued AgentSession event processing to finish.
     * Needed in print/json mode so final agent_end emission is not lost before process exit.
     */
    async waitForEventProcessing() {
        await this._agentEventQueue;
    }
'

    substituteInPlace $out/lib/node_modules/pi/dist/modes/print-mode.js \
      --replace-fail '    for (const message of messages) {
        await session.prompt(message);
    }
    // In text mode, output final response
' '    for (const message of messages) {
        await session.prompt(message);
    }
    if (typeof session.waitForEventProcessing === "function") {
        await session.waitForEventProcessing();
    }
    // In text mode, output final response
'

    substituteInPlace $out/lib/node_modules/pi/dist/core/model-registry.js \
      --replace-fail '    baseUrl: Type.Optional(Type.String({ minLength: 1 })),
    apiKey: Type.Optional(Type.String({ minLength: 1 })),
    api: Type.Optional(Type.String({ minLength: 1 })),
    headers: Type.Optional(Type.Record(Type.String(), Type.String())),
    authHeader: Type.Optional(Type.Boolean()),
    models: Type.Optional(Type.Array(ModelDefinitionSchema)),
    modelOverrides: Type.Optional(Type.Record(Type.String(), ModelOverrideSchema)),
});' '    baseUrl: Type.Optional(Type.String({ minLength: 1 })),
    apiKey: Type.Optional(Type.String()),
    api: Type.Optional(Type.String({ minLength: 1 })),
    headers: Type.Optional(Type.Record(Type.String(), Type.String())),
    authHeader: Type.Optional(Type.Boolean()),
    authRequired: Type.Optional(Type.Boolean()),
    models: Type.Optional(Type.Array(ModelDefinitionSchema)),
    modelOverrides: Type.Optional(Type.Record(Type.String(), ModelOverrideSchema)),
});' \
      --replace-fail '    customProviderApiKeys = new Map();
    registeredProviders = new Map();
    loadError = undefined;' '    customProviderApiKeys = new Map();
    authOptionalProviders = new Set();
    registeredProviders = new Map();
    loadError = undefined;' \
      --replace-fail '        this.authStorage.setFallbackResolver((provider) => {
            const keyConfig = this.customProviderApiKeys.get(provider);
            if (keyConfig) {
                return resolveConfigValue(keyConfig);
            }
            return undefined;
        });' '        this.authStorage.setFallbackResolver((provider) => {
            const keyConfig = this.customProviderApiKeys.get(provider);
            if (keyConfig !== undefined) {
                return resolveConfigValue(keyConfig);
            }
            return undefined;
        });' \
      --replace-fail '        this.customProviderApiKeys.clear();
        this.loadError = undefined;' '        this.customProviderApiKeys.clear();
        this.authOptionalProviders.clear();
        this.loadError = undefined;' \
      --replace-fail '                if (providerConfig.apiKey) {
                    this.customProviderApiKeys.set(providerName, providerConfig.apiKey);
                }' '                if (providerConfig.authRequired === false) {
                    this.authOptionalProviders.add(providerName);
                }
                if (providerConfig.apiKey) {
                    this.customProviderApiKeys.set(providerName, providerConfig.apiKey);
                }' \
      --replace-fail '                if (!providerConfig.apiKey) {
                    throw new Error(`Provider ''${providerName}: "apiKey" is required when defining custom models.`);
                }' '                if (providerConfig.authRequired !== false && !providerConfig.apiKey) {
                    throw new Error("Provider " + providerName + ": \"apiKey\" is required when defining custom models unless authRequired=false.");
                }' \
      --replace-fail '        return this.models.filter((m) => this.authStorage.hasAuth(m.provider));' '        return this.models.filter((m) => this.authStorage.hasAuth(m.provider) || this.authOptionalProviders.has(m.provider));' \
      --replace-fail '        if (config.apiKey) {
            this.customProviderApiKeys.set(providerName, config.apiKey);
        }' '        if (config.authRequired === false) {
            this.authOptionalProviders.add(providerName);
        }
        if (config.apiKey) {
            this.customProviderApiKeys.set(providerName, config.apiKey);
        }' \
      --replace-fail '            if (!config.apiKey && !config.oauth) {
                throw new Error(`Provider ''${providerName}: "apiKey" or "oauth" is required when defining models.`);
            }' '            if (config.authRequired !== false && !config.apiKey && !config.oauth) {
                throw new Error("Provider " + providerName + ": \"apiKey\" or \"oauth\" is required when defining models unless authRequired=false.");
            }'

    substituteInPlace $out/lib/node_modules/pi/dist/modes/interactive/components/model-selector.js \
      --replace-fail '            const hintText = "Only showing models with configured API keys (see README for details)";' '            const hintText = "Only showing models with configured access (API key, OAuth, or authless local provider)";'

    makeWrapper ${pkgs.nodejs}/bin/node $out/bin/pi \
      --add-flags "$out/lib/node_modules/pi/dist/cli.js" \
      --set NODE_PATH "$out/lib/node_modules/pi/node_modules" \
      --set-default PI_PACKAGE_DIR "$out/lib/node_modules/pi" \
      --set-default PI_AI_ANTIGRAVITY_VERSION "1.24.0"
    runHook postInstall
  '';
}
