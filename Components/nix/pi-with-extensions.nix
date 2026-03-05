{ pkgs, pi, pi_linkup_extension }:

pkgs.stdenvNoCC.mkDerivation {
  pname = "pi-with-extensions";
  version = "upstream";
  dontUnpack = true;

  installPhase = ''
    runHook preInstall

    mkdir -p "$out/lib/node_modules/pi"
    cp -a "${pi}/lib/node_modules/pi/." "$out/lib/node_modules/pi/"
    chmod -R u+w "$out/lib/node_modules/pi"

    mkdir -p "$out/lib/node_modules/pi/node_modules/@aliou"
    ln -s "${pi_linkup_extension}" "$out/lib/node_modules/pi/node_modules/@aliou/pi-linkup"

    mkdir -p "$out/bin"
    cat > "$out/bin/pi" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail

: "''${PI_PACKAGE_DIR:=__PI_PACKAGE_DIR__}"
export PI_PACKAGE_DIR

export NODE_PATH="''${PI_PACKAGE_DIR}/node_modules''${NODE_PATH:+:$NODE_PATH}"

exec ${pkgs.nodejs}/bin/node "''${PI_PACKAGE_DIR}/dist/cli.js" \
  --extension "''${PI_PACKAGE_DIR}/node_modules/@aliou/pi-linkup" \
  "$@"
EOF
    substituteInPlace "$out/bin/pi" --replace-fail "__PI_PACKAGE_DIR__" "$out/lib/node_modules/pi"
    chmod +x "$out/bin/pi"

    runHook postInstall
  '';
}
