{ craneLib, pkgs, src, aski_lib_src, repo_root }:

let
  commonArgs = {
    pname = "mentci-mcp";
    version = "0.1.0";
    inherit src;
    cargoExtraArgs = "--manifest-path Cargo.toml";
    nativeBuildInputs = [ pkgs.capnproto ];
    cargoLock = repo_root + "/Cargo.lock";
    postUnpack = ''
      ln -s ${aski_lib_src} /build/aski-lib
      ln -s ${repo_root}/Components/schema /build/schema
    '';
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
