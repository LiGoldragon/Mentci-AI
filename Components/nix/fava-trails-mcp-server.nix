{ pkgs, fava_trails }:
pkgs.writeShellScriptBin "fava-trails-mcp-server" ''
  ${fava_trails}/bin/python -m fava_trails.server
''
