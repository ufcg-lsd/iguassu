import os
from zipfile import ZipFile
import shutil

#RUN WITH PYTHON 3 AND IN SUDO MODE

IGUASSU_BRANCH=
ARREBOL_BRANCH=
WEB_UI_BRANCH=

iguassu_dir_name = "iguassu-{}".format(IGUASSU_BRANCH.replace("/", "-"))
arrebol_dir_name = "arrebol-{}".format(ARREBOL_BRANCH.replace("/", "-"))
iwi_dir_name = "iwi-{}".format(WEB_UI_BRANCH.replace("/", "-"))

#Properties names
oauth_storage_service_token_url="oauth_storage_service_token_url"
oauth_storage_service_client_id="oauth_storage_service_client_id"
oauth_storage_service_client_secret="oauth_storage_service_client_secret"

storage_service_host_url="storage_service_host_url"
arrebol_service_host_url="arrebol_service_host_url"
iguassu_service_host_url="iguassu_service_host_url"
web_ui_host_url="web_ui_host_url"

job_state_monitor_period="job_state_monitor_period"
session_monitor_period="session_monitor_period"
job_submission_monitor_period="job_submission_monitor_period"

VUE_APP_EXTERNAL_LINK_DOCS="VUE_APP_EXTERNAL_LINK_DOCS"
VUE_APP_EXTERNAL_LINK_GITHUB_REPO="VUE_APP_EXTERNAL_LINK_GITHUB_REPO"
VUE_APP_EXTERNAL_LINK_ABOUT="VUE_APP_EXTERNAL_LINK_ABOUT"

VUE_APP_OWNCLOUD_SERVER_HOST="VUE_APP_OWNCLOUD_SERVER_HOST"
VUE_APP_OWNCLOUD_OAUTH_CLIENT_ID="VUE_APP_OWNCLOUD_OAUTH_CLIENT_ID"
VUE_APP_OWNCLOUD_OAUTH_SECRET="VUE_APP_OWNCLOUD_OAUTH_SECRET"
VUE_APP_OWNCLOUD_OAUTH_REDIRECT_URI="VUE_APP_OWNCLOUD_OAUTH_REDIRECT_URI"
VUE_APP_IGUASSU_API="VUE_APP_IGUASSU_API"

FAIL = '\033[91m'
ENDC = '\033[0m'

def unzip_file(file_name):
    zf = ZipFile(file_name, 'r')
    zf.extractall()
    zf.close()

# Download/extract the archive file from repository and delete zip
def download_repository(url):
    os.system("wget {}".format(url))
    file_name = os.path.basename(url)
    unzip_file(file_name)
    os.remove(file_name)

def install_iguassu():
    print("---> Installing Iguassu")
    url = "https://github.com/ufcg-lsd/iguassu/archive/{}.zip".format(IGUASSU_BRANCH)
    download_repository(url)

    os.chdir(iguassu_dir_name)
    os.system("mvn clean install -DskipTests")
    os.chdir("..")

def install_arrebol():
    print("---> Installing Arrebol")
    url = "https://github.com/ufcg-lsd/arrebol/archive/{}.zip".format(ARREBOL_BRANCH)
    download_repository(url)

    os.chdir(arrebol_dir_name)
    os.system("mvn clean install -DskipTests")
    os.chdir("..")

def install_web_ui():
    print("---> Installing Web UI")
    url = "https://github.com/emanueljoivo/iwi/archive/{}.zip".format(WEB_UI_BRANCH)
    download_repository(url)

    os.chdir(iwi_dir_name)
    os.system("yarn install")
    os.chdir("..")

def input_datastore_properties():
    properties = {}
    print("Datastore Service Configuration Properties")
    properties[oauth_storage_service_token_url]=input(oauth_storage_service_token_url + "=")
    properties[oauth_storage_service_client_id]=input(oauth_storage_service_client_id + "=")
    properties[oauth_storage_service_client_secret]=input(oauth_storage_service_client_secret + "=")
    return properties

def input_service_host_addresses():
    addresses = {}
    print("Service Host Addresses")
    print("Requires http prefix 'http://")
    addresses[storage_service_host_url]=input(storage_service_host_url + "=")
    addresses[iguassu_service_host_url]=input(iguassu_service_host_url + "=")
    addresses[arrebol_service_host_url]=input(arrebol_service_host_url + "=")
    addresses[web_ui_host_url]=input(web_ui_host_url + "=")
    return addresses

def get_monitors_periods():
    periods = {}
    periods[job_state_monitor_period]=5000
    periods[session_monitor_period]=3600000
    periods[job_submission_monitor_period]=5000
    return periods

def write_conf_file(confs, path, file_name):
    os.chdir(path)
    f = open(file_name, "w+")
    for key, value in confs.items():
        f.write(str(key)+"="+str(value)+"\n")
    f.close()
    os.chdir("..")

def get_web_ui_conf(confs):
    ui_conf = {}
    ui_conf[VUE_APP_EXTERNAL_LINK_DOCS] = "https://nicedoc.io/ufcg-lsd/iguassu"
    ui_conf[VUE_APP_EXTERNAL_LINK_GITHUB_REPO] = "https://github.com/ufcg-lsd/iguassu"
    ui_conf[VUE_APP_EXTERNAL_LINK_ABOUT] = "http://cloudlab-brasil.rnp.br/iguassu"

    ui_conf[VUE_APP_OWNCLOUD_SERVER_HOST] = confs[storage_service_host_url]
    ui_conf[VUE_APP_OWNCLOUD_OAUTH_CLIENT_ID] = confs[oauth_storage_service_client_id]
    ui_conf[VUE_APP_OWNCLOUD_OAUTH_SECRET] = confs[oauth_storage_service_client_secret]
    ui_conf[VUE_APP_OWNCLOUD_OAUTH_REDIRECT_URI] = confs[web_ui_host_url] + "/auth"

    ui_conf[VUE_APP_IGUASSU_API] = confs[iguassu_service_host_url]

    return ui_conf

def write_web_ui_conf(confs, web_ui_dir_name):
    ui_confs = get_web_ui_conf(confs)
    write_conf_file(ui_confs, web_ui_dir_name, ".env")

def write_iguassu_conf(confs, iguassu_dir_name):
    confs.pop(web_ui_host_url, None)
    write_conf_file(confs, iguassu_dir_name, "iguassu.conf")

def main():
    print("---> Run with python 3 and in sudo mode\n")
    print("---> Installing Iguassu System Service\n")
    print("---> Requirements:\n------> OpenJDK8\n------> Maven\n------> Node < 11\n------> Yarn\n")
    print("Configure the following settings...\n")
    ds_properties = input_datastore_properties()
    host_addresses = input_service_host_addresses()
    monitors_periods = get_monitors_periods()
    confs = {**ds_properties, **host_addresses, **monitors_periods}

    try:
        install_iguassu()
        install_arrebol()
        install_web_ui()
        
        write_web_ui_conf(confs, iwi_dir_name)
        write_iguassu_conf(confs, iguassu_dir_name)
    except:
        print(FAIL + "An exception was thrown during an installation." + ENDC)
        print(FAIL + "Rolling back..." + ENDC)
        shutil.rmtree(iguassu_dir_name, ignore_errors=True)
        shutil.rmtree(arrebol_dir_name, ignore_errors=True)
        shutil.rmtree(iwi_dir_name, ignore_errors=True)

if __name__ == "__main__":
    main()
