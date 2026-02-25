{ craneLib, pkgs }:

let
  commonArgs = {
    pname = "chronos";
    version = "0.1.0";
    src = ../..;
    cargoExtraArgs = "--manifest-path Components/chronos/Cargo.toml";
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
