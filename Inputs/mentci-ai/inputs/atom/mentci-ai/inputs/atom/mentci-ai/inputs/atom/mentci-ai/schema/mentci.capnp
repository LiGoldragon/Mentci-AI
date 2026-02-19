@0xda3d767664917178;

interface MentciDaemon {
  # Future RPC methods for the daemon
  ping @0 () -> ();
}

struct Project {
  name @0 :Text;
  description @1 :Text;
  
  # The inputs/dependencies for this project
  inputs @2 :List(Input);
  
  # Configuration for the jail environment
  jailConfig @3 :JailConfig;
}

struct Input {
  name @0 :Text;
  
  # The known type from the "Level 5" ecosystem
  type @1 :InputType;
  
  # Where this input comes from
  source @2 :Source;

  union Source {
    flake @3 :Text; # Flake URI (e.g., "github:Criome/CriomOS")
    git @4 :Text;   # Raw Git URL
    local @5 :Text; # Local filesystem path
  }
}

enum InputType {
  # Structural / Archetypal Types
  flake @0;        # Generic Nix Flake
  git @1;          # Raw Git Repository
  atomV0 @2;       # Prototype Criome "Atom" (Sema-compatible)
  typedAtom @3;    # Fully Typed "Dark Factory" Atom (Mentci-AI Standard)

  # Specific Project Overrides (Legacy/Transitional)
  # These may eventually be redefined as metadata on one of the types above.
  mentci @4;      
  criomos @5;
  sema @6;
  lojix @7;
  seahawk @8;
  skrips @9;
  mkZolaWebsite @10;
  webpublish @11;
  goldragon @12;
  maisiliym @13;
  kibord @14;
  attractor @15;
}

struct JailConfig {
  inputsPath @0 :Text;
  pure @1 :Bool;
}

# -- Filesystem Ontology --
# This struct maps the physical filesystem to semantic types.

struct NixFlake {
  description @0 :Text;
  
  # Inputs mapping (name -> FlakeInput)
  inputs @1 :List(FlakeInputEntry);
  
  # Outputs function definition (complex in Nix, simplified here as code or strict structure)
  outputs @2 :FlakeOutputs; 
}

struct FlakeInputEntry {
  key @0 :Text;
  value @1 :FlakeInput;
}

struct FlakeInput {
  url @0 :Text;
  inputs @1 :List(FlakeInputEntry); # For 'follows' or overrides
  flake @2 :Bool = true;            # Default true
}

struct FlakeOutputs {
  # The raw Nix expression function: { self, nixpkgs, ... }: { ... }
  expression @0 :Text; 
}

struct MentciRepository {
  # 1. Documentation (The "Why" and "How")
  architecturalGuidelines @0 :MarkdownFile; # ARCHITECTURAL_GUIDELINES.md
  level5Philosophy @1 :MarkdownFile;        # Level5-Ai-Coding.md
  workContext @2 :MarkdownFile;             # Work.md
  references @3 :MarkdownFile;              # REFERENCES.md
  readme @4 :MarkdownFile;                  # README.md

  # 2. Nix Infrastructure (The "Where" and "Environment")
  flake @5 :NixFile;                        # flake.nix
  flakeLock @6 :LockFile;                   # flake.lock
  jail @7 :NixFile;                         # jail.nix
  
  # 3. Python Glue (The "Launcher")
  jailLauncher @8 :PythonFile;              # jail_launcher.py

  # 4. Schema (The "Truth")
  schemaDir @9 :SchemaDirectory;            # schema/
  
  # 5. Logs (Memory)
  logsDir @10 :LogsDirectory;               # logs/

  # 6. Implementation (The "What")
  srcDir @11 :RustDirectory;                # src/
  cargoToml @12 :TomlFile;                  # Cargo.toml
  cargoLock @13 :LockFile;                  # Cargo.lock
  
  # 7. Version Control
  gitIgnore @14 :TextFile;                  # .gitignore
}

struct LogsDirectory {
  # Per-user log files, e.g., "logs/user_li.log"
  userLogs @0 :List(MachineLogFile);
}

struct MachineLogFile {
  entries @0 :List(LogEntry);
}

struct LogEntry {
  timestamp @0 :Text;       # ISO 8601 preferred
  userId @1 :Text;          # e.g., "li", "system"
  intentSummary @2 :Text;   # "Short summary of the intent"
  model @3 :Text;           # e.g., "gemini-2.0-flash"
  signature @4 :Data;       # Future cryptographic signature
}

struct SchemaDirectory {
  mentciSpec @0 :CapnpFile;                 # mentci.capnp
}

struct RustDirectory {
  main @0 :RustFile;                        # main.rs
}

# -- Primitives --
struct MarkdownFile { content @0 :Text; }
struct NixFile { expression @0 :Text; }
struct PythonFile { script @0 :Text; }
struct CapnpFile { spec @0 :Text; }
struct RustFile { code @0 :Text; }
struct TomlFile { config @0 :Text; }
struct LockFile { json @0 :Text; }
struct TextFile { text @0 :Text; }