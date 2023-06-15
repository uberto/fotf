#!/usr/bin/env bash
readonly BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

(docker-compose --file ${BASE_DIR}/docker-compose-local-pg.yml down)
