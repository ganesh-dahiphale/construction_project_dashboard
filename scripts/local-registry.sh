#!/usr/bin/env bash
set -euo pipefail

REGISTRY_NAME="registry"
REGISTRY_PORT="5000"

echo "Checking if local registry container '${REGISTRY_NAME}' is running on port ${REGISTRY_PORT}..."

if [ "$(docker ps -q -f name=^/${REGISTRY_NAME}$)" ]; then
    echo "Local registry '${REGISTRY_NAME}' is already running."
elif [ "$(docker ps -aq -f name=^/${REGISTRY_NAME}$)" ]; then
    echo "Local registry container exists but is stopped. Starting '${REGISTRY_NAME}'..."
    docker start "${REGISTRY_NAME}"
else
    echo "Starting fresh local registry container '${REGISTRY_NAME}' on port ${REGISTRY_PORT}..."
    docker run -d -p "${REGISTRY_PORT}:5000" --restart unless-stopped --name "${REGISTRY_NAME}" registry:2
fi

echo "Verifying local registry status..."
docker ps --filter "name=^/${REGISTRY_NAME}$"
curl -s "http://localhost:${REGISTRY_PORT}/v2/" || echo "Registry responded."
