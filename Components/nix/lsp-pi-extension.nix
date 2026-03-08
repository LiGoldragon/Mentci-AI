{ pkgs }:

pkgs.buildNpmPackage {
  pname = "lsp-pi-extension";
  version = "1.0.3-mentci.1";

  src = pkgs.fetchFromGitHub {
    owner = "LiGoldragon";
    repo = "lsp-pi";
    rev = "b8b9982f5a3ebab003b4231647097efe95f6eca3";
    hash = "sha256-+P6XVoWcfFZb/oPSpNarBKrOUlWjFQW9YtJ+QKbloBE=";
  };

  npmDepsHash = "sha256-TDx8/utCoX1yJmR+58DL/0bBJgjtMUcEmQCM8Wd2anE=";

  dontNpmBuild = true;

  installPhase = ''
    runHook preInstall
    mkdir -p "$out"
    cp -a ./. "$out/"
    runHook postInstall
  '';
}
