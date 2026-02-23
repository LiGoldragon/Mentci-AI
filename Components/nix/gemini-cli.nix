{ lib
, buildNpmPackage
, fetchFromGitHub
, nodejs_22
, pkg-config
, libsecret
, makeWrapper
}:

buildNpmPackage rec {
  pname = "gemini-cli";
  version = "0.30.0-preview.3";
  nodejs = nodejs_22;

  src = fetchFromGitHub {
    owner = "google-gemini";
    repo = "gemini-cli";
    rev = "v${version}";
    hash = "sha256-px3ksJZzPQLE5idW7W9egbFPS2so3bZhnMaUpQGouI4=";
  };

  npmDepsHash = "sha256-+pvut1A8RP8dKp3tNAfzqTIu9YVLL8ZCZKtg44CsN0U=";

  nativeBuildInputs = [ pkg-config makeWrapper ];
  buildInputs = [ libsecret ];

  makeCacheWritable = true;
  npmFlags = [ "--legacy-peer-deps" ];
  
  buildPhase = ''
    runHook preBuild
    npm run bundle
    runHook postBuild
  '';

  installPhase = ''
    runHook preInstall

    mkdir -p $out/libexec/gemini
    cp -r bundle/* $out/libexec/gemini/
    
    # Copy node_modules, removing broken workspace symlinks
    cp -r node_modules $out/libexec/gemini/
    
    # Remove docs directory which contains broken symlinks
    rm -rf $out/libexec/gemini/docs

    # Remove all broken symlinks in the output
    find $out -type l -xtype l -print -delete

    mkdir -p $out/bin
    makeWrapper ${nodejs}/bin/node $out/bin/gemini \
      --add-flags "$out/libexec/gemini/gemini.js" \
      --set NODE_PATH "$out/libexec/gemini/node_modules"

    runHook postInstall
  '';

  meta = with lib; {
    description = "Gemini CLI tool";
    homepage = "https://github.com/google-gemini/gemini-cli";
    license = licenses.asl20;
    maintainers = with maintainers; [ ];
    mainProgram = "gemini";
  };
}
