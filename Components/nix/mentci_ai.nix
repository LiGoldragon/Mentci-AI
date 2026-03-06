{ craneLib, src }:

let
  commonArgs = {
    pname = "mentci-ai";
    version = "0.1.0";
    inherit src;
    cargoExtraArgs = "--manifest-path Cargo.toml";
    cargoLock = if builtins.pathExists (src + "/Cargo.lock") then src + "/Cargo.lock" else ../../Cargo.lock;
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
