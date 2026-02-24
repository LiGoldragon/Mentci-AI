{ pkgs, src }:

pkgs.buildNpmPackage {
  pname = "pi-coding-agent";
  version = "0.54.2"; # Or retrieve from src/package.json if possible, but hardcoding for now or "unstable"
  src = src;

  npmDepsHash = "sha256-nKDwNXZpqe1zSkkr1pVi9V2u1fq/bQivZ6LgFPewoDA=";

  # The build script in root package.json builds all packages including coding-agent
  npmBuildScript = "build";

  postPatch = ''
    sed -i 's/keyof (typeof MODELS)\[TProvider\]/string/g' packages/ai/src/models.ts
    sed -i 's/^type ModelApi<TProvider extends KnownProvider, TModelId extends string> = .*$/type ModelApi<TProvider extends KnownProvider, TModelId extends string> = Api;/g' packages/ai/src/models.ts
    sed -i 's/Model<ModelApi<TProvider, string>>\[\]/any[]/g' packages/ai/src/models.ts
    sed -i 's/return models ? (Array.from(models.values()) as .*$/return models ? (Array.from(models.values()) as any[]) : [];/' packages/ai/src/models.ts
  '';

  nativeBuildInputs = [ pkgs.pkg-config ];
  buildInputs = [ pkgs.vips pkgs.pixman pkgs.cairo pkgs.pango pkgs.libjpeg pkgs.giflib pkgs.librsvg ];

  # Install phase: copy the built artifact
  installPhase = ''
    runHook preInstall
    mkdir -p $out/bin $out/lib/node_modules/pi-coding-agent
    cp -r packages/coding-agent/dist $out/lib/node_modules/pi-coding-agent/
    cp -r packages/coding-agent/package.json $out/lib/node_modules/pi-coding-agent/
    # Copy node_modules as well? buildNpmPackage handles node_modules in out/lib/node_modules usually
    
    # Symlink the binary
    ln -s $out/lib/node_modules/pi-coding-agent/dist/cli.js $out/bin/pi
    chmod +x $out/bin/pi
    runHook postInstall
  '';
}