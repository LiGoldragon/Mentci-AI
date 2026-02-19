#!/usr/bin/env mksh
set -o errexit

targetHost="$1"
shift
remainingArgs="$@"

for KeyId in $(sd "\n" " " <~/.password-store/.gpg-id); do
	gpg --export --armor $KeyId >>~/pass-pubkeys.asc
done
