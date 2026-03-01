@0xbf8e4f1a2b3c4d5e;

struct Datom {
  entity @0 :UInt64;
  attribute @1 :Text;
  value @2 :Value;
  tx @3 :UInt64;
  added @4 :Bool;
}

struct Value {
  union {
    text @0 :Text;
    int @1 :Int64;
    uint @2 :UInt64;
    float @3 :Float64;
    bool @4 :Bool;
    blob @5 :Data;
  }
}

struct Transaction {
  id @0 :UInt64;
  timestamp @1 :Int64;
  datoms @2 :List(Datom);
}
