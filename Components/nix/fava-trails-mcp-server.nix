{ pkgs, fava_trails }:
pkgs.writeShellScriptBin "fava-trails-mcp-server" ''
  export PYTHONPATH="${fava_trails}/${pkgs.python3.sitePackages}"
  ${fava_trails}/bin/python -m fava_trails.server
''
