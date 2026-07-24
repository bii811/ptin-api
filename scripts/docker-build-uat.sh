#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

IMAGE="ptin:uat"

docker build \
  --build-arg PROFILE=uat \
  -t "${IMAGE}" \
  -f "${REPO_ROOT}/Dockerfile" \
  "${REPO_ROOT}"

echo "Built ${IMAGE}"
