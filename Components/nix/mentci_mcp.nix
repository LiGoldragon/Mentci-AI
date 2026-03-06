{ craneLib, pkgs, src, aski_lib_src, schema_src }:

let
  commonArgs = {
    pname = "mentci-mcp";
    version = "0.1.0";
    inherit src;
    cargoExtraArgs = "--manifest-path Cargo.toml";
    nativeBuildInputs = [ pkgs.capnproto ];
    cargoLock = if builtins.pathExists (src + "/Cargo.lock") then src + "/Cargo.lock" else ../../Cargo.lock;
    postUnpack = ''
      ln -s ${aski_lib_src} /build/aski-lib
      ln -s ${schema_src} /build/schema
    '';
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
