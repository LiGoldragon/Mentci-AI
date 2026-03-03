# RefusalFeedbackMessage Template

```edn
{:message :RefusalFeedbackMessage
 :targetRequestHash "<hash>"
 :reasons ["<reason>"]
 :suggestions ["<suggestion>"]
 :questions ["<question>"]
 :requiredEvidence ["<evidence-hash-or-description>"]
 :issuedBy "<role>"
 :policyHash "<policy-hash>"}
```

## Usage
Use this message for every refusal. Never return an empty denial.
