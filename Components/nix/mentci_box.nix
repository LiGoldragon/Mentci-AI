{ craneLib, pkgs, src, mentci_box_lib_src, repo_root }:

let
  commonArgs = {
    pname = "mentci-box";
    version = "0.1.0";
    inherit src;
    nativeBuildInputs = [ pkgs.capnproto ];
    cargoExtraArgs = "--manifest-path Cargo.toml";
    postUnpack = ''
      mkdir -p /build/mentci-box-lib
      cp -r ${mentci_box_lib_src}/. /build/mentci-box-lib/
      ln -s ${repo_root}/Components/schema /build/schema
    '';
    cargoLock = repo_root + "/Cargo.lock";
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
