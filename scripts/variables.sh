out="out"
in="examples"
build_dir="$dir/build/classes/java/main"
package="net/lisstem/taint"
in="examples"

red() {
  echo -e "\e[31m$*\e[0m"
}

green() {
  echo -e "\e[32m$*\e[0m"
}

echo_err() { echo "$@" 1>&2; }