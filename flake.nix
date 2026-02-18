{
  description = "Mentci-AI Rust Daemon";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.05";
    flake-utils.url = "github:numtide/flake-utils";
    crane.url = "github:ipetkov/crane";
    crane.inputs.nixpkgs.follows = "nixpkgs";

    # Level 5 / Criome Ecosystem Inputs
    criomos.url = "github:Criome/CriomOS";
    sema.url = "github:Criome/sema";
    lojix.url = "github:Criome/lojix";
    seahawk.url = "github:Criome/seahawk";
    skrips.url = "github:Criome/skrips";
    mkZolaWebsite.url = "github:Criome/mkZolaWebsite";

    # LiGoldragon Inputs
    webpublish.url = "github:LiGoldragon/WebPublish";
    goldragon.url = "github:LiGoldragon/goldragon";
    maisiliym.url = "github:LiGoldragon/maisiliym";
    ndi.url = "github:LiGoldragon/ndi";
    kibord.url = "github:LiGoldragon/kibord";
  };

  outputs = { 
    self, nixpkgs, flake-utils, crane, 
    criomos, sema, lojix, seahawk, skrips, mkZolaWebsite,
    webpublish, goldragon, maisiliym, ndi, kibord
  }:
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
          inputs = { 
            inherit criomos sema lojix seahawk skrips mkZolaWebsite;
            inherit webpublish goldragon maisiliym ndi kibord;
          };
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