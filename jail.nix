{ pkgs, inputs, inputsPath ? "inputs" }:

let
  lib = pkgs.lib;

  # Helper to convert a list of input names to an attribute set mapping Name -> Store Path
  mkInputMap = names: 
    lib.genAttrs names (name: "${inputs.${name}.outPath}");

  # -- Ontology Resides in Data --
  inputManifest = {
    atom = {
      mentci-ai = "${./.}"; 
    };
    flake = mkInputMap [
      "lojix" "seahawk" "skrips" "mkZolaWebsite"
      "webpublish" "goldragon" "maisiliym" "kibord"
    ];
    untyped = mkInputMap [
      "attractor" "opencode"
    ];
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