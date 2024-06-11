#!/bin/bash
dir=$( dirname -- "$( readlink -f -- "$0"; )"; )
resultpath="$dir/build/reports/jmh/"
resultfile=results.txt
cd "$dir" || exit 1
approaches=( none box shadow )
for approach in "${approaches[@]}"
do
  echo "approach: $approach"
  ./gradlew jmh "-Pargs=$approach" && mv "$resultpath$resultfile" "$resultpath$approach.txt"
done
