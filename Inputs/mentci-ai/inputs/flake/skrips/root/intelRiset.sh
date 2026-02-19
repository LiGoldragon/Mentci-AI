#!/usr/bin/env mksh
set -o errexit

for CPU in /sys/devices/system/cpu/cpu[0-9]*; do
	[[ -e $CPU/online ]] && echo "1" >$CPU/online
	CPUID=$(basename $CPU | cut -b4-)
	echo -en "CPU: $CPUID\t"
	[[ -e $CPU/online ]] && echo "1" >$CPU/online
	THREAD1=$(cat $CPU/topology/thread_siblings_list | cut -f2 -d-)
	if [[ $CPUID = $THREAD1 ]]; then
		echo "-> disable"
		[[ -e $CPU/online ]] && echo "0" >$CPU/online
	else
		echo "-> enable"
		[[ -e $CPU/online ]] && echo "1" >$CPU/online
	fi
done
