#!/usr/bin/env bash

SCRIPT_DIR=$(cd $(dirname "$0"); pwd)

libs=$(find $SCRIPT_DIR/../libs -type f -name *.jar)

for lib in $libs
do

echo $lib

CP="${CP}:${lib}"

done

echo ${CP}

java -cp ${CP} org.apache.ignite.scenario.LoadCacheTask  -cfg $SCRIPT_DIR/../config/ignite-remote-client-config.xml
