{ pkgs }:

pkgs.buildNpmPackage {
  pname = "pi-mcp-adapter-extension";
  version = "2.1.2";

  src = pkgs.fetchurl {
    url = "https://registry.npmjs.org/pi-mcp-adapter/-/pi-mcp-adapter-2.1.2.tgz";
    hash = "sha256-eXlrYNiHdOlqH9sAvWvU4T5106w8aSD0I0mTsAHZGGg=";
  };

  npmDepsHash = "sha256-uaGng/iZhs9W5GZSFAUMNiW2QdBSNwsb+tT6rF8BrY4=";

  postPatch = ''
    cp ${./pi-mcp-adapter-package-lock.json} package-lock.json
  '';

  dontNpmBuild = true;

  installPhase = ''
    runHook preInstall
    mkdir -p "$out"
    cp -a ./. "$out/"
    runHook postInstall
  '';
}
