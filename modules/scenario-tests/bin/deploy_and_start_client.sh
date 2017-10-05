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

# Creating an array of IP addresses of the remote hosts from CLIENT_HOSTS.
function define_ips()
{
    # Defining IP of the local machine.
    local local_ip_addresses=`ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p'`
    local comma_separated_ips="${CLIENT_HOSTS}"
    local ips=${comma_separated_ips//,/ }
    local uniq_ips=`echo "${ips[@]}" | tr ' ' '\n' | sort -u | tr '\n' ' '`
    for local_ip in ${local_ip_addresses[@]}
    do
        uniq_ips=( "${uniq_ips[@]/$local_ip}" )
    done
    echo ${uniq_ips[@]}
}

IGNITE_ZIP_BASENAME=$(basename $IGNITE_ZIP_FILE)
IGNITE_CLIENT_CONFIG_BASENAME=$(basename $IGNITE_CLIENT_CONFIG_FILE)
IGNITE_DIR_NAME=${IGNITE_ZIP_BASENAME//.zip/}


# Starting client nodes on remote hosts.
function start_client_nodes()
{
    IFS=' ' read -ra ips_array <<< $(define_ips)
    for ip in ${ips_array[@]}
    do
        #if [[ $ip != "127.0.0.1" && $ip != "localhost" ]]
        #then
            echo "<"$(date +"%H:%M:%S")"> Starting client node on the host ${ip}"

            ssh -o StrictHostKeyChecking=no -o PasswordAuthentication=no ${REMOTE_USER}"@"${ip} $REMOTE_WORK_DIR/$IGNITE_DIR_NAME/scenario-tests/bin/start-remote-client.sh &
        #fi
    done

    rm ${IGNITE_CLIENT_CONFIG_FILE}.temp${NOW}
    rm ${IGNITE_CLIENT_CONFIG_FILE}.temp${NOW}.bak
}

start_client_nodes