{ pkgs }:

let
  linkupSrc = pkgs.fetchurl {
    url = "https://registry.npmjs.org/@aliou/pi-linkup/-/pi-linkup-0.7.4.tgz";
    hash = "sha256-lPxk3YJ7Wgn3gOgWusWsh/8u3DieJgkr3zFwAdNFAB4=";
  };

  settingsSrc = pkgs.fetchurl {
    url = "https://registry.npmjs.org/@aliou/pi-utils-settings/-/pi-utils-settings-0.10.0.tgz";
    hash = "sha256-OGoMCqTMkqKvwg2BJbUVTc4jDCtmZRP5iZnJUPkD10A=";
  };

  settingsPkg = pkgs.stdenvNoCC.mkDerivation {
    pname = "pi-utils-settings";
    version = "0.10.0";
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
  version = "0.7.4";
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
