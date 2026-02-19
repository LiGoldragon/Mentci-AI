#!/usr/bin/env mksh

set -o errexit

target="$1"
efi-size=256MiB
data-start = 257MiB

sgdisk --clear --mbrtogpt $target

sgdisk "--new=1:1MiB:"$efi-size $target
sgdisk --change-name=1:efiboot $target
sgdisk --part-type=1:EF00 $target

sgdisk "--new=2:"$data-start":-1MiB" $target
sgdisk --change-name=2:btrfs $target
sgdisk --part-type=2:8300 $target

mkfs.vfat $target"1"
