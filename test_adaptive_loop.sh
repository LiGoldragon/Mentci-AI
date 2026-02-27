#!/usr/bin/env bash
set -e

echo "=== 1. Agent calls update_skill_heuristics (Learner MCP) ==="
cat << 'RPC' > test-rpc.json
{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"update_skill_heuristics","arguments":{"skillName":"sema-programmer","condition":"structural_edit fails on module macros","resolution":"Use text edit tool as fallback for #[allow(...)]","context":"Verified during rust warning cleanup."}}}
RPC
target/debug/mentci-mcp < test-rpc.json > rpc-out.json

echo
echo "=== 2. Check Tier 3: runtime.edn ==="
cat .pi/skills/sema-programmer/runtime.edn

echo
echo "=== 3. Compile Absolute Rules (Policy Engine) ==="
cargo run --manifest-path Components/mentci-policy-engine/Cargo.toml --bin compile_rules > /dev/null

echo "=== 4. Check Tier 2: absolute_rules.md ==="
tail -n 12 .pi/skills/sema-programmer/absolute_rules.md

echo
echo "=== 5. Cleanup ==="
rm .pi/skills/sema-programmer/runtime.edn .pi/skills/*/absolute_rules.md test-rpc.json rpc-out.json
echo "SUCCESS: Adaptive loop complete."
