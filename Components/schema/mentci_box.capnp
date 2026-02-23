@0xcf8e9a2b1d3c4e5f;

struct LinkGuardConfig {
  roots @0 :List(Text);
  rules @1 :List(Rule);
  allowlist @2 :List(Text);
}

struct Rule {
  name @0 :Text;
  regex @1 :Text;
  message @2 :Text;
  # Optional: ignore list for this specific rule
  ignore @3 :List(Text);
}

struct MentciBoxRequest {
  worktreePath @0 :Text;
  homePath @1 :Text;
  shareNetwork @2 :Bool = false;
  
  # Preparation steps
  fetchSources @3 :Bool = true;
  prepareComponents @4 :Bool = true;
  
  # Binds
  binds @5 :List(BindMapping);
  roBinds @6 :List(BindMapping);
}

struct BindMapping {
  source @0 :Text;
  target @1 :Text;
}
