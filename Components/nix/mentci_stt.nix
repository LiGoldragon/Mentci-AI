{ craneLib, pkgs, src, mentci_box_lib_src, mentci_user_src, schema_src }:

let
  commonArgs = {
    pname = "mentci-stt";
    version = "0.1.0";
    inherit src;
    cargoExtraArgs = "--manifest-path Cargo.toml";
    nativeBuildInputs = [ pkgs.capnproto ];
    postUnpack = ''
      mkdir -p /build/mentci-box-lib
      cp -r ${mentci_box_lib_src}/. /build/mentci-box-lib/
      ln -s ${mentci_user_src} /build/mentci-user
      ln -s ${schema_src} /build/schema
    '';
    cargoLock = ../../Cargo.lock;
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
