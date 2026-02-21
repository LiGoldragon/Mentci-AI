{ pkgs, inputs, inputsPath ? "Inputs", outputsPath ? "Outputs" }:

let
  lib = pkgs.lib;

  mkInput = name: inputType: 
    let
      input = inputs.${name};
      # If it has a .src (is a derivation), use it. Otherwise use the whole thing (flake input/path).
      src = if (lib.isDerivation input && input ? src) then input.src else input;
    in {
      sourcePath = "${input.outPath}";
      srcPath = "${src}";
      inherit inputType;
    };

  # -- Ontology Resides in Data --
  inputManifest = {
    mentci-ai = {
      sourcePath = "${./.}";
      srcPath = "${./.}";
      inputType = "atom";
    };
    mentci-aid = {
      sourcePath = "${./.}";
      srcPath = "${./.}";
      inputType = "atom";
    };
    criomos = mkInput "criomos" "flake";
    lojix = mkInput "lojix" "flake";
    seahawk = mkInput "seahawk" "flake";
    skrips = mkInput "skrips" "flake";
    mkZolaWebsite = mkInput "mkZolaWebsite" "flake";
    webpublish = mkInput "webpublish" "flake";
    goldragon = mkInput "goldragon" "flake";
    maisiliym = mkInput "maisiliym" "flake";
    kibord = mkInput "kibord" "flake";
    aski = mkInput "aski" "flake";
    attractor = mkInput "attractor" "untyped";
    attractor-docs = mkInput "attractor-docs" "untyped";
    opencode = mkInput "opencode" "untyped";
  };

  outputManifest = {
    mentci-ai = {
      workspaceName = "output-mentci-ai";
      workingBookmark = "dev";
      targetBookmark = "jailCommit";
    };
  };

  allowedPushBookmarks = builtins.map (item: item.targetBookmark) (lib.attrValues outputManifest);
  networkPolicy = {
    mode = "push-only";
    allowedGitHosts = [
      "127.0.0.1"
      "localhost"
      "internal-git.test"
    ];
  };

  jailPolicyFile = pkgs.writeText "mentci-jail-policy.json" (builtins.toJSON {
    inherit outputsPath outputManifest allowedPushBookmarks networkPolicy;
  });

in
pkgs.mkShell {
  name = "mentci-jail";

  # Pass variables via structured attributes
  __structuredAttrs = true;
  
  # This will be available in .attrs.json
  jailConfig = {
    inherit inputsPath inputManifest outputsPath outputManifest;
    policyPath = "${jailPolicyFile}";
  };

  buildInputs = [
    pkgs.babashka
    pkgs.clojure
    pkgs.nix-prefetch-git
    pkgs.tree
  ];

  shellHook = ''
    # Minimal Shim: Call Clojure launcher
    bb ${./scripts/launcher.clj}
  '';
}
