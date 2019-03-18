#!/bin/bash

MY_PATH="`dirname \"$0\"`"              # relative
MY_PATH="`( cd \"$MY_PATH\" && pwd )`"  # absolutized and normalized
if [ -z "$MY_PATH" ] ; then
  # error; for some reason, the path is not accessible
  # to the script (e.g. permissions re-evaled after suid)
  exit 1  # fail
fi

echo "Dir path: "$MY_PATH

HOSTS_CONF_FILE=$MY_PATH"/hosts.conf"
echo "hosts.conf file path:" $HOST_CONF_FILE

IGUASSU_HOST_IP_PATTERN="iguassu_host_ip"
IGUASSU_HOST_IP=$(grep $IGUASSU_HOST_IP_PATTERN $HOSTS_CONF_FILE | awk -F "=" '{print $2}')

# Remove user of VM
REMOTE_USER_PATTERN="remote_user"
REMOTE_USER=$(grep $REMOTE_USER_PATTERN $HOSTS_CONF_FILE | awk -F "=" '{print $2}')

# Ansible ssh private key file path
PRIVATE_KEY_FILE_PATH_PATTERN="ansible_ssh_private_key_file"
PRIVATE_KEY_FILE_PATH=$(grep $PRIVATE_KEY_FILE_PATH_PATTERN $HOSTS_CONF_FILE | awk -F "=" '{print $2}')

# Iguassu conf-files path
IGUASSU_CONF_FILES_PATH_PATTERN="iguassu_conf_files_path"
IGUASSU_CONF_FILES_PATH=$(grep $IGUASSU_CONF_FILES_PATH_PATTERN $HOSTS_CONF_FILE | awk -F "=" '{print $2}')

# Iguassu-dashboard conf-files path
DASHBOARD_CONF_FILES_PATH_PATTERN="dashboard_conf_files_path"
DASHBOARD_CONF_FILES_PATH=$(grep $DASHBOARD_CONF_FILES_PATH_PATTERN $HOSTS_CONF_FILE | awk -F "=" '{print $2}')

#ansible-playbook path
ANSIBLE_FILES_PATH_PATTERN="ansible_files_path"
ANSIBLE_FILES_PATH=$(grep $ANSIBLE_FILES_PATH_PATTERN $HOSTS_CONF_FILE | awk -F "=" '{print $2}')

#scripts_files_path
SCRIPTS_FILES_PATH_PATTERN="scripts_files_path"
SCRIPTS_FILES_PATH=$(grep $SCRIPTS_FILES_PATH_PATTERN $HOSTS_CONF_FILE | awk -F "=" '{print $2}')

#private key to send to the container
PRIVATE_KEY_TO_CONTAINER_PATTERN="private_key_to_container"
PRIVATE_KEY_TO_CONTAINER=$(grep $PRIVATE_KEY_TO_CONTAINER_PATTERN $HOSTS_CONF_FILE | awk -F "=" '{print $2}')

ANSIBLE_PLAYBOOK_FILE=$ANSIBLE_FILES_PATH/"ansible-playbook"
ANSIBLE_HOSTS_FILE=$ANSIBLE_FILES_PATH/"hosts"
ANSIBLE_CFG_FILE=$ANSIBLE_FILES_PATH/"ansible.cfg"

echo "Ansible ssh private key file path: $PRIVATE_KEY_FILE_PATH"
echo "Remote user in host: $REMOTE_USER"
echo "Iguassu host ip: " $IGUASSU_HOST_IP
echo "Remote User: " $REMOTE_USER
echo "Private key to container: " $PRIVATE_KEY_TO_CONTAINER

#Testing ssh-port
SSH_PORT_STATUS=$(nmap $IGUASSU_HOST_IP -PN -p ssh | egrep 'open|closed|filtered')
echo "SSH port status: " $SSH_PORT_STATUS

#Testing Connection
ssh -i $PRIVATE_KEY_FILE_PATH -q $REMOTE_USER@$IGUASSU_HOST_IP exit
RETURN_TEST=$?
echo "Status Connection: " $RETURN_TEST

if [ $RETURN_TEST -eq 0 ]; then
    echo "successful connection test"
else
    echo "the ssh connection failed"
    exit
fi

# DELETE EVERYTHING BETWEEN [iguassu-machine] AND [iguassu-machine:vars]
sed -i 's/\[iguassu-machine\].*\[iguassu-machine:vars\]/[iguassu-machine]\n\n[iguassu-machine:vars]/' $ANSIBLE_HOSTS_FILE
# WRITES THE IP ADDRESS IN HOST FILE
sed -i "2s/.*/$IGUASSU_HOST_IP/" $ANSIBLE_HOSTS_FILE

#WRITES THE SSH PRIVATE KEY FILE PATH IN HOST FILE
sed -i "s#.*$PRIVATE_KEY_FILE_PATH_PATTERN=.*#$PRIVATE_KEY_FILE_PATH_PATTERN=$PRIVATE_KEY_FILE_PATH#g" $ANSIBLE_HOSTS_FILE

#WRITES THE SSH PRIVATE KEY FILE PATH IN HOST FILE
sed -i "s/.*$REMOTE_USER_PATTERN = .*/$REMOTE_USER_PATTERN = $REMOTE_USER/" $ANSIBLE_CFG_FILE

PATH_VM="/home/$REMOTE_USER"
echo "Path to add configuration files: $PATH_VM"

# sends the configuration files (backend-confs/frontend-confs)
ssh -i $PRIVATE_KEY_FILE_PATH $REMOTE_USER@$IGUASSU_HOST_IP 'mkdir conf-files && cd conf-files && mkdir secret'
scp -i $PRIVATE_KEY_FILE_PATH -r $IGUASSU_CONF_FILES_PATH     $REMOTE_USER@$IGUASSU_HOST_IP:$PATH_VM/conf-files
scp -i $PRIVATE_KEY_FILE_PATH -r $DASHBOARD_CONF_FILES_PATH   $REMOTE_USER@$IGUASSU_HOST_IP:$PATH_VM/conf-files
scp -i $PRIVATE_KEY_FILE_PATH -r $PRIVATE_KEY_TO_CONTAINER    $REMOTE_USER@$IGUASSU_HOST_IP:$PATH_VM/conf-files/secret

# sends the deploy script
scp -i $PRIVATE_KEY_FILE_PATH -r $SCRIPTS_FILES_PATH/* $REMOTE_USER@$IGUASSU_HOST_IP:$PATH_VM

# run ansible
DEPLOY_IGUASSU_YML_FILE="deploy-iguassu.yml"

(cd $ANSIBLE_FILES_PATH && ansible-playbook $DEPLOY_IGUASSU_YML_FILE)
