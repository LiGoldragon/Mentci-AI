{ craneLib, pkgs, src }:

let
  commonArgs = {
    pname = "mentci-user";
    version = "0.1.0";
    inherit src;
    cargoExtraArgs = "--manifest-path Cargo.toml";
    nativeBuildInputs = [ pkgs.capnproto ];
    cargoLock = if builtins.pathExists (src + "/Cargo.lock") then src + "/Cargo.lock" else ../../Cargo.lock;
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
