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
  generic @0;
  mentci @1;      # Self-reference
  criomos @2;
  sema @3;
  lojix @4;
  webpublish @5;
}

struct JailConfig {
  # Where the project inputs are mounted/linked
  inputsPath @0 :Text;
  
  # Whether to enforce pure isolation
  pure @1 :Bool;
}
