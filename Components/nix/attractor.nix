{ pkgs, src, coding_agent, unified_llm }:

pkgs.stdenv.mkDerivation {
  pname = "attractor";
  version = "0.1.0";
  inherit src;
  nativeBuildInputs = [ pkgs.bun pkgs.makeWrapper ];
  dontBuild = true;
  doCheck = true;
  checkPhase = ''
    runHook preCheck
    export HOME="$TMPDIR"
    mkdir -p attractor/node_modules
    ln -s ${coding_agent} attractor/node_modules/coding-agent
    ln -s ${unified_llm} attractor/node_modules/unified-llm
    cd attractor
    bun test $(find tests -name '*.test.ts' ! -path 'tests/integration/cross-feature-parity.test.ts' | sort)
    runHook postCheck
  '';
  installPhase = ''
    runHook preInstall
    mkdir -p $out/share/attractor $out/bin
    if [ -d attractor ]; then
      rm -rf attractor/node_modules
      cp -R attractor/. $out/share/attractor
    else
      rm -rf node_modules
      cp -R . $out/share/attractor
    fi
    makeWrapper ${pkgs.bun}/bin/bun $out/bin/attractor-server \
      --add-flags "run $out/share/attractor/bin/attractor-server.ts"
    runHook postInstall
  '';
}
