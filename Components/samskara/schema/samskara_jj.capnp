@0xf8a3b5c2d1e9f4a7; # Unique ID for Saṃskāra-JJ contract

struct JjFlow {
  changeId @0 :Text;
  commitId @1 :Text;
  description @2 :Text;
  author @3 :Text;
  timestamp @4 :Text;
  isWorkingCopy @5 :Bool;
  isConflict @6 :Bool;
  bookmarks @7 :List(Text);
  parents @8 :List(Text);
}

struct FlowBatch {
  flows @0 :List(JjFlow);
}
