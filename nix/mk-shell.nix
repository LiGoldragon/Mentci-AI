{ pkgs }:

# A pure-clojure re-implementation of mkShell using builtins.derivation.
# Avoids stdenv overhead and allows overriding defaults via Clojure logic.

{ name ? "mentci-shell"
, packages ? [ ]
, shellHook ? ""
, env ? { }
}:

let
  builder = ../scripts/mk_shell.clj;
  
  # Ensure all packages are built and available as paths
  packagePaths = map (p: "${p}") packages;

  shellSpec = {
    inherit name shellHook env;
    packages = packagePaths;
  };

  # The actual derivation
  drv = builtins.derivation {
    inherit name;
    inherit (pkgs) system;
    
    # Use bash as builder to satisfy 'nix develop'
    builder = "${pkgs.bash}/bin/bash";
    args = [ "-c" "${pkgs.babashka}/bin/bb ${builder}" ];
    
    # Pass configuration
    spec = builtins.toJSON shellSpec;
    
    # Structured Attributes support
    __structuredAttrs = true;
    exportReferencesGraph.references = packages;
    
    # Dependencies needed by the builder or the shell
    inherit packages;
  } // (env);

in
# We return an attribute set that looks like a derivation
# but provides a shellHook that loads our generated setup file.
drv // {
  # This makes it compatible with 'nix develop' and 'nix-shell'
  shellHook = ''
    source ${drv}/setup
  '';
}
