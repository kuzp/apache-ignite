#!/usr/bin/env bash

# Define script directory.
SCRIPT_HOME=$(cd $(dirname "$0"); pwd)

source "${SCRIPT_HOME}"/include/main-functions.sh

check_properties $1

read_properties $1

deploy_to_hosts "${SERVER_HOSTS},${CLIENT_HOSTS}"
