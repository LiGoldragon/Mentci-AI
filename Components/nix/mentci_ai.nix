{ craneLib, pkgs }:

let
  commonArgs = {
    pname = "mentci-ai";
    version = "0.1.0";
    src = ../..;
    cargoExtraArgs = "--manifest-path Components/mentci-aid/Cargo.toml";
    nativeBuildInputs = [ pkgs.capnproto ];
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
