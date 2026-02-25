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
  # Where the project inputs are mounted/linked
  inputsPath @0 :Text;
  
  # Whether to enforce pure isolation
  pure @1 :Bool;
}