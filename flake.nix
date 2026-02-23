{
  description = "Mentci-AI Rust Daemon";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/fb7944c166a3b630f177938e478f0378e64ce108";
    flake-utils.url = "github:numtide/flake-utils";
    crane.url = "github:ipetkov/crane";

    # Level 5 / Criome Ecosystem Sources
    criomos.url = "github:Criome/CriomOS";
    sema.url = "github:Criome/sema";
    lojix.url = "github:Criome/lojix";
    seahawk.url = "github:Criome/seahawk";
    skrips.url = "github:Criome/skrips";
    mkZolaWebsite.url = "github:Criome/mkZolaWebsite";

    # LiGoldragon Sources
    webpublish.url = "github:LiGoldragon/WebPublish";
    goldragon.url = "github:LiGoldragon/goldragon";
    maisiliym.url = "github:LiGoldragon/maisiliym";
    kibord.url = "github:LiGoldragon/kibord";
    bookofsol = {
      url = "github:LiGoldragon/TheBookOfSol";
      flake = false;
    };
    bookofgoldragon = {
      url = "github:LiGoldragon/TheBookOfGoldragon";
      flake = false;
    };
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

        pi-mono = {
          url = "github:badlogic/pi-mono";
          flake = false;
        };

        # Research Sources
        leash = {
          url = "github:strongdm/leash";
          flake = false;
        };
        activeadmin = {
          url = "github:activeadmin/activeadmin";
          flake = false;
        };
      };
    
      outputs = inputs@{
        self, nixpkgs, flake-utils, crane, 
        criomos, sema, lojix, seahawk, skrips, mkZolaWebsite,
        webpublish, goldragon, maisiliym, kibord, bookofsol, bookofgoldragon, aski, attractor, opencode, codex-cli-nix, leash, ...
      }:
        flake-utils.lib.eachDefaultSystem (system:
          let
            attractorSrc = inputs.attractor;
            attractorDocsSrc = inputs."attractor-docs";
            pkgs = import nixpkgs { inherit system; };
            craneLib = crane.mkLib pkgs;
    
            namespace = import ./Components/nix {
              inherit pkgs craneLib system inputs;
              codex_cli_nix = codex-cli-nix;
              attractor_src = attractorSrc;
              attractor_docs_src = attractorDocsSrc;
              pi_mono_src = inputs.pi-mono;
              repo_root = ./.;
            };

            jail = import ./Components/nix/jail.nix {
              inherit pkgs;
              Sources = namespace.jail_sources;
            };

            devShell = namespace.dev_shell { inherit jail; };
    

      in {
        packages = {
          default = namespace.mentci_ai;
          mentciAi = namespace.mentci_ai;
          mentciBox = namespace.mentci_box;
          mentciBoxDefault = namespace.mentci_box_default;
          execute = namespace.execute;
          attractor = namespace.attractor;
          codingAgent = namespace.coding_agent;
          unifiedLlm = namespace.unified_llm;
          gemini-cli = namespace.gemini_cli;
        };

        checks = {
          attractor = namespace.attractor;
          execute = namespace.execute_check;
        };

        apps.default = flake-utils.lib.mkApp {
          drv = namespace.mentci_ai;
        };
        apps.execute = flake-utils.lib.mkApp {
          drv = namespace.execute;
        };

        devShells.default = devShell;
      });
}
