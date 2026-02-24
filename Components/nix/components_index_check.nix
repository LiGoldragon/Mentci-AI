{ pkgs, repo_root }:

pkgs.runCommand "components-index-check" { } ''
  cd ${repo_root}

  mapfile -t index_paths < <(sed -n 's/.*:path "\(.*\)".*/\1/p' Components/index.edn | sort -u)
  mapfile -t fs_paths < <(find Components -mindepth 1 -maxdepth 1 -type d -printf '%f\n' | sort -u)

  printf '%s\n' "Index paths:"
  printf '%s\n' "''${index_paths[@]}"
  printf '%s\n' "Filesystem paths:"
  printf '%s\n' "''${fs_paths[@]}"

  if [ "''${#index_paths[@]}" -eq 0 ]; then
    echo "Components/index.edn has no :path entries" >&2
    exit 1
  fi

  missing_in_fs=0
  for p in "''${index_paths[@]}"; do
    if [ ! -d "Components/$p" ]; then
      echo "Missing component directory for index path: $p" >&2
      missing_in_fs=1
    fi
  done

  missing_in_index=0
  for p in "''${fs_paths[@]}"; do
    if ! printf '%s\n' "''${index_paths[@]}" | grep -qx "$p"; then
      echo "Missing index entry for component directory: $p" >&2
      missing_in_index=1
    fi
  done

  if [ "$missing_in_fs" -ne 0 ] || [ "$missing_in_index" -ne 0 ]; then
    exit 1
  fi

  touch "$out"
''
