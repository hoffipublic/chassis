#!/usr/bin/env bash

[ -e "$HOME/.bash_configs/bash_ruby" ] && source "$HOME/.bash_configs/bash_ruby"

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$SCRIPTDIR" || exit 1

bundle exec jekyll serve
