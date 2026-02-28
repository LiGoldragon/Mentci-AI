{ craneLib, pkgs, src, ... }:

let
  # Support .capnp files in the source
  capnpFilter = path: type: (builtins.match ".*\\.capnp$" path != null);
  cleanSrc = pkgs.lib.cleanSourceWith {
    src = src;
    filter = path: type:
      (capnpFilter path type) || (craneLib.filterCargoSources path type);
  };
  commonArgs = {
    pname = "mentci-user";
    version = "0.1.0";
    src = cleanSrc;
    nativeBuildInputs = [ pkgs.capnproto ];
    doCheck = false;
  };
  cargoArtifacts = craneLib.buildDepsOnly commonArgs;
in
craneLib.buildPackage (commonArgs // {
  inherit cargoArtifacts;
})
