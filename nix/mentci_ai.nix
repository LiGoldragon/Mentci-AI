{ craneLib, pkgs, src }:

let
  commonArgs = {
    pname = "mentci-ai";
    version = "0.1.0";
    inherit src;
    nativeBuildInputs = [ pkgs.capnproto ];
  };
in
craneLib.buildPackage commonArgs
