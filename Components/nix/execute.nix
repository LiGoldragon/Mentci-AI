{ craneLib, pkgs, src, mentci_box_lib_src, schema_src, chronos_lib }:

let
  commonArgs = {
    pname = "mentci-execute";
    version = "0.1.0";
    inherit src;
    cargoExtraArgs = "--manifest-path Cargo.toml --bin execute";
    nativeBuildInputs = [ pkgs.capnproto ];
    postUnpack = ''
      mkdir -p /build/mentci-box-lib
      cp -r ${mentci_box_lib_src}/. /build/mentci-box-lib/
      ln -s ${schema_src} /build/schema
    '';
    cargoLock = src + "/Cargo.lock";
    doCheck = false;
    propagatedBuildInputs = [ chronos_lib ];
  };
in
craneLib.buildPackage commonArgs
