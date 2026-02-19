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
    pkgs.python3
    pkgs.python3Packages.ply
    pkgs.python3Packages.pyrfc3339
    pkgs.python3Packages.pytz
    pkgs.python3Packages.six
    pkgs.nix-prefetch-git
    pkgs.tree
  ];

  shellHook = ''
    # Minimal Shim: Call Python launcher
    # The .attrs.json path is provided by Nix when __structuredAttrs is enabled
    python3 ${./jail_launcher.py}
  '';
}