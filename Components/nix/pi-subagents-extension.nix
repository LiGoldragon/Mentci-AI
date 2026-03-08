{ pkgs }:

pkgs.stdenvNoCC.mkDerivation {
  pname = "pi-subagents-extension";
  version = "1.3.3710";

  src = pkgs.fetchurl {
    url = "https://registry.npmjs.org/@oh-my-pi/subagents/-/subagents-1.3.3710.tgz";
    hash = "sha256-h8U37eSRnTn/eXFg2NpkAs64u4eObwdrJeqTbwUxyaE=";
  };

  nativeBuildInputs = [ pkgs.gnutar ];
  dontConfigure = true;
  dontBuild = true;

  installPhase = ''
    runHook preInstall
    mkdir -p "$out"
    tar -xzf "$src" --strip-components=1 -C "$out"
    runHook postInstall
  '';
}
