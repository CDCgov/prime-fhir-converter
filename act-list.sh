#!/bin/bash
set -e

# Detect system architecture and set appropriate flags for act
ARCH=$(uname -m)
ACT_FLAGS=""

# On ARM64 (Apple Silicon or other ARM64 systems), use linux/amd64 containers for compatibility
if [[ "$ARCH" == "arm64" || "$ARCH" == "aarch64" ]]; then
    echo "Detected ARM64 architecture ($ARCH)"
    echo "Using --container-architecture linux/amd64 for compatibility"
    ACT_FLAGS="--container-architecture linux/amd64"
else
    echo "Detected $ARCH architecture"
fi

# Check if act is installed
if ! command -v act &> /dev/null; then
    echo "Error: 'act' is not installed or not in PATH"
    echo "Install from: https://github.com/nektos/act"
    echo ""
    echo "macOS: brew install act"
    echo "Linux: Use releases from GitHub"
    exit 1
fi

echo "Running act --list for build-and-publish.yml workflow..."
echo ""

# Run act with appropriate flags
act --list --workflows .github/workflows/build-and-publish.yml $ACT_FLAGS
exit 0
