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

java -cp ${CP} org.apache.ignite.scenario.${MAIN_CLASS} $SCRIPT_HOME/$1 $0
