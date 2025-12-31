#!/usr/bin/env bash
set -euo pipefail
java -jar dist/ecommerce-strategy-lab.jar --server.port=${1:-8083}
