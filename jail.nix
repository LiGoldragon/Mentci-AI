{ pkgs, inputs, inputsPath ? "inputs" }:

let
  lib = pkgs.lib;

  mkInput = name: inputType: {
    sourcePath = "${inputs.${name}.outPath}";
    inherit inputType;
  };

  # -- Ontology Resides in Data --
  inputManifest = {
    mentci-ai = {
      sourcePath = "${./.}";
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
    brynary-attractor = mkInput "brynary-attractor" "untyped";
    opencode = mkInput "opencode" "untyped";
  };

in
pkgs.mkShell {
  name = "mentci-jail";

  # Pass variables via structured attributes
  __structuredAttrs = true;
  
  # This will be available in .attrs.json
  jailConfig = {
    inherit inputsPath inputManifest;
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
