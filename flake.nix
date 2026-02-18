{
  description = "Mentci-AI Rust Daemon";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.05";
    flake-utils.url = "github:numtide/flake-utils";
    crane.url = "github:ipetkov/crane";
    crane.inputs.nixpkgs.follows = "nixpkgs";
  };

  outputs = { self, nixpkgs, flake-utils, crane }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
        craneLib = crane.lib.${system};
        src = craneLib.cleanCargoSource ./.;

        commonArgs = {
          pname = "mentci-ai";
          version = "0.1.0";
          inherit src;
          nativeBuildInputs = [ pkgs.capnproto ];
        };

        mentciAi = craneLib.buildPackage commonArgs;
      in {
        packages = {
          default = mentciAi;
          mentciAi = mentciAi;
        };

        apps.default = flake-utils.lib.mkApp {
          drv = mentciAi;
        };

        devShells.default = pkgs.mkShell {
          packages = [
            pkgs.capnproto
            pkgs.cargo
            pkgs.git
            pkgs.nixpkgs-fmt
            pkgs.rustc
            pkgs.rust-analyzer
          ];
          shellHook = ''
            export RUST_SRC_PATH=${pkgs.rustPlatform.rustLibSrc}
          '';
        };
      });
}
