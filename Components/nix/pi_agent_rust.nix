{ pkgs, src }:

pkgs.rustPlatform.buildRustPackage {
  pname = "pi-agent-rust";
  version = "unstable";
  src = src;

  cargoLock = {
    lockFile = "${src}/Cargo.lock";
  };

  doCheck = false;
}
