#!/usr/bin/env mksh
set -o errexit

targetHost="$1"
shift
targetIuzyr="${1:-root}"
shift
opcynylArgz="${@:+"-c $@"}"
zshEksek=~/.nix-profile/bin/zsh

fainylKymend="export SHELL=$zshEksek && exec $zshEksek $opcynylArgz"

ssh -t -i ~/.ssh/root root@$targetHost -- $fainylKymend
