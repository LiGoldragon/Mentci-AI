{ pkgs, src }:

pkgs.buildNpmPackage {
  pname = "pi";
  version = "0.54.2";
  src = src;

  npmDepsHash = "sha256-nKDwNXZpqe1zSkkr1pVi9V2u1fq/bQivZ6LgFPewoDA=";

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

    makeWrapper ${pkgs.nodejs}/bin/node $out/bin/pi \
      --add-flags "$out/lib/node_modules/pi/dist/cli.js" \
      --set NODE_PATH "$out/lib/node_modules/pi/node_modules" \
      --set-default PI_PACKAGE_DIR "$out/lib/node_modules/pi" \
      --set-default PI_AI_ANTIGRAVITY_VERSION "1.23.0"
    runHook postInstall
  '';
}
