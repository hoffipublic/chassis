#!/usr/bin/env bash

[ -e "$HOME/.bash_configs/bash_ruby" ] && source "$HOME/.bash_configs/bash_ruby"

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$SCRIPTDIR" || exit 1

# shellcheck disable=SC2016
awkScript='/^#(.*)# CREATE.SH *$/ { print substr($0, 2) } /^[^#](.*)# CREATE.SH *$/ { print "#"$0 } !/# CREATE.SH *$/ { print $0 }'

gawk -i inplace "$awkScript" "$SCRIPTDIR/_config.yml"
gawk -i inplace "$awkScript" "$SCRIPTDIR/Gemfile"

bundle exec jekyll serve

gawk -i inplace "$awkScript" "$SCRIPTDIR/_config.yml"
gawk -i inplace "$awkScript" "$SCRIPTDIR/Gemfile"
