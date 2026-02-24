{ pkgs, src, rust_toolchain, pi_mono_src }:

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

  postPatch = ''
    mkdir -p legacy_pi_mono_code/pi-mono/packages/ai/src
    cp ${pi_mono_src}/packages/ai/src/models.generated.ts \
      legacy_pi_mono_code/pi-mono/packages/ai/src/models.generated.ts
  '';

  doCheck = false;
}
