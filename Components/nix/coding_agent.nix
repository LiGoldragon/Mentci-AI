{ pkgs, src }:

pkgs.stdenv.mkDerivation {
  pname = "coding-agent";
  version = "unstable";
  src = src;

  nativeBuildInputs = [ pkgs.bun ];

  dontBuild = true;

  installPhase = ''
    runHook preInstall
    cd packages/coding-agent
    mkdir -p $out
    cp -R . $out
    cd $out
    bun install --production
    runHook postInstall
  '';
}
