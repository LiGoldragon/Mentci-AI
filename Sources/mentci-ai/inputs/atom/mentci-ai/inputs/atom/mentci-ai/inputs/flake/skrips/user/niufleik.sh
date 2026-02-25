#!/usr/bin/env mksh
set -o errexit

fleikPath=~/dev/$1

mkdir $fleikPath
cd $fleikPath
git init >/dev/null

cat >$fleikPath/flake.nix <<EOF
{
  description = "${1}";

  outputs = { self }: {
    SobUyrld = {
      modz = [ ];
      lamdy = import ./lamdy.nix
    };
  };
}
EOF

cat >$fleikPath/lamdy.nix <<EOF
{ ... }:
let

in
{
}
EOF

print Niu Fleik krieityd: $1
