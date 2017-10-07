#!/usr/bin/env bash

# Define script directory.
SCRIPTS_HOME=$(cd $(dirname "$0"); pwd)

source "${SCRIPTS_HOME}"/include/functions.sh

check_properties $1

read_properties $1

start_client_nodes ${CLIENT_HOSTS}