#!/usr/bin/env mksh
set -o errexit

find -type l | while IFS= read -r lnk; do
	if readlink "$lnk" | grep -q '^/nix/store/'; then
		rm "$lnk"
	fi
done
