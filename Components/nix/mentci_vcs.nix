{ craneLib, pkgs }:

let
  commonArgs = {
    pname = "mentci-vcs";
    version = "0.1.0";
    src = ../mentci-vcs;
    cargoLock = ../mentci-vcs/Cargo.lock;
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
