@0xb9f4e2c7a13d2d19;

struct IntelligenceCarrier {
  # Hash of the text-version (Aski/JSON), for synchronization
  textHash @0 :Text;

  # Core persona and behavior
  persona @1 :Persona;

  # Mandatory invariants (Supreme Law)
  invariants @2 :List(Invariant);

  # Collection of skills
  skills @3 :List(Skill);

  # Workspace-specific context
  context @4 :ProjectContext;
}

struct Persona {
  name @0 :Text;
  description @1 :Text;
  tone @2 :Text;
  beliefs @3 :List(Text);
}

struct Invariant {
  id @0 :Text;
  rule @1 :Text;
  severity @2 :Severity;
}

enum Severity {
  critical @0;
  advisory @1;
}

struct Skill {
  name @0 :Text;
  whenToUse @1 :Text;
  instructions @2 :Text;
  resources @3 :List(Text);
}

struct ProjectContext {
  mission @0 :Text;
  invariants @1 :List(Text);
  operationalSnapshot @2 :Text;
}
