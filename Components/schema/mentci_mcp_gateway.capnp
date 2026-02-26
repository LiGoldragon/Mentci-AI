@0xb1f8a972dd6b6b77;

struct ToolCallEnvelope {
  toolName @0 :Text;
  filePath @1 :Text;
  workflowPhase @2 :Text;
  requestId @3 :Text;
}

struct PolicyDecision {
  decision @0 :DecisionKind;
  code @1 :Text;
  reason @2 :Text;
}

enum DecisionKind {
  allow @0;
  warn @1;
  block @2;
}

struct ToolReviewRequest {
  requestId @0 :Text;
  toolCall @1 :ToolCallEnvelope;
  diffText @2 :Text;
  policyDecision @3 :PolicyDecision;
}

struct ToolReviewResponse {
  requestId @0 :Text;
  verdict @1 :DecisionKind;
  notes @2 :Text;
}
