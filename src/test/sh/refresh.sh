#!/usr/bin/env bash
curl --silent -i -k -G -X POST \
    -H "Authorization: Basic dXNlcl9jcmVkOnN1cGVyc2VjcmV0" \
    -d "refresh_token={REFRESH_TOKEN_VALUE}" \
    -d "grant_type=refresh_token" \
    https://sjcoins.testing.softjourn.if.ua/auth/oauth/token