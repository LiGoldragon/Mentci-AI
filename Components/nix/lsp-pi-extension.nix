{ pkgs }:

pkgs.buildNpmPackage {
  pname = "lsp-pi-extension";
  version = "1.0.3";

  src = pkgs.fetchurl {
    url = "https://registry.npmjs.org/lsp-pi/-/lsp-pi-1.0.3.tgz";
    hash = "sha256-hJIF5hjO7/FNkCXEKuI56W59V86oxQp+Wm8myUkOi5I=";
  };

  npmDepsHash = "sha256-i/qd5QYj/afHCOvwV0MjI/yNnXs98YxDYCX2Oh+UMGY=";

  postPatch = ''
    cp ${./lsp-pi-package-lock.json} package-lock.json
  '';

  dontNpmBuild = true;

  installPhase = ''
    runHook preInstall
    mkdir -p "$out"
    cp -a ./. "$out/"
    runHook postInstall
  '';
}
