{ craneLib, pkgs }:

let
  commonArgs = {
    pname = "mentci-ai";
    version = "0.1.0";
    src = ../mentci-aid;
    nativeBuildInputs = [ pkgs.capnproto ];
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
