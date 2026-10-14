#!/usr/bin/env bash
# ==============================================================================
# Workstation Deployment Script: Deploy Release to Target Node via Ansible
# Project: Construction Progress Dashboard
# Milestone: Week 15 Final End-to-End Release
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${WORKSPACE_ROOT}"

# Default parameters
INVENTORY="${INVENTORY:-ansible/inventory/hosts.yml}"
IMAGE_REPO="${IMAGE_REPO:-localhost:5000/construction-dashboard}"
TARGET_HOST="${TARGET_HOST:-127.0.0.1}"
TARGET_PORT="${TARGET_PORT:-8082}"

# Determine Version and Release Tag if not passed as argument
if [ $# -ge 1 ] && [ -n "$1" ]; then
    RELEASE_TAG="$1"
else
    APP_VERSION=$(mvn -q -DforceStdout help:evaluate -Dexpression=project.version 2>/dev/null || grep -m1 '<version>' pom.xml | sed -E 's/.*<version>(.*)<\/version>/\1/' | tr -d ' ')
    SHORT_SHA=$(git rev-parse --short=7 HEAD 2>/dev/null || echo "manual")
    RELEASE_TAG="${APP_VERSION}-b${BUILD_NUMBER:-1}-${SHORT_SHA}"
fi

echo "================================================================================"
echo "DEPLOYING RELEASE VIA ANSIBLE WORKSTATION RUNNER"
echo "================================================================================"
echo "Workspace:    ${WORKSPACE_ROOT}"
echo "Release Tag:  ${RELEASE_TAG}"
echo "Image Repo:   ${IMAGE_REPO}"
echo "Inventory:    ${INVENTORY}"
echo "Target Host:  ${TARGET_HOST}:${TARGET_PORT}"
echo "================================================================================"

# Ensure host key checking is disabled for local test lab
export ANSIBLE_HOST_KEY_CHECKING=False

# 1. Run Ansible Provision & Deployment Playbook
echo ""
echo ">>> Executing Ansible Provision and Deploy Playbook..."
ansible-playbook -i "${INVENTORY}" ansible/playbooks/provision.yml \
    -e "release_tag=${RELEASE_TAG}" \
    -e "docker_image=${IMAGE_REPO}" | tee ansible-deploy.log

# 2. Run Ansible Health Check Playbook
echo ""
echo ">>> Executing Ansible Health Check Playbook..."
ansible-playbook -i "${INVENTORY}" ansible/playbooks/healthcheck.yml | tee ansible-healthcheck.log

# 3. Workstation HTTP Verification
echo ""
echo ">>> Performing HTTP Verification from Workstation..."
HEALTH_URL="http://${TARGET_HOST}:${TARGET_PORT}/health"
LOGIN_URL="http://${TARGET_HOST}:${TARGET_PORT}/login"

echo "Checking ${HEALTH_URL}..."
HEALTH_RESP=$(curl -s -m 5 "${HEALTH_URL}" || echo "")
echo "Health Response: ${HEALTH_RESP}"

if echo "${HEALTH_RESP}" | grep -q "UP"; then
    echo "✓ Health Check Endpoint returned UP"
else
    echo "✗ ERROR: Health Check failed at ${HEALTH_URL}"
    exit 1
fi

echo "Checking ${LOGIN_URL} for release tag ${RELEASE_TAG}..."
LOGIN_HTML=$(curl -s -m 5 "${LOGIN_URL}" || echo "")
if echo "${LOGIN_HTML}" | grep -q "${RELEASE_TAG}"; then
    echo "✓ Login page contains expected release version tag '${RELEASE_TAG}'"
else
    echo "✗ WARNING: Release tag '${RELEASE_TAG}' not found in login page HTML"
fi

# 4. Generate deploy-info.txt
COMMIT_SHA=$(git rev-parse --short=7 HEAD 2>/dev/null || echo "unknown")
cat <<EOF > deploy-info.txt
Deployment Information - Week 15 Final Release
-----------------------------------------------------------
Application Name: Construction Progress Dashboard
Deploy Target:    ansible
Registry Mode:    ${IMAGE_REPO}
Image Repository: ${IMAGE_REPO}
Image Tag:        ${RELEASE_TAG}
Git Commit:       ${COMMIT_SHA}
Target Node URL:  http://${TARGET_HOST}:${TARGET_PORT}
Health URL:       ${HEALTH_URL}
Timestamp:        $(date -u +"%Y-%m-%d %H:%M:%S UTC")
EOF

echo ""
echo "================================================================================"
echo "DEPLOYMENT AND VERIFICATION COMPLETED SUCCESSFULLY!"
echo "Deploy info written to deploy-info.txt"
echo "================================================================================"
