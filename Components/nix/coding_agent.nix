{ pkgs, src }:

pkgs.buildNpmPackage {
  pname = "pi";
  version = "0.54.2";
  src = src;

  npmDepsHash = "sha256-nKDwNXZpqe1zSkkr1pVi9V2u1fq/bQivZ6LgFPewoDA=";

  npmBuildScript = "build";
  npmWorkspace = "packages/coding-agent";

  nativeBuildInputs = [ pkgs.pkg-config pkgs.makeWrapper ];
  buildInputs = [ pkgs.vips pkgs.pixman pkgs.cairo pkgs.pango pkgs.libjpeg pkgs.giflib pkgs.librsvg ];

  installPhase = ''
    runHook preInstall
    mkdir -p $out/bin $out/lib/node_modules/pi
    cp -r packages/coding-agent/dist $out/lib/node_modules/pi/
    cp -r packages/coding-agent/package.json $out/lib/node_modules/pi/
    cp -r node_modules $out/lib/node_modules/pi/
    find $out -type l -xtype l -delete

    makeWrapper ${pkgs.nodejs}/bin/node $out/bin/pi \
      --add-flags "$out/lib/node_modules/pi/dist/cli.js" \
      --set NODE_PATH "$out/lib/node_modules/pi/node_modules"
    runHook postInstall
  '';
}
