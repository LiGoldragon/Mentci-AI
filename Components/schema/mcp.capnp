@0xa381cff3b4dbe5f8;

struct McpConfig {
  serverName @0 :Text;
  serverVersion @1 :Text;
  tools @2 :List(McpToolDef);
}

struct McpToolDef {
  name @0 :Text;
  description @1 :Text;
  inputSchemaJson @2 :Text; # The JSON Schema string required by MCP
}

struct StructuralEditRequest {
  filePath @0 :Text;
  language @1 :Text;
  
  # Pattern to search for
  pattern @2 :Text;
  
  # Transformation/Replacement code
  rewrite @3 :Text;
  
  # Optional: specific rule or filter
  rule @4 :Text;
}

struct StructuralEditResponse {
  success @0 :Bool;
  message @1 :Text;
  diff @2 :Text;
}
