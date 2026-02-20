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
            attractorDocs = inputs."attractor-docs";
            pkgs = import nixpkgs { inherit system; };
            craneLib = crane.mkLib pkgs;
            src = craneLib.cleanCargoSource ./.;
    
            commonArgs = {
              pname = "mentci-ai";
              version = "0.1.0";
              inherit src;
              nativeBuildInputs = [ pkgs.capnproto ];
            };
    
            mentciAi = craneLib.buildPackage commonArgs;

            attractorPkg = pkgs.stdenv.mkDerivation {
              pname = "attractor";
              version = "0.1.0";
              src = attractorDocs;
              nativeBuildInputs = [ pkgs.bun pkgs.makeWrapper ];
              dontBuild = true;
              doCheck = true;
              checkPhase = ''
                runHook preCheck
                export HOME="$TMPDIR"
                mkdir -p attractor/node_modules
                ln -sf ../coding-agent attractor/node_modules/coding-agent
                ln -sf ../unified-llm attractor/node_modules/unified-llm
                cd attractor
                bun test $(find tests -name '*.test.ts' ! -path 'tests/integration/cross-feature-parity.test.ts' | sort)
                runHook postCheck
              '';
              installPhase = ''
                runHook preInstall
                mkdir -p $out/share/attractor $out/bin
                if [ -d attractor ]; then
                  rm -rf attractor/node_modules
                  cp -R attractor/. $out/share/attractor
                else
                  rm -rf node_modules
                  cp -R . $out/share/attractor
                fi
                makeWrapper ${pkgs.bun}/bin/bun $out/bin/attractor-server \
                  --add-flags "run $out/share/attractor/bin/attractor-server.ts"
                runHook postInstall
              '';
            };
    
            # -- OpenCode Agentic Interface --
            opencodePkg = pkgs.python3Packages.buildPythonApplication {
              pname = "opencode";
              version = "0.1.0";
              src = opencode;
              pyproject = true;
              build-system = [ pkgs.python3Packages.setuptools ];
              # OpenCode is a specialized tool, we wrap it to ensure it has its dependencies.
              # For now, we assume it's a standard Python app.
              doCheck = false;
              catchConflicts = false;
            };
    
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
    
            # -- Core Tooling --
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
              codex-cli-nix.packages.${system}.default
              (pkgs.writeShellScriptBin "mentci-commit" ''
                ${pkgs.babashka}/bin/bb ${./scripts/commit.clj} --runtime "$(pwd)/workspace/.mentci/runtime.json" "$@"
              '')
              (pkgs.writeShellScriptBin "mentci-jj" ''
                ${pkgs.babashka}/bin/bb ${./scripts/jj_workflow.clj} --runtime "$(pwd)/workspace/.mentci/runtime.json" "$@"
              '')
              (pkgs.writeShellScriptBin "mentci-bootstrap" ''
                ${pkgs.cargo}/bin/cargo run --quiet --bin mentci-ai -- job/jails bootstrap "$@"
              '')
            ];
    
            # Import jail configuration
            jail = import ./jail.nix {
              inherit pkgs;
              inputs = { 
                inherit criomos sema lojix seahawk skrips mkZolaWebsite;
                inherit webpublish goldragon maisiliym kibord aski attractor opencode;
                attractor-docs = attractorDocs;
              };
            };
    

      in {
        packages = {
          default = mentciAi;
          mentciAi = mentciAi;
          mentciClj = mentciClj;
          attractor = attractorPkg;
        };

        checks = {
          attractor = attractorPkg;
        };

        apps.default = flake-utils.lib.mkApp {
          drv = mentciAi;
        };

        devShells.default = pkgs.mkShell {
          name = "mentci-ai-dev";
          packages = commonPackages;
          env = {
            MENTCI_MODE = "ADMIN";
            MENTCI_RO_INDICATOR = "RW (Admin)";
            RUST_SRC_PATH = "${pkgs.rustPlatform.rustLibSrc}";
            MENTCI_REPO_ROOT = "$(pwd)";
            JJ_CONFIG = "$(pwd)/jj-project-config.toml";
            jailConfig = builtins.toJSON jail.jailConfig;
          };
        };
      });
}
