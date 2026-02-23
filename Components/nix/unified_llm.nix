{ pkgs, src }:

pkgs.stdenv.mkDerivation {
  pname = "unified-llm";
  version = "unstable";
  src = src;

  nativeBuildInputs = [ pkgs.bun ];

  dontBuild = true;

  installPhase = ''
    runHook preInstall
    cd packages/unified-llm
    mkdir -p $out
    cp -R . $out
    cd $out
    bun install --production
    runHook postInstall
  '';
}
