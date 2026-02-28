{ craneLib, pkgs, ... }:

let
  src = craneLib.cleanCargoSource (craneLib.path ./.);
  commonArgs = {
    pname = "mentci-user";
    version = "0.1.0";
    inherit src;
    nativeBuildInputs = [ pkgs.capnproto ];
    doCheck = false;
  };
  cargoArtifacts = craneLib.buildDepsOnly commonArgs;
in
craneLib.buildPackage (commonArgs // {
  inherit cargoArtifacts;
})
