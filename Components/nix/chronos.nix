{ craneLib, pkgs, src, repo_root }:

let
  commonArgs = {
    pname = "chronos";
    version = "0.1.0";
    inherit src;
    cargoExtraArgs = "--manifest-path Cargo.toml";
    cargoLock = repo_root + "/Cargo.lock";
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
