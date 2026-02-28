@0xabcdef1234567891;

# This schema defines the structured, content-addressed "Commit" for the
# Fractal DVCS architecture.

struct MentciCommit {
  # The cryptographic hash of the direct parent archive (the ancestor tree).
  # This points to a single-straight-trunk clone archive.
  parentArchiveHash @0 :Text;
  
  # The URI or local path where the parent archive can be retrieved.
  parentArchiveLocator @1 :Text;

  # Semantic metadata for the LLM / human operator.
  title @2 :Text;
  prompt @3 :Text;
  context @4 :Text;
  summary @5 :Text;
  
  # The allowed branch or "DVCS Variable Spacename" this repository owns.
  ownedSpacename @6 :Text;

  # Additional cryptographically verifiable signatures.
  signature @7 :Text;
}
