{
  description = "Mentci-AI Rust Daemon";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.05";
    flake-utils.url = "github:numtide/flake-utils";
    crane.url = "github:ipetkov/crane";
    crane.inputs.nixpkgs.follows = "nixpkgs";

    criomos.url = "github:Criome/CriomOS";
    sema.url = "github:Criome/sema";
    lojix.url = "github:Criome/lojix";
    webpublish.url = "github:LiGoldragon/WebPublish";
  };

  outputs = { self, nixpkgs, flake-utils, crane, criomos, sema, lojix, webpublish }:
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

        # Import jail configuration
        jail = import ./jail.nix {
          inherit pkgs;
          inputs = { inherit criomos sema lojix webpublish; };
          # Default input path is "inputs", can be overridden here
        };

      in {
        packages = {
          default = mentciAi;
          mentciAi = mentciAi;
        };

        apps.default = flake-utils.lib.mkApp {
          drv = mentciAi;
        };

        devShells.default = pkgs.mkShell {
          inputsFrom = [ jail ];
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
            ${jail.shellHook}
          '';
        };
      });
}
