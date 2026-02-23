# Mentci-Box Library Instructions

This library provides the core sandbox logic for the `mentci-box` executable and other Mentci-AI components.

## Objects
- `SandboxConfig`: Data object representing the sandbox configuration.
- `Sandbox`: Action object that executes a command within the configured isolation.

## Style
Strict adherence to Sema Object Style:
- Single Object In/Out.
- Direction encodes action (`from_args`, `to_bwrap_args`).
