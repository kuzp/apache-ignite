#!/usr/bin/env bash

# Define script directory.
SCRIPT_HOME=$(cd $(dirname "$0"); pwd)

function define_variables(){
    IGNITE_ZIP_BASENAME=$(basename $IGNITE_ZIP_FILE)
    IGNITE_DIR_NAME=${IGNITE_ZIP_BASENAME/.zip/}
    SERVER_CONFIG_BASENAME=$(basename $SERVER_CONFIG_FILE)
    CLIENT_CONFIG_BASENAME=$(basename $CLIENT_CONFIG_FILE)
}

function print_help()
{
    echo "There will be help to_do"

}

# Copying working directory to remote hosts.
function deploy_to_hosts()
{
    define_variables

    IFS=' ' read -ra ips_array <<< $(define_ips $1)

    for ip1 in ${ips_array[@]}
    do
        echo $ip1
    done

    for ip in ${ips_array[@]}
    do
        if [[ $ip != "127.0.0.1" && $ip != "localhost" ]]
        then
            echo "<"$(date +"%H:%M:%S")"> Copying to the host ${ip}"

            ssh -o StrictHostKeyChecking=no $ip rm -rf $REMOTE_WORK_DIR

            ssh -o StrictHostKeyChecking=no $ip mkdir -p $REMOTE_WORK_DIR

            scp -o StrictHostKeyChecking=no $IGNITE_ZIP_FILE $ip:$REMOTE_WORK_DIR/$IGNITE_ZIP_BASENAME

            ssh -o StrictHostKeyChecking=no $ip unzip -q $REMOTE_WORK_DIR/$IGNITE_ZIP_BASENAME -d $REMOTE_WORK_DIR
        fi
    done
}

# Copying working directory to remote hosts.
function deploy_configs()
{
    define_variables

    IFS=' ' read -ra ips_array <<< $(define_ips $1)

    for ip in ${ips_array[@]}
    do
        if [[ $ip != "127.0.0.1" && $ip != "localhost" ]]
        then
            echo "<"$(date +"%H:%M:%S")"> Copying config directory to the host ${ip}"

            scp -r -o StrictHostKeyChecking=no $SCRIPT_HOME/../config/* $ip:$REMOTE_WORK_DIR/$IGNITE_DIR_NAME/scenario-tests/config/ >> /dev/null
            scp -r -o StrictHostKeyChecking=no $SCRIPT_HOME/../bin/* $ip:$REMOTE_WORK_DIR/$IGNITE_DIR_NAME/scenario-tests/bin/ >> /dev/null
        fi
    done
}


# Creating an array of IP addresses of the remote hosts from $1.
function define_ips()
{
    # Defining IP of the local machine.
    local local_ip_addresses=`ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p'`
    local comma_separated_ips=$1
    local ips=${comma_separated_ips//,/ }
    local uniq_ips=`echo "${ips[@]}" | tr ' ' '\n' | sort -u | tr '\n' ' '`
    for local_ip in ${local_ip_addresses[@]}
    do
        uniq_ips=( "${uniq_ips[@]/$local_ip}" )
    done
    echo ${uniq_ips[@]}
}

function start_server_nodes()
{
    define_variables

    IFS=' ' read -ra ips_array <<< $(define_ips $1)
    for ip in ${ips_array[@]}
    do
        if [[ $ip != "127.0.0.1" && $ip != "localhost" ]]
        then
            echo "<"$(date +"%H:%M:%S")"> Starting node on the host ${ip}"

            ssh -o StrictHostKeyChecking=no -o PasswordAuthentication=no ${ip} nohup $REMOTE_WORK_DIR/$IGNITE_DIR_NAME/bin/ignite.sh $REMOTE_WORK_DIR/$IGNITE_DIR_NAME/scenario-tests/config/$SERVER_CONFIG_BASENAME &
        fi
    done
}

# Starting client nodes on remote hosts.
function start_client_nodes()
{
    define_variables

    IFS=' ' read -ra ips_array <<< $(define_ips $1)
    for ip in ${ips_array[@]}
    do
        if [[ $ip != "127.0.0.1" && $ip != "localhost" ]]
        then
            echo "<"$(date +"%H:%M:%S")"> Starting client node on the host ${ip}"

            ssh -o StrictHostKeyChecking=no -o PasswordAuthentication=no ${ip} $REMOTE_WORK_DIR/$IGNITE_DIR_NAME/scenario-tests/bin/start-client.sh $REMOTE_WORK_DIR/$IGNITE_DIR_NAME/scenario-tests/config/test.properties &
        fi
    done
}

function kill_java()
{
    IFS=' ' read -ra ips_array <<< $(define_ips $1)
    for ip in ${ips_array[@]}
    do
        if [[ $ip != "127.0.0.1" && $ip != "localhost" ]]
        then
            echo "<"$(date +"%H:%M:%S")"> Killing java on the host ${ip}"
            ssh -o StrictHostKeyChecking=no $ip pkill -f 'java'
        fi
    done
}

function read_properties()
{
    CONFIG_INCLUDE=$1

    CONFIG_TMP=`mktemp tmp.XXXXXXXX`

    cp $CONFIG_INCLUDE $CONFIG_TMP
    chmod +x $CONFIG_TMP

    . $CONFIG_TMP
    rm $CONFIG_TMP
}

function check_properties()
{
    if [[ $1 == "" ]]; then
        echo "Error. You need to specify a property file."

        print_help

        exit 1
    fi
}

function edit_remote_config()
{
    cp $SERVER_CONFIG_FILE "${SERVER_CONFIG_FILE}.temp${NOW}"
    IFS=' ' read -ra ips_array <<< $(define_ips)
    for ip in ${ips_array[@]}
        do
            ip_list=`echo -e ${ip_list}"<value>${ip}:47500..47509<\/value>"`
        done
    sed -i.bak s/"<!--to_be_replaced_by_IP_list-->"/"$ip_list"/g ${SERVER_CONFIG_FILE}.temp${NOW}
}