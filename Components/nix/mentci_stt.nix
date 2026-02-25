{ craneLib, pkgs }:

let
  commonArgs = {
    pname = "mentci-stt";
    version = "0.1.0";
    src = ../..;
    cargoExtraArgs = "--manifest-path Components/mentci-stt/Cargo.toml";
    nativeBuildInputs = [ pkgs.capnproto ];
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
