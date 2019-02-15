#!/bin/bash

DIR_PATH=$(pwd)

HOSTS_CONF_FILE=$DIR_PATH/"conf-files"/"hosts.conf"

ANSIBLE_FILES_DIR=$DIR_PATH/"ansible-playbook"
ANSIBLE_HOSTS_FILE=$ANSIBLE_FILES_DIR/"hosts"
ANSIBLE_CFG_FILE=$ANSIBLE_FILES_DIR/"ansible.cfg"

IGUASSU_HOST_IP_PATTERN="iguassu_host_ip"
IGUASSU_HOST_IP=$(grep $IGUASSU_HOST_IP_PATTERN $HOSTS_CONF_FILE | awk -F "=" '{print $2}')

echo "Iguassu host ip:" $IGUASSU_HOST_IP

# WRITES THE IP ADDRESS IN HOST FILE
# DELETE EVERYTHING BETWEEN [iguassu-machine] AND [iguassu-machine:vars] AND THEN WRITE IP
sed -i 's/\[iguassu-machine\].*\[iguassu-machine:vars\]/[iguassu-machine]\n\n[iguassu-machine:vars]/' $ANSIBLE_HOSTS_FILE
sed -i "2s/.*/$IGUASSU_HOST_IP/" $ANSIBLE_HOSTS_FILE


# Ansible ssh private key file path
PRIVATE_KEY_FILE_PATH_PATTERN="ansible_ssh_private_key_file"
PRIVATE_KEY_FILE_PATH=$(grep $PRIVATE_KEY_FILE_PATH_PATTERN $HOSTS_CONF_FILE | awk -F "=" '{print $2}')

echo "Ansible ssh private key file path: $PRIVATE_KEY_FILE_PATH"
sed -i "s#.*$PRIVATE_KEY_FILE_PATH_PATTERN=.*#$PRIVATE_KEY_FILE_PATH_PATTERN=$PRIVATE_KEY_FILE_PATH#g" $ANSIBLE_HOSTS_FILE


# Remove user of VM
REMOTE_USER_PATTERN="remote_user"
REMOTE_USER=$(grep $REMOTE_USER_PATTERN $HOSTS_CONF_FILE | awk -F "=" '{print $2}')

echo "Remote user in host: $REMOTE_USER"
sed -i "s/.*$REMOTE_USER_PATTERN = .*/$REMOTE_USER_PATTERN = $REMOTE_USER/" $ANSIBLE_CFG_FILE

PATH_VM="/home/$REMOTE_USER"
echo "Path to add configuration files: $PATH_VM"

# creates the folder and sends the configuration files (backend-confs/frontend-confs)
ssh $REMOTE_USER@$IGUASSU_HOST_IP 'mkdir conf-files'
scp -r $DIR_PATH/conf-files/backend-confs    $REMOTE_USER@$IGUASSU_HOST_IP:$PATH_VM/conf-files
scp -r $DIR_PATH/conf-files/frontend-confs   $REMOTE_USER@$IGUASSU_HOST_IP:$PATH_VM/conf-files

# sends the deploy script
scp $DIR_PATH/deploy-iguassu.sh $REMOTE_USER@$IGUASSU_HOST_IP:$PATH_VM

# run ansible
DEPLOY_IGUASSU_FILE_PATH="deploy-iguassu.yml"

(cd ansible-playbook && ansible-playbook $DEPLOY_IGUASSU_FILE_PATH)
