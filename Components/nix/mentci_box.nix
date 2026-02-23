{ craneLib, pkgs }:

let
  commonArgs = {
    pname = "mentci-box";
    version = "0.1.0";
    src = ../.;
    nativeBuildInputs = [ pkgs.capnproto ];
    cargoExtraArgs = "-p mentci-box";
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
