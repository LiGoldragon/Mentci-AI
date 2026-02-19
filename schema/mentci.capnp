@0xda3d767664917178;

using FS = import "atom_filesystem.capnp";

interface MentciDaemon {
  # Future RPC methods for the daemon
  ping @0 () -> ();
}

interface PreFetchOrchestrator {
  # Tool Specification: URL Prefetching
  prefetchUrl @0 (request :UrlPrefetchRequest) -> (response :UrlPrefetchResponse);

  # Tool Specification: Git Repository Prefetching
  prefetchGit @1 (request :GitPrefetchRequest) -> (response :GitPrefetchResponse);
}

struct UrlPrefetchRequest {
  url @0 :Text;
  unpack @1 :Bool = false;
  hashAlgo @2 :Text = "sha256";
}

struct UrlPrefetchResponse {
  sri @0 :Text; # The finalized SRI string
  path @1 :Text; # Deterministic derivation path
}

struct GitPrefetchRequest {
  url @0 :Text;
  rev @1 :Text; # Specific commit, branch, or tag
  submodules @2 :Bool = false;
}

struct GitPrefetchResponse {
  sri @0 :Text;
  rev @1 :Text; # Exact resolved commit revision
  path @2 :Text;
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
  provenance @2 :HostingProvenance;

  source :union {
    flake @3 :Text; # Flake URI
    git @4 :Text;   # Raw Git URL
    local @5 :Text; # Local filesystem path
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
  opencode @16;
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

# -- Runtime Orchestration (Attractor Alignment) --

struct Graph {
  nodes @0 :List(Node);
  edges @1 :List(Edge);
  startNode @2 :Text;
}

struct Node {
  id @0 :Text;
  label @1 :Text;
  shape @2 :Text;
  type @3 :Text;
  prompt @4 :Text;
}

struct Edge {
  from @0 :Text;
  to @1 :Text;
  label @2 :Text;
  condition @3 :Text;
}

struct Outcome {
  status @0 :StageStatus;
  notes @1 :Text;
  preferredLabel @2 :Text;
  # contextUpdates @3 :Map(Text, Text); # Cap'n Proto doesn't have native Map, use List of Entry
  contextUpdates @3 :List(Entry);
}

struct Entry {
  key @0 :Text;
  value @1 :Text;
}

enum StageStatus {
  success @0;
  partialSuccess @1;
  fail @2;
  retry @3;
  skipped @4;
}
