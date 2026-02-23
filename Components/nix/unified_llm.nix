{ pkgs, src }:

pkgs.stdenv.mkDerivation {
  pname = "unified-llm";
  version = "unstable";
  src = src;

  dontBuild = true;

  installPhase = ''
    runHook preInstall
    mkdir -p $out
    cp -R packages/ai/. $out
    runHook postInstall
  '';
}
