#!/bin/bash
set -euo pipefail

IMAGE="${1:-localhost:5000/construction-dashboard:broken-demo}"
TARGET_HOST="${2:-127.0.0.1}"
TARGET_USER="${3:-ganesh}"
KEY_PATH="${4:-$HOME/.ssh/cpd_ansible}"

DOCKER_BIN="docker"
if ! command -v docker >/dev/null 2>&1 || ! docker info >/dev/null 2>&1; then
    if command -v docker.exe >/dev/null 2>&1; then
        DOCKER_BIN="docker.exe"
    fi
fi

echo "==========================================================="
echo "Streaming broken demo image to target node via SSH..."
echo "Image:  ${IMAGE}"
echo "Binary: ${DOCKER_BIN}"
echo "Target: ${TARGET_USER}@${TARGET_HOST}"
echo "==========================================================="

"${DOCKER_BIN}" save "${IMAGE}" | ssh -o StrictHostKeyChecking=no -i "${KEY_PATH}" "${TARGET_USER}@${TARGET_HOST}" "sudo docker load"

echo "Successfully loaded ${IMAGE} into target Docker daemon."
