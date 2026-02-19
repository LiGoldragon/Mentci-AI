#!/usr/bin/env mksh
set -o errexit

targetHost="$1"
shift
remainingArgs="$@"

ssh root@$targetHost $remainingArgs
