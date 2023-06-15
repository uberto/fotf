#!/usr/bin/env bash
readonly BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# delete the volumes if they exists
docker volume rm pg-volume || true
# create fresh volumes
docker volume create pg-volume

(docker-compose --file ${BASE_DIR}/docker-compose-local-pg.yml up -d)

#if it cannot start network check if there are ghost containers with
# docker container ls -a
# and in case remove them with
# docker container rm xxxx

