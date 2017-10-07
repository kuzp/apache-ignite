#!/usr/bin/env bash

# Define script directory.
SCRIPTS_HOME=$(cd $(dirname "$0"); pwd)

source "${SCRIPTS_HOME}"/include/functions.sh

check_config $1

read_config $1

deploy_configs ${SERVER_HOSTS}

start_server_nodes ${SERVER_HOSTS}