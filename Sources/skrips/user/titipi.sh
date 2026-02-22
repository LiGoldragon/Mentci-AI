#!/usr/bin/env mksh
set -o errexit

path="$1"
shift
remainingArgs="$@"

wget --recursive \
	--continue \
	--page-requisites \
	--adjust-extension \
	--no-host-directories \
	--convert-links \
	--execute robots=off --wait=1 \
	--reject="*.js" \
	--level inf \
	--no-parent $path \
	$remainingArgs
