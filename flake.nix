{
  description = "Mentci-AI Rust Daemon";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/fb7944c166a3b630f177938e478f0378e64ce108";
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
    kibord.url = "github:LiGoldragon/kibord";

    # External Collaborations
    attractor = {
      url = "github:strongdm/attractor";
      flake = false;
    };
    opencode = {
      url = "github:opencode-ai/opencode";
      flake = false;
    };
  };

  outputs = { 
    self, nixpkgs, flake-utils, crane, 
    criomos, sema, lojix, seahawk, skrips, mkZolaWebsite,
    webpublish, goldragon, maisiliym, kibord, attractor, opencode
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

        # -- Core Tooling --
        commonPackages = [
          pkgs.capnproto
          pkgs.cargo
          pkgs.git
          pkgs.nixpkgs-fmt
          pkgs.rustc
          pkgs.rust-analyzer
          pkgs.jet
          pkgs.python3
          pkgs.jujutsu
          pkgs.gdb
          pkgs.strace
          pkgs.valgrind
          pkgs.rsync
          (pkgs.writeShellScriptBin "mentci-commit" ''
            ${pkgs.python3}/bin/python3 ${./scripts/jail_commit.py} "$@"
          '')
        ];

        # Import jail configuration
        jail = import ./jail.nix {
          inherit pkgs;
          inputs = { 
            inherit criomos sema lojix seahawk skrips mkZolaWebsite;
            inherit webpublish goldragon maisiliym kibord attractor opencode;
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
          name = "mentci-ai-dev";
          inputsFrom = [ jail ];
          packages = commonPackages;
          shellHook = ''
            export MENTCI_MODE="ADMIN"
            export MENTCI_RO_INDICATOR="RW (Admin)"
            export RUST_SRC_PATH=${pkgs.rustPlatform.rustLibSrc}
            export MENTCI_REPO_ROOT="$(pwd)"
            export MENTCI_COMMIT_TARGET="dev"
            export MENTCI_WORKSPACE="$(pwd)/workspace"

            # 0. Global JJ Configuration
            # Explicitly include the tracked project config for all jj operations in this shell
            export JJ_CONFIG="$MENTCI_REPO_ROOT/jj-project-config.toml"

            # 1. Workspace Initialization (jj workspace)
            if [ ! -d "$MENTCI_WORKSPACE" ]; then
              echo "Initializing Agent Workspace..."
              # jj workspace add creates a checkout of the current branch in a subfolder
              # This folder is ignored by the main repo's .gitignore
              jj workspace add "$MENTCI_WORKSPACE"
            fi

            # 2. Run Jail Launcher to organize read-only inputs
            ${jail.shellHook}

            echo "--------------------------------------------------"
            echo "Mentci-AI Development Environment Active."
            echo "Main Repo: $MENTCI_REPO_ROOT (RO Intent)"
            echo "Workspace: $MENTCI_WORKSPACE (RW Implementation)"
            echo "Commit Target: $MENTCI_COMMIT_TARGET"
            echo "--------------------------------------------------"
          '';
        };
      });
}