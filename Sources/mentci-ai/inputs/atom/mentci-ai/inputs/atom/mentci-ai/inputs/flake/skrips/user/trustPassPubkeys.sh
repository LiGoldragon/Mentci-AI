#!/usr/bin/env mksh
set -o errexit

for KeyId in $(cat ~/.password-store/.gpg-id); do
	echo -e "5\ny\n" |
		gpg --command-fd 0 --edit-key "$KeyId" trust
	echo "Trusted GPG KeyID: $KeyId"
done
