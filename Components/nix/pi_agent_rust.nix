{ pkgs, src, rust_toolchain }:

let
  rustPlatform = pkgs.makeRustPlatform {
    cargo = rust_toolchain;
    rustc = rust_toolchain;
  };
in
rustPlatform.buildRustPackage {
  pname = "pi-agent-rust";
  version = "unstable";
  src = src;

  cargoLock = {
    lockFile = "${src}/Cargo.lock";
  };
  doCheck = false;
}
