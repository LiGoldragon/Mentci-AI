@0xda3d767664917178;

using FS = import "atom_filesystem.capnp";

interface MentciDaemon {
  # Future RPC methods for the daemon
  ping @0 () -> ();
}

struct Project {
  atom @0 :Atom;
  
  # Configuration for the jail environment
  jailConfig @1 :JailConfig;
}

struct Atom {
  name @0 :Text;
  description @1 :Text;
  
  # Optional Content-Addressable hash (e.g., Nix store hash or git commit)
  caHash @2 :Text;
  
  # Where this atom can be found / hosting provenance
  sources @3 :List(Input);
  
  # The filesystem structure of this atom
  filesystem @4 :FS.AtomFilesystem;
}

struct Input {
  name @0 :Text;
  type @1 :InputType;
  source @2 :Source;
  provenance @3 :HostingProvenance;

  union Source {
    flake @4 :Text; # Flake URI
    git @5 :Text;   # Raw Git URL
    local @6 :Text; # Local filesystem path
  }
}

enum HostingProvenance {
  unknown @0;
  github @1;
  gitlab @2;
  sourcehut @3;
  criome @4;
  custom @5;
}

enum InputType {
  # Structural / Archetypal Types
  flake @0;        # Generic Nix Flake
  git @1;          # Raw Git Repository
  atomV0 @2;       # Prototype Criome "Atom"
  typedAtom @3;    # Fully Typed "Dark Factory" Atom

  # Specific Project Overrides (Legacy/Transitional)
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

struct NixFlake {
  description @0 :Text;
  inputs @1 :List(FlakeInputEntry);
  outputs @2 :FlakeOutputs; 
}

struct FlakeInputEntry {
  key @0 :Text;
  value @1 :FlakeInput;
}

struct FlakeInput {
  url @0 :Text;
  inputs @1 :List(FlakeInputEntry); 
  flake @2 :Bool = true;
}

struct FlakeOutputs {
  expression @0 :Text; 
}
