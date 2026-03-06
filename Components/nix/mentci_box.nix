{ craneLib, pkgs, src, mentci_box_lib_src, schema_src }:

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
      ln -s ${schema_src} /build/schema
    '';
    cargoLock = ./locks/mentci_box.Cargo.lock;
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
