#!/bin/bash
dir=$( dirname -- "$( readlink -f -- "$0"; )"; )
source "$dir/scripts/variables.sh"

cd "$build_dir" || exit 1
test_log="$package/$in/$1"
ret=1
if untainted=$(java "$package/$in/$1" 2>&1); then
  cd "$dir/$out" || exit 1
  if tainted=$(java "$package/$in/$1" 2>&1); then
    if [[ $untainted = "$tainted" ]]; then
      ret=0
    fi
  fi
  echo "$tainted" > "$test_log".tainted.log
fi
cd "$dir/$out" || exit 1
echo "$untainted" > "$test_log".untainted.log

if [[ $ret -eq 0 ]]; then
  green "Test $1: success"
else
  echo_err "$(red "Test $1: failure")"
fi

exit $ret