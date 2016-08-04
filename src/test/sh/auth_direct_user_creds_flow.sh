#!/usr/bin/env bash
##get token
tokens=(`curl --silent -i -k -G -X POST \
  -H "Authorization: Basic dXNlcl9jcmVkOnN1cGVyc2VjcmV0" \
  -H "Content-Type:application/x-www-form-urlencoded" \
  -d "username=vdanyliuk" \
  -d "password=cec24fdc" \
  -d "grant_type=password" \
  https://localhost:8111/oauth/token \
  | grep -Po "((?<=access_token\":\")[^\"]+)|((?<=refresh_token\":\")[^\"]+)"`)

echo "ACCESS_TOKEN: "${tokens[0]}
echo "REFRESH_TOKEN: "${tokens[1]}
