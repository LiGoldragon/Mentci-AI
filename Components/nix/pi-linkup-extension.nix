{ pkgs }:

let
  linkupSrc = pkgs.fetchurl {
    url = "https://registry.npmjs.org/@aliou/pi-linkup/-/pi-linkup-0.7.3.tgz";
    hash = "sha256-SVm1CtRl+yiBIh+h30Wi5FkReB9tRvUwvvvBiROH0BA=";
  };

  settingsSrc = pkgs.fetchurl {
    url = "https://registry.npmjs.org/@aliou/pi-utils-settings/-/pi-utils-settings-0.4.0.tgz";
    hash = "sha256-M9bUg18waZD4PqDL10Ha8DL/nr90rrgz0RKcVrIMzBk=";
  };

  settingsPkg = pkgs.stdenvNoCC.mkDerivation {
    pname = "pi-utils-settings";
    version = "0.4.0";
    src = settingsSrc;
    nativeBuildInputs = [ pkgs.gnutar ];
    dontConfigure = true;
    dontBuild = true;
    installPhase = ''
      runHook preInstall
      mkdir -p "$out"
      tar -xzf "$src" --strip-components=1 -C "$out"
      runHook postInstall
    '';
  };
in
pkgs.stdenvNoCC.mkDerivation {
  pname = "pi-linkup-extension";
  version = "0.7.3";
  src = linkupSrc;
  nativeBuildInputs = [ pkgs.gnutar ];
  dontConfigure = true;
  dontBuild = true;
  installPhase = ''
    runHook preInstall
    mkdir -p "$out"
    tar -xzf "$src" --strip-components=1 -C "$out"

    mkdir -p "$out/node_modules/@aliou"
    ln -s "${settingsPkg}" "$out/node_modules/@aliou/pi-utils-settings"
    runHook postInstall
  '';
}
