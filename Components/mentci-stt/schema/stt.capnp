@0xf4b2d18c7a13d2d9;

struct SttRequest {
  audioPath @0 :Text;
  vocabulary @1 :List(Text);
  
  # Provider Configuration
  providerUrl @2 :Text;
  apiKeySecretName @3 :Text;
  model @4 :Text;
  
  # Prompt Configuration
  basePrompt @5 :Text;
  includeEmotionalEmphasis @6 :Bool;
  emotionalEmphasisInstruction @7 :Text;
  
  # Audio Configuration
  mimeType @8 :Text;
  criticalPhoneticInstructions @9 :Text;
  transcriptDir @10 :Text;
  vocabularyPreambleTemplate @11 :Text;
  chronosFormat @12 :Text;
  chronosPrecision @13 :Text;
  transcriptFilenameTemplate @14 :Text;
}

struct SttResponse {
  transcription @0 :Text;
  timestamp @1 :Text;
}
