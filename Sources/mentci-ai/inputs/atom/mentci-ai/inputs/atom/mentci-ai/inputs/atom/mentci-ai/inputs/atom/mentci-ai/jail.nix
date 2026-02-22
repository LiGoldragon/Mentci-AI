{ pkgs, inputs, inputsPath ? "inputs" }:

let
  lib = pkgs.lib;

  # Helper to convert a list of input names to an attribute set mapping Name -> Store Path
  # We try to access .outPath directly to avoid evaluating the whole flake output set
  # which might cause infinite recursion for self-referential flakes like 'sema'.
  mkInputMap = names: 
    lib.genAttrs names (name: "${inputs.${name}.outPath}");

  # -- Ontology Resides in Data --
  inputManifest = {
    atom = {
      mentci-ai = "${./.}"; # Use path to current directory
    };
    flake = mkInputMap [
      # "criomos" "sema" # Disabled due to infinite recursion in flake evaluation
      "lojix" "seahawk" "skrips" "mkZolaWebsite"
      "webpublish" "goldragon" "maisiliym" "kibord"
    ];
    untyped = mkInputMap [
      "attractor"
    ];
  };

  # Serialize configuration to JSON for Python
  jailConfig = pkgs.writeText "jail_config.json" (builtins.toJSON {
    inherit inputsPath inputManifest;
  });

in
pkgs.mkShell {
  name = "mentci-jail";

  buildInputs = [
    pkgs.python3
    pkgs.capnproto
    pkgs.gdb
    pkgs.strace
    pkgs.valgrind
  ];

  shellHook = ''
    # Export config path explicitly
    export JAIL_CONFIG_JSON="${jailConfig}"

    # Minimal Shim: Delegate immediately to Python
    # We use 'python3' from buildInputs
    
    exec python3 ${./jail_launcher.py}
  '';
}
