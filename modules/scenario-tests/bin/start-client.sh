#!/usr/bin/env bash

# Define script directory.
SCRIPTS_HOME=$(cd $(dirname "$0"); pwd)

source "${SCRIPTS_HOME}"/include/functions.sh

check_properties $1

read_properties $1

libs=$(find $SCRIPTS_HOME/../libs -type f -name *.jar)

for lib in $libs; do

    CP="${CP}:${lib}"

done

java -cp ${CP} org.apache.ignite.scenario.${MAIN_CLASS} -cfg $SCRIPTS_HOME/../config/ignite-remote-client-config.xml -c ${CACHE}
