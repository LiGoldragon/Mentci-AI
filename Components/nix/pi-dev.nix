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

    makeWrapper ${pkgs.nodejs}/bin/node $out/bin/pi \
      --add-flags "$out/lib/node_modules/pi/dist/cli.js" \
      --set NODE_PATH "$out/lib/node_modules/pi/node_modules" \
      --set-default PI_PACKAGE_DIR "$out/lib/node_modules/pi" \
      --set-default PI_AI_ANTIGRAVITY_VERSION "1.24.0"
    runHook postInstall
  '';
}
