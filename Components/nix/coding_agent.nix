{ pkgs, src }:

pkgs.stdenv.mkDerivation {
  pname = "coding-agent";
  version = "unstable";
  src = src;

  nativeBuildInputs = [ pkgs.bun ];

  dontBuild = true;

  installPhase = ''
    runHook preInstall
    echo "--- Listing files in coding_agent build ---"
    ls -R .
    echo "--- End listing ---"
    cd packages/coding-agent
    mkdir -p $out
    cp -R . $out
    cd $out
    bun install --production
    runHook postInstall
  '';
}
