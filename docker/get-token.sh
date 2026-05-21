#!/usr/bin/env bash
# Fetch an access token from the local Keycloak realm via the password grant.
# Used by the README quickstart so a curl call against the API is one pipe away.
#
# WARNING: this script targets the LOCAL non-production Keycloak. The seeded
# users ("admin"/"admin", "user"/"user") and the client secret embedded here
# live only in docker/keycloak/realm-export.json. They MUST NOT be used in any
# real environment.
#
# Usage:
#   docker/get-token.sh <username> <password>
#   TOKEN=$(docker/get-token.sh admin admin)
#   curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/examples
set -euo pipefail

if [ $# -ne 2 ]; then
  echo "usage: $0 <username> <password>" >&2
  exit 64
fi

USERNAME="$1"
PASSWORD="$2"

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:9080}"
REALM="${KEYCLOAK_REALM:-generic}"
CLIENT_ID="${KEYCLOAK_CLIENT_ID:-generic-spring-service}"
CLIENT_SECRET="${KEYCLOAK_CLIENT_SECRET:-generic-spring-service-secret}"

response="$(curl -sS -f \
  -X POST "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "grant_type=password" \
  --data-urlencode "client_id=${CLIENT_ID}" \
  --data-urlencode "client_secret=${CLIENT_SECRET}" \
  --data-urlencode "username=${USERNAME}" \
  --data-urlencode "password=${PASSWORD}")"

if command -v jq >/dev/null 2>&1; then
  printf '%s\n' "$response" | jq -r '.access_token'
else
  printf '%s\n' "$response" \
    | sed -n 's/.*"access_token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p'
fi
