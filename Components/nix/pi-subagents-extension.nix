{ pkgs }:

pkgs.stdenvNoCC.mkDerivation {
  pname = "pi-subagents-extension";
  version = "1.3.3710-mentci.1";

  src = pkgs.fetchFromGitHub {
    owner = "LiGoldragon";
    repo = "oh-my-pi";
    rev = "2ba8670cb";
    hash = "sha256-9J8tKD+vYX8dXEF0f7Iip9+Zm/HnBuJw1Jeelcov+s4=";
  };

  sourceRoot = "source/plugins/subagents";

  dontConfigure = true;
  dontBuild = true;

  installPhase = ''
    runHook preInstall
    mkdir -p "$out"
    cp -R ./. "$out"/
    chmod -R u+w "$out"
    runHook postInstall
  '';
}
