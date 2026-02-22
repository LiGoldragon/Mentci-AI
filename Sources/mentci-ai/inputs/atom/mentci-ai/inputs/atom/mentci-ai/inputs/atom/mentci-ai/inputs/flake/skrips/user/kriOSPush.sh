#!/usr/bin/env mksh
set -o errexit

KriOSDerivation=$(realpath --zero $1)
TargetHost=${2:-"localhost"}
SwitchFlag=${3:-"switch"}

if [[ ! -e $KriOSDerivation ]]; then
	echo "KriOspush error: No valid derivation given"
	exit 1
fi

[[ $TargetHost != "localhost" ]] &&
	nix copy --to ssh://root@$TargetHost $KriOSDerivation

ssh root@$TargetHost -- "kriOSActivate $KriOSDerivation $SwitchFlag"
