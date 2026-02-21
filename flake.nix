{
  description = "Mentci-AI Rust Daemon";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/fb7944c166a3b630f177938e478f0378e64ce108";
    flake-utils.url = "github:numtide/flake-utils";
    crane.url = "github:ipetkov/crane";

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
    kibord.url = "github:LiGoldragon/kibord";
    aski = {
      url = "github:Criome/aski";
      flake = false;
    };

    # External Collaborations
        attractor = {
          url = "github:strongdm/attractor";
          flake = false;
        };
        attractor-docs = {
          url = "github:brynary/attractor";
          flake = false;
        };
    
        opencode = {
          url = "github:opencode-ai/opencode";
          flake = false;
        };
        codex-cli-nix.url = "github:sadjow/codex-cli-nix";
      };
    
      outputs = inputs@{ 
        self, nixpkgs, flake-utils, crane, 
        criomos, sema, lojix, seahawk, skrips, mkZolaWebsite,
        webpublish, goldragon, maisiliym, kibord, aski, attractor, opencode, codex-cli-nix, ...
      }:
        flake-utils.lib.eachDefaultSystem (system:
          let
            attractorSrc = inputs.attractor;
            attractorDocsSrc = inputs."attractor-docs";
            pkgs = import nixpkgs { inherit system; };
            craneLib = crane.mkLib pkgs;
            src = craneLib.cleanCargoSource ./.;
    
            namespace = import ./nix {
              inherit pkgs craneLib system inputs src;
              codex_cli_nix = codex-cli-nix;
              attractor_src = attractorSrc;
              attractor_docs_src = attractorDocsSrc;
              scripts_dir = ./scripts;
              repo_root = ./.;
              jj_project_config = ./jj-project-config.toml;
            };

            jail = import ./jail.nix {
              inherit pkgs;
              inputs = namespace.jail_inputs;
            };

            devShell = namespace.dev_shell { inherit jail; };
    

      in {
        packages = {
          default = namespace.mentci_ai;
          mentciAi = namespace.mentci_ai;
          mentciClj = namespace.mentci_clj;
          attractor = namespace.attractor;
          gemini-cli = namespace.gemini_cli;
        };

        checks = {
          attractor = namespace.attractor;
        };

        apps.default = flake-utils.lib.mkApp {
          drv = namespace.mentci_ai;
        };

        devShells.default = devShell;
      });
}
