@0x9a8b7c6d5e4f3a2c;

# Criome Core Initialization & Action Protocol
# This contract establishes the root identity of an Agent or Component within the Criome architecture.
# It uses a short Content-Addressed (CA) hash of its own version/structure (via BLAKE3) to ensure 
# components can interact safely even when developed in parallel branches.

struct CriomeIdentity {
  # The BLAKE3 256-bit hash (represented as 32 bytes or 64 hex chars).
  # BLAKE3 is the chosen short CA hash due to its exceptional speed, tree-hashing mode, 
  # and verified streaming capabilities via Bao.
  blake3Hash @0 :Text;
  
  # A readable namespace/name for the component.
  namespace @1 :Text;
}

struct InitMessage {
  identity @0 :CriomeIdentity;
  
  # Arbitrary initialization payload, specific to the component.
  payload @1 :AnyPointer;
}

interface ActionAgentChannel {
  # Submits an action request (e.g. jj commit, build) to the Action Agent.
  # The Action Agent operates on the physical layer (filesystem, process)
  # while the primary agent stays in the logical plane.
  executeAction @0 (intent :Text, workingDir :Text) -> (result :ActionResult);

  struct ActionResult {
    success @0 :Bool;
    logs @1 :Text;
    blake3StateHash @2 :Text; # The new state hash after action execution
  }
}
