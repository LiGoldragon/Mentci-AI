{ craneLib, pkgs }:

let
  commonArgs = {
    pname = "mentci-mcp";
    version = "0.1.0";
    src = ../..;
    cargoExtraArgs = "--manifest-path Components/mentci-mcp/Cargo.toml";
    nativeBuildInputs = [ pkgs.capnproto ];
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
