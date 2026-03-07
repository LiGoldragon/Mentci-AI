{ pkgs, pi, pi_linkup_extension, pi_mcp_adapter_extension, jcodemunch_mcp }:

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
    ln -s "${pi_mcp_adapter_extension}" "$out/lib/node_modules/pi/node_modules/pi-mcp-adapter"

    mkdir -p "$out/bin"
    cat > "$out/bin/pi" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail

: "''${PI_PACKAGE_DIR:=__PI_PACKAGE_DIR__}"
export PI_PACKAGE_DIR

export NODE_PATH="''${PI_PACKAGE_DIR}/node_modules''${NODE_PATH:+:$NODE_PATH}"
export PATH="__JCODEMUNCH_BIN__:$PATH"

exec ${pkgs.nodejs}/bin/node "''${PI_PACKAGE_DIR}/dist/cli.js" \
  --extension "''${PI_PACKAGE_DIR}/node_modules/@aliou/pi-linkup" \
  --extension "''${PI_PACKAGE_DIR}/node_modules/pi-mcp-adapter" \
  "$@"
EOF
    substituteInPlace "$out/bin/pi" \
      --replace-fail "__PI_PACKAGE_DIR__" "$out/lib/node_modules/pi" \
      --replace-fail "__JCODEMUNCH_BIN__" "${jcodemunch_mcp}/bin"
    chmod +x "$out/bin/pi"

    runHook postInstall
  '';
}
