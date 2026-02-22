#!/usr/bin/env mksh
set -o errexit

target=$1

targetDeps=$(nix-store --query --requisites $target)

for dep in $targetDeps; do
	if [[ -e $dep ]]; then
		name=$(basename $dep)
		ln -s $dep ~/builds/x86-64/bootstrap/$name
	else
		continue
	fi
done
