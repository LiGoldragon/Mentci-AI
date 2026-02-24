{ craneLib, pkgs, repo_root }:

let
  commonArgs = {
    pname = "mentci-launch";
    version = "0.1.0";
    src = repo_root;
    cargoExtraArgs = "--manifest-path Components/mentci-launch/Cargo.toml";
    nativeBuildInputs = [ pkgs.capnproto ];
    doCheck = false;
  };
in
craneLib.buildPackage commonArgs
