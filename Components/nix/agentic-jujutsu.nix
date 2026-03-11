{ lib
, buildNpmPackage
, fetchurlPkg
, makeWrapper
, nodejs_22
}:

buildNpmPackage rec {
  pname = "agentic-jujutsu";
  version = "2.3.6";
  nodejs = nodejs_22;

  src = fetchurlPkg {
    url = "https://registry.npmjs.org/agentic-jujutsu/-/agentic-jujutsu-${version}.tgz";
    hash = "sha256-4dBobHx8fwmzv/laKG/AWEUVVWnjGqAb4GC91oLkExo=";
  };

  npmDepsHash = "sha256-XyF/AxaWJjePMkDST7RFUkI/9NN45vD1mWzQDuOVYlA=";

  postPatch = ''
    cp ${./agentic-jujutsu-package-lock.json} package-lock.json
  '';

  dontNpmBuild = true;

  nativeBuildInputs = [ makeWrapper ];

  installPhase = ''
    runHook preInstall

    mkdir -p "$out/libexec/agentic-jujutsu"
    cp -a ./. "$out/libexec/agentic-jujutsu/"

    mkdir -p "$out/bin"
    makeWrapper ${nodejs}/bin/node "$out/bin/agentic-jujutsu" \
      --add-flags "$out/libexec/agentic-jujutsu/bin/cli.js" \
      --set NODE_PATH "$out/libexec/agentic-jujutsu/node_modules"

    makeWrapper ${nodejs}/bin/node "$out/bin/jj-agent" \
      --add-flags "$out/libexec/agentic-jujutsu/bin/cli.js" \
      --set NODE_PATH "$out/libexec/agentic-jujutsu/node_modules"

    runHook postInstall
  '';

  meta = {
    description = "Agent-oriented Jujutsu CLI wrapper";
    homepage = "https://github.com/ruvnet/agentic-flow/tree/main/packages/agentic-jujutsu";
    license = lib.licenses.mit;
    maintainers = [ ];
    mainProgram = "agentic-jujutsu";
  };
}
