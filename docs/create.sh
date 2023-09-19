#!/usr/bin/env bash

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$SCRIPTDIR" || exit 1

cmd="default"
if [[ -n $1 && $1 =~ ^default|local|github|install|-h|--help|help$ ]]; then cmd=$1 ; shift ; fi
if [[ $cmd =~ ^-h|--help|help$ ]]; then
  echo "synopsis: ${0##*/} [local|github|install|-h|--help|help]"
  exit 0
fi

echo "./$(basename "$SCRIPTDIR")/${0##*/}"
echo "CWD=${SCRIPTDIR}"

# shellcheck disable=SC2016
awkScriptToLocal='match($0, /^(# *)?gem "jekyll(.*# CREATE.SH) *$/, m)              { print "gem \"jekyll" m[2] }
                  match($0, /^(# *)?gem "github-pages"(.*# CREATE.SH) *$/, m)       { print "#gem \"github-pages\"" m[2] }
                  match($0, /^(# *)?baseurl: ?""(.*# CREATE.SH) *$/, m)             { print "baseurl: \"\"" m[2] }
                  match($0, /^(# *)?baseurl: ?"\/chassis"(.*# CREATE.SH) *$/, m)    { print "#baseurl: \"/chassis\"" m[2] }
                  match($0, /^(# *)?theme: bulma-clean-theme(.*# CREATE.SH) *$/, m) { print "theme: bulma-clean-theme" m[2] }
                  match($0, /^(# *)?remote_theme: chrisrhymes\/bulma-clean-theme(.*# CREATE.SH) *$/, m) { print "#remote_theme: chrisrhymes/bulma-clean-theme" m[2] }
                  match($0, /^(# *)?chassisassetsgit: ?"\/(.*# CREATE.SH) *$/, m)   { print "chassisassetsgit: \"/" m[2] }
                  match($0, /^(# *)?chassisassetsgit: ?"http(.*# CREATE.SH) *$/, m) { print "#chassisassetsgit: \"http" m[2] }
                  !/# CREATE.SH *$/ { print $0 }
                 '

# shellcheck disable=SC2016
awkScriptToPages='match($0, /^(# *)?gem "jekyll(.*# CREATE.SH) *$/, m)              { print "#gem \"jekyll" m[2] }
                  match($0, /^(# *)?gem "github-pages"(.*# CREATE.SH) *$/, m)       { print "gem \"github-pages\"" m[2] }
                  match($0, /^(# *)?baseurl: ?""(.*# CREATE.SH) *$/, m)             { print "#baseurl: \"\"" m[2] }
                  match($0, /^(# *)?baseurl: ?"\/chassis"(.*# CREATE.SH) *$/, m)    { print "baseurl: \"/chassis\"" m[2] }
                  match($0, /^(# *)?theme: bulma-clean-theme(.*# CREATE.SH) *$/, m) { print "#theme: bulma-clean-theme" m[2] }
                  match($0, /^(# *)?remote_theme: chrisrhymes\/bulma-clean-theme(.*# CREATE.SH) *$/, m) { print "remote_theme: chrisrhymes/bulma-clean-theme" m[2] }
                  match($0, /^(# *)?chassisassetsgit: ?"\/(.*# CREATE.SH) *$/, m)   { print "#chassisassetsgit: \"/" m[2] }
                  match($0, /^(# *)?chassisassetsgit: ?"http(.*# CREATE.SH) *$/, m) { print "chassisassetsgit: \"http" m[2] }
                  !/# CREATE.SH *$/ { print $0 }
                 '

# don't forget to do source ~/.bash_configs/bash_ruby

function main() {
  if [[ $cmd == "default" || $cmd == "local" ]]; then
    echo "switching _config.yml to local jekyll values"
    gawk -i inplace "$awkScriptToLocal" "$SCRIPTDIR/_config.yml"
    echo "switching Gemfile     to local jekyll values"
    gawk -i inplace "$awkScriptToLocal" "$SCRIPTDIR/Gemfile"

    if [[ $cmd == "local" ]]; then return 0 ; fi

    echo "generating png and svg from *.drawio files in ../../drawiochassis/assets" ...
    drawio "../../drawiochassis/assets"

    # as we softlink assets/imagebinary
    # to have the *.png|*svg files in the linked (local) git repo
    # (don't forget to do: source ~/.bash_configs/bash_drawio && drawio
    #  in the drawiochassis git repo)
    # the image binaries will be copied over on exec jekyll serve
    # so when we rsync/scp the whole _site dir to our remote webserver
    # the image binaries will also be copied over
    echo "ln -s ../../../drawiochassis/assets/imagebinary ./assets/"
    ln -s ../../../drawiochassis/assets/imagebinary ./assets/
    # the (back) softlinks of drawiochassis git repo to this repo cause trouble for jekyll so we remove them from the generated _site
    echo "find _site/assets/imagebinary -type l -name '*drawio' -exec rm {} \;"
    find _site/assets/imagebinary -type l -name '*drawio' -exec rm {} \;

    echo bundle exec jekyll serve "$@"
    bundle exec jekyll serve "$@"

    echo
    echo "cleanup:"
    echo "rm ./assets/imagebinary # local softlink created above"
    rm ./assets/imagebinary

    echo "switching _config.yml back to github-pages jekyll values"
    gawk -i inplace "$awkScriptToPages" "$SCRIPTDIR/_config.yml"
    echo "switching Gemfile     back to github-pages jekyll values"
    gawk -i inplace "$awkScriptToPages" "$SCRIPTDIR/Gemfile"

    # the (back) softlinks of drawiochassis git repo to this repo cause trouble for jekyll so we remove them from the generated _site
    # here also, so that we don't rsync/scp it to somewhere it shouldn't be (e.g. a webserver)
    echo "find _site/assets/imagebinary -type l -name '*drawio' -exec rm {} \;"
    find _site/assets/imagebinary -type l -name '*drawio' -exec rm {} \;
    return 0
  fi

  if [[ $cmd == "github" ]]; then
    gawk -i inplace "$awkScriptToPages" "$SCRIPTDIR/_config.yml"
    gawk -i inplace "$awkScriptToPages" "$SCRIPTDIR/Gemfile"
    return 0
  fi

  if [[ $cmd == "install" ]]; then
    echo bundle install "$@"
    bundle install "$@"
    return 0
  fi
} # main

# check for valid ruby on MacOS
if [[ $OSTYPE == 'darwin'* && $(which ruby) == "/usr/bin/ruby" ]]; then
  if [[ -e "$HOME/.bash_configs/bash_ruby" ]]; then
    # shellcheck disable=SC2088
    echo source "~/.bash_configs/bash_ruby # NOT using the MacOs system ruby"
    source "$HOME/.bash_configs/bash_ruby"
  else
    echo "MacOs system ruby version not suitable for jekyll" >&2
    return 1
  fi
fi

#export DRAWIOEXE="/mnt/c/Program Files/draw.io/draw.io.exe"
export DRAWIOEXE="/Applications/draw.io.app/Contents/MacOS/draw.io"

function drawio() {
  local chassisassetsgit="$1"
  mapfile -d $'\0' paths < <(find -L "$chassisassetsgit" -type d \( -name public -o -name tmp -o -name resources -o -name vendor -o -name node_modules -o -name dist \) -prune \
      -o -type f -name '*.drawio' -print0)
  for file in "${paths[@]}"; do
      "$DRAWIOEXE" "$file" --crop --export --format png --embed-diagram --output "${file}.png" | awk '{m=match($0, $2); print "      " substr($0,m)}'
      "$DRAWIOEXE" "$file" --crop --export --format svg --embed-diagram --output "${file}.svg" | awk '{m=match($0, $2); print "      " substr($0,m)}'
  done
}

main "$@"
