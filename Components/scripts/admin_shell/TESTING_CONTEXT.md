# Testing Context: admin_shell

## Coverage State
- CLI path and runtime behavior are validated manually through `mentci-admin-shell`.
- Rust bootstrap behavior is covered by `Components/tests/mentci_bootstrap.rs`.

## Verification Commands
- `nix develop -c mentci-admin-shell --cmd 'pwd && test -d .jj && echo ok'`
- `cargo test --manifest-path Components/Cargo.toml --test mentci_bootstrap`

## Mocking Strategy
- No external service mocks.
- Uses real local `jj` repository state and Bubblewrap jail wrapper (`mentci-jail-run`).
