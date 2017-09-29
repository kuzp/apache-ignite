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

REMOTE_USER="oostanin"

function edit_xml()
{
    cp $IGNITE_CONFIG_FILE "${IGNITE_CONFIG_FILE}.temp${NOW}"
    IFS=' ' read -ra ips_array <<< $(define_ips)
    for ip in ${ips_array[@]}
        do
            ip_list=`echo -e ${ip_list}"<value>${ip}:47500..47509<\/value>"`
        done
    sed -i.bak s/"<!--to_be_replaced_by_IP_list-->"/"$ip_list"/g ${IGNITE_CONFIG_FILE}.temp${NOW}
}

# Copying working directory to remote hosts.
function copy_to_hosts()
{
    IFS=' ' read -ra ips_array <<< $(define_ips)
    for ip in ${ips_array[@]}
    do
        #if [[ $ip != "127.0.0.1" && $ip != "localhost" ]]
        #then
            echo "<"$(date +"%H:%M:%S")"> Copying to the host ${ip}"

            ssh -o StrictHostKeyChecking=no $ip rm -rf $REMOTE_WORK_DIR

            ssh -o StrictHostKeyChecking=no $ip mkdir -p $REMOTE_WORK_DIR

            scp -o StrictHostKeyChecking=no  $IGNITE_ZIP_FILE $ip:$REMOTE_WORK_DIR/$IGNITE_ZIP_BASENAME

            ssh -o StrictHostKeyChecking=no $ip unzip -q $REMOTE_WORK_DIR/$IGNITE_ZIP_BASENAME -d $REMOTE_WORK_DIR

            ssh -o StrictHostKeyChecking=no $ip mkdir -p $REMOTE_WORK_DIR/$IGNITE_DIR_NAME/log

            scp -o StrictHostKeyChecking=no  ${IGNITE_CONFIG_FILE}.temp${NOW} $ip:$REMOTE_WORK_DIR/$IGNITE_DIR_NAME/config/$IGNITE_CONFIG_BASENAME

            scp -o StrictHostKeyChecking=no  $IGNITE_BASE_CONFIG $ip:$REMOTE_WORK_DIR/$IGNITE_DIR_NAME/config/ignite-base-config.xml

            ssh -o StrictHostKeyChecking=no -o PasswordAuthentication=no ${REMOTE_USER}"@"${ip} nohup $REMOTE_WORK_DIR/$IGNITE_DIR_NAME/bin/ignite.sh $REMOTE_WORK_DIR/$IGNITE_DIR_NAME/config/$IGNITE_CONFIG_BASENAME &
        #fi
    done

    rm ${IGNITE_CONFIG_FILE}.temp${NOW}
    rm ${IGNITE_CONFIG_FILE}.temp${NOW}.bak
}

function kill_nodes()
{
    IFS=' ' read -ra ips_array <<< $(define_ips)
    for ip in ${ips_array[@]}
    do
        if [[ $ip != "127.0.0.1" && $ip != "localhost" ]]
        then
            echo "<"$(date +"%H:%M:%S")"> Killing java on the host ${ip}"
            ssh -o StrictHostKeyChecking=no $ip pkill -f 'java'
        fi
    done
}

edit_xml
#kill_nodes
copy_to_hosts