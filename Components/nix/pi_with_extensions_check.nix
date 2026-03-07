{ pkgs, pi_with_extensions }:

pkgs.runCommand "pi-with-extensions-check" { } ''
  test -x ${pi_with_extensions}/bin/pi
  test -f ${pi_with_extensions}/lib/node_modules/pi/node_modules/@mariozechner/pi-ai/bedrock-provider.js
  test -f ${pi_with_extensions}/lib/node_modules/pi/node_modules/@mariozechner/pi-ai/bedrock-provider.d.ts
  test -f ${pi_with_extensions}/lib/node_modules/pi/node_modules/@mariozechner/pi-ai/oauth.js
  test -f ${pi_with_extensions}/lib/node_modules/pi/node_modules/@mariozechner/pi-ai/oauth.d.ts
  test -e ${pi_with_extensions}/lib/node_modules/pi/node_modules/pi-mcp-adapter/index.ts
  grep -q "node_modules/pi-mcp-adapter" ${pi_with_extensions}/bin/pi
  grep -q "jcodemunch-mcp" ${pi_with_extensions}/bin/pi
  touch "$out"
''
