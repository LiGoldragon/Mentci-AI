{ pkgs, src }:

pkgs.stdenv.mkDerivation {
  pname = "unified-llm";
  version = "unstable";
  src = src;

  nativeBuildInputs = [ pkgs.bun ];

  sourceRoot = "packages/unified-llm";

  dontBuild = true;

  installPhase = ''
    runHook preInstall
    mkdir -p $out
    cp -R . $out
    cd $out
    bun install --production
    runHook postInstall
  '';
}
