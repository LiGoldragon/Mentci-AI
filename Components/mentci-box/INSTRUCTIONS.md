# Mentci-Box Bootstrapping Instructions

`mentci-box` is a standalone, high-reliability Rust binary for sandboxed execution in the Mentci-AI ecosystem.

## Usage
Run with standard sandbox arguments followed by `--` and the command to execute:

```bash
mentci-box --workdir . -- /bin/ls -la
```

## Role in Bootstrapping
Because `mentci-box` has minimal dependencies (it only requires `bwrap` in the host environment), it can be used to bootstrap more complex environments (like the Nix Jail) before the full `mentci-aid` or other heavy tools are available or rebuilt.

## Development
- Logic lives in `Components/mentci-box-lib/`.
- Executable lives in `Components/mentci-box/`.
- Follow Sema Object Style for any new additions.
