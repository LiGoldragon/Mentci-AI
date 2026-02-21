@0xcf8e223fda633378;

struct Outcome {
  status @0 :Status;
  notes @1 :Text;
  
  enum Status {
    success @0;
    fail @1;
    partialSuccess @2;
  }
}

struct ToolInput {
  union {
    launcher @0 :LauncherInput;
    commit @1 :CommitInput;
    version @2 :VersionInput;
  }
}

struct LauncherInput {
  inputsPath @0 :Text;
  isImpure @1 :Bool;
}

struct CommitInput {
  message @0 :Text;
  targetBookmark @1 :Text;
}

struct VersionInput {
  coreDir @0 :Text;
}
