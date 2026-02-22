@0x8ed54326ad743f57;

# -- Atom Filesystem Ontology --
# This spec defines the structural types for a "Level 5" repository.

struct AtomFilesystem {
  # 1. Documentation (The "Why" and "How")
  architecturalGuidelines @0 :MarkdownFile;
  level5Philosophy @1 :MarkdownFile;
  workContext @2 :MarkdownFile;
  references @3 :MarkdownFile;
  readme @4 :MarkdownFile;

  # 2. Nix Infrastructure (The "Where" and "Environment")
  flake @5 :NixFile;
  flakeLock @6 :LockFile;
  jail @7 :NixFile;
  
  # 3. Python Glue (The "Launcher")
  jailLauncher @8 :PythonFile;

  # 4. Schema (The "Truth")
  schemaDir @9 :SchemaDirectory;
  
  # 5. Logs (Memory)
  logsDir @10 :LogsDirectory;               # Logs/

  # 6. Implementation (The "What")
  srcDir @11 :RustDirectory;
  cargoToml @12 :TomlFile;
  cargoLock @13 :LockFile;
  
  # 7. Version Control
  gitIgnore @14 :TextFile;
}

struct SchemaDirectory {
  specs @0 :List(CapnpFileEntry);
}

struct CapnpFileEntry {
  name @0 :Text;
  file @1 :CapnpFile;
}

struct RustDirectory {
  files @0 :List(RustFileEntry);
}

struct RustFileEntry {
  name @0 :Text;
  file @1 :RustFile;
}

struct LogsDirectory {
  # Per-user log files, e.g., "logs/user_li.log"
  userLogs @0 :List(MachineLogFileEntry);
}

struct MachineLogFileEntry {
  name @0 :Text;
  file @1 :MachineLogFile;
}

struct MachineLogFile {
  entries @0 :List(LogEntry);
}

struct LogEntry {
  timestamp @0 :Text;       # ISO 8601
  userId @1 :Text;
  intentSummary @2 :Text;
  model @3 :Text;
  signature @4 :Data;
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
