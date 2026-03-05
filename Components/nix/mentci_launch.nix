{ craneLib, pkgs, src, repo_root }:

let
  commonArgs = {
    pname = "mentci-launch";
    version = "0.1.0";
    inherit src;
    cargoExtraArgs = "--manifest-path Cargo.toml";
    nativeBuildInputs = [ pkgs.capnproto ];
    postUnpack = ''
      ln -s ${repo_root}/Components/schema /build/schema
    '';
    cargoLock = repo_root + "/Cargo.lock";
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
