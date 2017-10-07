#!/usr/bin/env bash

# Define script directory.
SCRIPTS_HOME=$(cd $(dirname "$0"); pwd)

libs=$(find $SCRIPTS_HOME/../libs -type f -name *.jar)

for lib in $libs
do

echo $lib

CP="${CP}:${lib}"

done

echo ${CP}

java -cp ${CP} org.apache.ignite.scenario.LoadCacheTask  -cfg $SCRIPTS_HOME/../config/ignite-remote-client-config.xml -c "atomic"
