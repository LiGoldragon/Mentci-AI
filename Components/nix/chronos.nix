{ craneLib, pkgs, src }:

let
  commonArgs = {
    pname = "chronos";
    version = "0.1.0";
    inherit src;
    cargoExtraArgs = "--manifest-path Cargo.toml";
    cargoLock = ./locks/chronos.Cargo.lock;
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
