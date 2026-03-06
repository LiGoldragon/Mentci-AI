{ craneLib, pkgs, src, schema_src }:

let
  commonArgs = {
    pname = "mentci-launch";
    version = "0.1.0";
    inherit src;
    cargoExtraArgs = "--manifest-path Cargo.toml";
    nativeBuildInputs = [ pkgs.capnproto ];
    postUnpack = ''
      ln -s ${schema_src} /build/schema
    '';
    cargoLock = ./locks/mentci_launch.Cargo.lock;
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
