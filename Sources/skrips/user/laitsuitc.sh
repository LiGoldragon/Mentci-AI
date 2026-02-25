#!/usr/bin/env mksh
set -o errexit

darkOrLight=$1
vimSokets=$(nvr --serverlist)

pkill -USR1 termite

for soket in $vimSokets; do
	(nvr --nostart --servername $soket +"set bg=$darkOrLight" &)
done
