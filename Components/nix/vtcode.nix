{ pkgs, src, rust_toolchain }:

let
  rustPlatform = pkgs.makeRustPlatform {
    cargo = rust_toolchain;
    rustc = rust_toolchain;
  };
in
rustPlatform.buildRustPackage {
  pname = "vtcode";
  version = "0.83.0-goldragon-fixes"; # from upstream
  src = src;

  cargoLock = {
    lockFile = "${src}/Cargo.lock";
    outputHashes = {
      # Need to check if vtcode has git dependencies that require outputHashes
    };
  };

  # Some tests or processes might need skipping or env variables
  doCheck = false;
}
