#!/bin/bash
dir=$( dirname -- "$( readlink -f -- "$0"; )"; )
source "$dir/scripts/variables.sh"

if [ $# -eq 0 ]; then
  adapter=shadow
else
  adapter=$1
fi

shopt -s globstar

out_path="$dir/$out/$package/$in/"
cd "$dir" || exit 1
echo "Tainting examples..."
rm "$out_path"**/*.*
if ./gradlew TaintExamples "-Pargs=$dir $in $out $adapter"; then
  echo -e "\nRunning tests..."
  count=0
  success=0
  failure=0
  for file in "$out_path"**/*.class; do
    if [ -f "$file" ]; then
      ((count+=1))
      rel=$(realpath --relative-to="$out_path" "$file")
      if ./test_example.sh "${rel%.*}"; then
        ((success+=1))
      else
        ((failure+=1))
      fi
    fi
  done

  if [[ $failure -eq 0 ]]; then
    echo -e "\nResult: $(green "$count tests, $success succeeded, $failure failed")"
  else
    echo -e "\nResult: $(red "$count tests, $success succeeded, $failure failed")"
  fi
else
  exit $?
fi