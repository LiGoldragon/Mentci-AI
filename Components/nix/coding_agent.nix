{ pkgs, src }:

pkgs.stdenv.mkDerivation {
  pname = "coding-agent";
  version = "unstable";
  src = src;

  dontBuild = true;

  installPhase = ''
    runHook preInstall
    mkdir -p $out
    cp -R packages/coding-agent/. $out
    runHook postInstall
  '';
}
