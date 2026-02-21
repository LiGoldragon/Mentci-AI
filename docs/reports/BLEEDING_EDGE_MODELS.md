# Bleeding-Edge Open Models for Level 5/6 Coding (Feb 2026)

This document tracks models that surpass standard flagship performance for repository-scale reasoning and autonomous manifestation.

## 1. The "Ultimate Agentic" Tier
These models are specifically tuned for **Agentic Loops** and **SWE-Bench** performance (solving real-world GitHub issues).

### **Qwen3-Coder-480B-A35B-Instruct** (Alibaba)
- **Status:** Beta / Open Weights
- **Architecture:** 480B Total / 35B Active (MoE)
- **Primary Strength:** **Repository-Scale Synthesis.** Specifically trained to reason across 1M+ token context windows without performance degradation.
- **Why for Mentci:** Best for the "Dark Factory" implementation where the agent needs to understand the entire ecosystem (Criomos + Sema + Mentci).

### **Devstral 2** (Mistral AI)
- **Status:** Released Feb 2026
- **Architecture:** 123B Parameters
- **Primary Strength:** **Local Agentic Excellence.** Achieving 72.2% on SWE-Bench Verified, it is optimized to run on single-node high-end consumer hardware (RTX 4090s).
- **Why for Mentci:** Ideal for high-authority "Admin Mode" where local inference is preferred for security.

## 2. The "Infinite Context" Tier
For Level 6 Symbolic Interaction, context length is the primary constraint.

### **Llama 4 Scout** (Meta)
- **Status:** Stable / Open Weights
- **Context Window:** **10 Million Tokens.**
- **Primary Strength:** **Universal codebase ingestion.** Can hold multiple large repositories in its active attention span simultaneously.
- **Why for Mentci:** Essential for mapping cross-repo topological constraints.

## 3. The "Symbolic/Swarm" Tier
Models experimenting with new interaction paradigms.

### **Kimi K2.5** (Moonshot AI)
- **Special Feature:** **Native Agent Swarm Mode.**
- **Capability:** The model has native logic for decomposing tasks into sub-agents, aligning perfectly with our Level 4 Roadmap.

### **GPT-OSS 120B** (OpenAI)
- **Status:** Surprise Release (Early 2026)
- **Primary Strength:** **Reasoning Purity.** A rare open-weight release from OpenAI focused on "Tool-Use Correctness" and zero-shot architectural planning.

## Recommended Mentci-AI Stack (Bleeding Edge)
- **Director Agent:** DeepSeek-V4 (for cost-efficient planning).
- **Implementation Worker:** Qwen3-Coder-480B (for repository-level code manifestation).
- **Long-Term Auditor:** Llama 4 Scout (for 10M token consistency checks).

---
*Updated: ♓︎ 1° 28' 44" | 5919 AM (v0.12.1.28.44)*
