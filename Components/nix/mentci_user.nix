{ craneLib, pkgs }:

let
  commonArgs = {
    pname = "mentci-user";
    version = "0.1.0";
    src = ../..;
    cargoExtraArgs = "--manifest-path Components/mentci-user/Cargo.toml";
    nativeBuildInputs = [ pkgs.capnproto ];
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
