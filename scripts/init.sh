#!/bin/bash

cd /opt/mixchat
git init
git remote add origin https://github.com/prgrms-aibe-devcourse/AIBE3_final_project_team3_BE
git fetch origin
git checkout -f -t origin/release

curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.34.0/install.sh | bash
. ~/.nvm/nvm.sh
nvm install --lts
cd /opt/mixchat-fe
git remote add origin https://github.com/prgrms-aibe-devcourse/AIBE3_final_project_team3_FE