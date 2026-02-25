#!/usr/bin/env mksh
set -o errexit

echo "KriOSActivate:"
KriOSDerivation=$(realpath --zero $1)
echo "KriOSDerivation: $KriOSDerivation"
SwitchFlag=${2:-"switch"}
echo "SwitchFlag: $SwitchFlag"

nix-env --profile /nix/var/nix/profiles/system --set $KriOSDerivation

$KriOSDerivation/bin/switch-to-configuration $SwitchFlag
