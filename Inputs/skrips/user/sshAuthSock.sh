#!/usr/bin/env mksh

if [ -z "$SSH_AUTH_SOCK" ]; then
	export SSH_AUTH_SOCK=$(~/.nix-profile/bin/gpgconf --list-dirs agent-ssh-socket)
fi
