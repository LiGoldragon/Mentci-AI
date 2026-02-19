#!/usr/bin/env mksh
set -o errexit

targetHost="$1"

ssh -t $targetHost -- 'export SHELL=zsh && exec zsh'
