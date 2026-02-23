{ craneLib, pkgs }:

let
  commonArgs = {
    pname = "mentci-vcs";
    version = "0.1.0";
    src = ../mentci-vcs;
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
