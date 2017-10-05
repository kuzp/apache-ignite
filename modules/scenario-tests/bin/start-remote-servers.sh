#!/usr/bin/env bash

# Define script directory.
SCRIPT_DIR=$(cd $(dirname "$0"); pwd)
MAIN_DIR=$(cd ${SCRIPT_DIR}/../; pwd)

CONFIG_INCLUDE=$1

CONFIG_TMP=`mktemp tmp.XXXXXXXX`

cp $CONFIG_INCLUDE $CONFIG_TMP
chmod +x $CONFIG_TMP

. $CONFIG_TMP
rm $CONFIG_TMP

# Creating an array of IP addresses of the remote hosts from SERVER_HOSTS.
function define_ips()
{
    # Defining IP of the local machine.
    local local_ip_addresses=`ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p'`
    local comma_separated_ips="${SERVER_HOSTS}"
    local ips=${comma_separated_ips//,/ }
    local uniq_ips=`echo "${ips[@]}" | tr ' ' '\n' | sort -u | tr '\n' ' '`
    for local_ip in ${local_ip_addresses[@]}
    do
        uniq_ips=( "${uniq_ips[@]/$local_ip}" )
    done
    echo ${uniq_ips[@]}
}

IGNITE_ZIP_BASENAME=$(basename $IGNITE_ZIP_FILE)
IGNITE_CONFIG_BASENAME=$(basename $IGNITE_CONFIG_FILE)
IGNITE_DIR_NAME=${IGNITE_ZIP_BASENAME//.zip/}

echo $IGNITE_ZIP_BASENAME
echo $IGNITE_CONFIG_BASENAME
echo $IGNITE_DIR_NAME

# Copying working directory to remote hosts.
function start_nodes()
{
    IFS=' ' read -ra ips_array <<< $(define_ips)
    for ip in ${ips_array[@]}
    do
        #if [[ $ip != "127.0.0.1" && $ip != "localhost" ]]
        #then
            echo "<"$(date +"%H:%M:%S")"> Starting node on the host ${ip}"

            ssh -o StrictHostKeyChecking=no -o PasswordAuthentication=no ${ip} nohup $REMOTE_WORK_DIR/$IGNITE_DIR_NAME/bin/ignite.sh $REMOTE_WORK_DIR/$IGNITE_DIR_NAME/config/$IGNITE_CONFIG_BASENAME &
        #fi
    done

}

start_nodes