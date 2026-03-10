{ pkgs }:

pkgs.stdenvNoCC.mkDerivation {
  pname = "pi-subagents-extension";
  version = "1.3.3710-mentci.1";

  src = pkgs.fetchFromGitHub {
    owner = "LiGoldragon";
    repo = "oh-my-pi";
    rev = "37a731c85";
    hash = "sha256-39tg7hOQic+TCY1LZv69UBqI/QlC9+nTyN4EEvBWhRs=";
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
