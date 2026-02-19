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

        # -- Clojure Orchestrator Derivation --
        mentciClj = pkgs.stdenv.mkDerivation {
          pname = "mentci-clj-orchestrator";
          version = "0.1.0";
          src = ./scripts;
          nativeBuildInputs = [ pkgs.makeWrapper ];
          buildInputs = [ pkgs.babashka ];
          installPhase = ''
            mkdir -p $out/bin
            cp *.clj $out/bin/
            for f in $out/bin/*.clj; do
              chmod +x "$f"
              # Wrap with babashka
              mv "$f" "$f.orig"
              makeWrapper ${pkgs.babashka}/bin/bb "$f" --add-flags "$f.orig"
            done
          '';
        };

        # -- Pure Clojure Shell Factory --
        mkClojureShell = import ./nix/mk-shell.nix { inherit pkgs; };

        # -- Core Tooling (Pure Clojure Stack) --
        # Purpose: Level 5 orchestration via Babashka and JVM Clojure.
        commonPackages = [
          pkgs.babashka
          pkgs.clojure
          pkgs.clojure-lsp
          pkgs.jet
          pkgs.jujutsu
          pkgs.capnproto
          pkgs.cargo
          pkgs.rustc
          pkgs.rust-analyzer
          pkgs.git
          pkgs.gdb
          pkgs.strace
          pkgs.valgrind
          pkgs.rsync
          (pkgs.writeShellScriptBin "mentci-commit" ''
            ${pkgs.babashka}/bin/bb ${./scripts/commit.clj} "$@"
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
          mentciClj = mentciClj;
        };

        apps.default = flake-utils.lib.mkApp {
          drv = mentciAi;
        };

        devShells.default = mkClojureShell {
          name = "mentci-ai-dev";
          packages = commonPackages;
          env = {
            MENTCI_MODE = "ADMIN";
            MENTCI_RO_INDICATOR = "RW (Admin)";
            RUST_SRC_PATH = "${pkgs.rustPlatform.rustLibSrc}";
            MENTCI_REPO_ROOT = "$(pwd)";
            MENTCI_COMMIT_TARGET = "dev";
            MENTCI_WORKSPACE = "$(pwd)/workspace";
            JJ_CONFIG = "$(pwd)/jj-project-config.toml";
          };
          shellHook = ''
            # 1. Workspace Initialization (jj workspace)
            if [ ! -d "$MENTCI_WORKSPACE" ]; then
              echo "Initializing Agent Workspace..."
              jj workspace add "$MENTCI_WORKSPACE"
            fi

            # 2. Run Jail Launcher to organize read-only inputs
            bb ${./scripts/launcher.clj}

            echo "--------------------------------------------------"
            echo "Mentci-AI Pure Clojure Development Environment Active."
            echo "Main Repo: $MENTCI_REPO_ROOT (RO Intent)"
            echo "Workspace: $MENTCI_WORKSPACE (RW Implementation)"
            echo "Commit Target: $MENTCI_COMMIT_TARGET"
            echo "--------------------------------------------------"
          '';
        };
      });
}