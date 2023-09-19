#!/usr/bin/env bash

[ -e "$HOME/.bash_configs/bash_ruby" ] && source "$HOME/.bash_configs/bash_ruby"

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$SCRIPTDIR" || exit 1

cmd="default"
if [[ -n $1 ]]; then cmd=$1 ; fi

# shellcheck disable=SC2016
awkScriptToLocal='match($0, /^(# *)?gem "jekyll(.*# CREATE.SH) *$/, m)              { print "gem \"jekyll" m[2] }
                  match($0, /^(# *)?gem "github-pages"(.*# CREATE.SH) *$/, m)       { print "#gem \"github-pages\"" m[2] }
                  match($0, /^(# *)?baseurl: ?""(.*# CREATE.SH) *$/, m)             { print "baseurl: \"\"" m[2] }
                  match($0, /^(# *)?baseurl: ?"\/chassis"(.*# CREATE.SH) *$/, m)    { print "#baseurl: \"/chassis\"" m[2] }
                  match($0, /^(# *)?theme: bulma-clean-theme(.*# CREATE.SH) *$/, m) { print "theme: bulma-clean-theme" m[2] }
                  match($0, /^(# *)?remote_theme: chrisrhymes\/bulma-clean-theme(.*# CREATE.SH) *$/, m) { print "#remote_theme: chrisrhymes/bulma-clean-theme" m[2] }
                  match($0, /^(# *)?chassisassetsgit: ?"\/(.*# CREATE.SH) *$/, m)      { print "chassisassetsgit: \"/" m[2] }
                  match($0, /^(# *)?chassisassetsgit: ?"http(.*# CREATE.SH) *$/, m)    { print "#chassisassetsgit: \"http" m[2] }
                  !/# CREATE.SH *$/ { print $0 }
                 '

# shellcheck disable=SC2016
awkScriptToPages='match($0, /^(# *)?gem "jekyll(.*# CREATE.SH) *$/, m)              { print "#gem \"jekyll" m[2] }
                  match($0, /^(# *)?gem "github-pages"(.*# CREATE.SH) *$/, m)       { print "gem \"github-pages\"" m[2] }
                  match($0, /^(# *)?baseurl: ?""(.*# CREATE.SH) *$/, m)             { print "#baseurl: \"\"" m[2] }
                  match($0, /^(# *)?baseurl: ?"\/chassis"(.*# CREATE.SH) *$/, m)    { print "baseurl: \"/chassis\"" m[2] }
                  match($0, /^(# *)?theme: bulma-clean-theme(.*# CREATE.SH) *$/, m) { print "#theme: bulma-clean-theme" m[2] }
                  match($0, /^(# *)?remote_theme: chrisrhymes\/bulma-clean-theme(.*# CREATE.SH) *$/, m) { print "remote_theme: chrisrhymes/bulma-clean-theme" m[2] }
                  match($0, /^(# *)?chassisassetsgit: ?"\/(.*# CREATE.SH) *$/, m)      { print "#chassisassetsgit: \"/" m[2] }
                  match($0, /^(# *)?chassisassetsgit: ?"http(.*# CREATE.SH) *$/, m)    { print "chassisassetsgit: \"http" m[2] }
                  !/# CREATE.SH *$/ { print $0 }
                 '


if [[ $cmd == "default" || $cmd == "local" ]]; then
  gawk -i inplace "$awkScriptToLocal" "$SCRIPTDIR/_config.yml"
  gawk -i inplace "$awkScriptToLocal" "$SCRIPTDIR/Gemfile"

  if [[ $cmd == "local" ]]; then exit 0 ; fi

    ln -s ../../../drawiochassis/assets/imagebinary ./assets/
    #find _site/assets/imagebinary -type l -name '*drawio' -exec rm {} \;

    echo "./$(basename "$SCRIPTDIR")/${0##*/}"
    echo bundle exec jekyll serve "$@"
    bundle exec jekyll serve "$@"

    rm ./assets/imagebinary

  gawk -i inplace "$awkScriptToPages" "$SCRIPTDIR/_config.yml"
  gawk -i inplace "$awkScriptToPages" "$SCRIPTDIR/Gemfile"
  exit 0
fi

if [[ $cmd == "github" ]]; then
  gawk -i inplace "$awkScriptToPages" "$SCRIPTDIR/_config.yml"
  gawk -i inplace "$awkScriptToPages" "$SCRIPTDIR/Gemfile"
  exit 0
fi

if [[ $cmd == "install" ]]; then
  bundle install --redownload
  exit 0
fi
