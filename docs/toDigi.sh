#!/usr/bin/env bash

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$SCRIPTDIR" || exit 1

# sudo vi /etc/nginx/sites-enabled/hoffimuc.com
# root /var/www/hoffimuc.com/_site;
# sudo nginx -s reload
rsync --progress -avhe ssh _site hoffi@$DIGIIP:/var/www/hoffimuc.com/

if [[ $? -eq 0 ]]; then echo "SUCCESS" ; else echo "FAILED" ; fi
