# Ouranos User-Space LiteLLM + Tailnet Routing Design

## Goal
Create a self-hosted OpenRouter-style routing surface for daily development by running a user-space LiteLLM gateway in the current `ouranos` home environment, routing across private Prometheus-hosted models and cloud fallbacks, while avoiding risky early CriomOS mutations on `ouranos`.

## Constraints
- Do **not** update CriomOS on `ouranos` until the design is validated end-to-end.
- If a future CriomOS update on `ouranos` is considered, ask the user first and prove it will not break the current main-branch state.
- First implementation target is the current Nix home-user activation flow for `crioZones.maisiliym.ouranos.hom.li.light`.
- `prometheus` remains the heavy inference node.
- Network access should prefer private tailnet transport.
- The gateway should expose a stable local OpenAI-compatible endpoint for tools.

## Architectural Objects

### 1. Inference Object: `PrometheusInferenceBackends`
Responsibilities:
- host heavyweight model backends
- provide primary reasoning and coding lanes
- remain private and reachable over tailnet

Initial lane mapping:
- `prometheus-deepseek` -> primary deliberate reasoning backend
- `prometheus-qwen` -> primary efficient coding/subagent backend
- optional later: `prometheus-fast` -> low-latency utility backend

### 2. Gateway Object: `OuranosUserGateway`
Responsibilities:
- run LiteLLM as a **user-space service** in the current `ouranos` home environment
- bind to localhost
- provide one stable OpenAI-compatible endpoint for local tools
- translate human aliases into backend/provider routes

This object is the first implementation target because it is reversible, low-risk, and does not require immediate system-level CriomOS changes.

### 3. Routing Policy Object: `ModelRoutingPolicy`
Responsibilities:
- define semantic aliases
- define backend aliases
- define ordered fallback behavior
- keep provider/model choices out of client configuration

#### Human-facing aliases
- `main` -> best deliberate reasoning lane
- `subagent` -> efficient coding/workhorse lane
- `fast` -> low-latency utility lane

#### Debug/backend aliases
- `prometheus-deepseek`
- `prometheus-qwen`
- `cloud-reasoning`
- `cloud-coder`
- `cloud-fast`
- optional later: `prometheus-fast`

#### Initial routing intent
- `main` -> prefer `prometheus-deepseek`, fallback `cloud-reasoning`
- `subagent` -> prefer `prometheus-qwen`, fallback `cloud-coder`
- `fast` -> initial implementation may prefer a cheaper/faster cloud lane or a smaller Prometheus lane once available

### 4. Client Surface Object: `ClientBindings`
Responsibilities:
- point `pi`, subagents, and related local tools to one stable local endpoint
- avoid direct provider-specific configuration in day-to-day workflows
- preserve the meaning of semantic lanes even if the underlying model fleet changes

## Network Architecture
Recommended transport:
- Tailnet/private connectivity between `ouranos` and `prometheus`
- Keep Prometheus-hosted model serving private
- Use LiteLLM on `ouranos` as the public-facing local developer API surface

Rationale:
- better daily ergonomics than ad-hoc SSH tunnels
- private by default
- supports future multi-device use
- keeps transport concerns separate from model policy concerns

## Why Not CriomOS-Manage `ouranos` Yet?
That is not yet proven necessary. The first milestone should validate:
1. Prometheus endpoint shape
2. tailnet reachability
3. LiteLLM config ergonomics
4. alias and fallback behavior
5. tool compatibility with the local gateway

Only after those are confirmed should a CriomOS-level `ouranos` integration be considered.

## First Milestone
Implement the following without changing system-level CriomOS on `ouranos`:
- user-space LiteLLM service in the home environment
- localhost-bound OpenAI-compatible endpoint
- semantic aliases: `main`, `subagent`, `fast`
- explicit backend aliases for debugging
- Prometheus-primary routing over tailnet
- ordered cloud fallback routing

## Verification Gates
Before any CriomOS mutation on `ouranos`, prove:
1. the local gateway endpoint works
2. `main`, `subagent`, and `fast` each resolve correctly
3. explicit backend aliases work for debugging
4. Prometheus-primary routing works over tailnet
5. cloud fallback activates when Prometheus is unavailable
6. the current local workflow (`pi` and related tooling) still behaves correctly

## Implementation Direction
Preferred rollout order:
1. establish LiteLLM in the `ouranos` home-user environment
2. configure semantic and backend aliases
3. verify Prometheus connectivity over tailnet
4. verify fallback behavior
5. bind local tools to the gateway
6. evaluate whether CriomOS should later absorb the service/config as a managed host capability

## Notes on Model Policy
The semantic contract should remain stable even if backend choices evolve:
- `main` means "best deliberate reasoning lane"
- `subagent` means "best efficient coding lane"
- `fast` means "cheap low-latency utility lane"

This prevents provider/model churn from leaking into client and agent configuration.
