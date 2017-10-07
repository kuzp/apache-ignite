#!/usr/bin/env bash

# Define script directory.
SCRIPT_HOME=$(cd $(dirname "$0"); pwd)

source "${SCRIPT_HOME}"/include/main-functions.sh

check_properties $1

read_properties $1

libs=$(find $SCRIPT_HOME/../libs -type f -name *.jar)

for lib in $libs; do

    CP="${CP}:${lib}"

done

java -cp ${CP} org.apache.ignite.scenario.${MAIN_CLASS} -cfg $SCRIPT_HOME/../config/ignite-remote-client-config.xml -c ${CACHE_NAME} -s ${DATASET_SIZE} -f ${FIELDS_PER_ENTRY} -fs ${FIELD_SIZE}
