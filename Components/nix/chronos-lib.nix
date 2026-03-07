{ craneLib, src }:

let
  commonArgs = {
    pname = "chronos-lib";
    version = "0.1.0";
    inherit src;
    cargoExtraArgs = "--manifest-path Cargo.toml";
    cargoLock = src + "/Cargo.lock";
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
