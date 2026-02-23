{ pkgs, src }:

pkgs.stdenv.mkDerivation {
  pname = "coding-agent";
  version = "unstable";
  src = src;

  nativeBuildInputs = [ pkgs.bun ];

  sourceRoot = "source/packages/coding-agent";

  # Don't try to build anything by default
  dontBuild = true;

  installPhase = ''
    runHook preInstall
    mkdir -p $out
    cp -R . $out
    # Run bun install inside the destination
    cd $out
    bun install --production
    runHook postInstall
  '';
}
