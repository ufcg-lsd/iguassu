import os
from zipfile import ZipFile

IGUASSU_BRANCH="release/v1.0.0"
ARREBOL_BRANCH="feature/remote-worker"
WEB_UI_BRANCH="master"

def unzip_file(file_name):
    zf = ZipFile(file_name, 'r')
    zf.extractall()
    zf.close()

# Download and extract the archive file from repository
def download_repository(url):
    os.system("wget {}".format(url))
    file_name = os.path.basename(url)
    unzip_file(file_name)
    os.remove(file_name)

def install_iguassu():
    print("---> Installing Iguassu")
    url = "https://github.com/ufcg-lsd/iguassu/archive/{}.zip".format(IGUASSU_BRANCH)
    download_repository(url)

    iguassu_dir_name = "iguassu-{}".format(IGUASSU_BRANCH.replace("/", "-"))
    os.chdir(iguassu_dir_name)
    os.system("mvn clean install -DskipTests")
    os.chdir("..")
    return iguassu_dir_name

def install_arrebol():
    print("---> Installing Arrebol")
    url = "https://github.com/ufcg-lsd/arrebol/archive/{}.zip".format(ARREBOL_BRANCH)
    download_repository(url)

    arrebol_dir_name = "arrebol-{}".format(ARREBOL_BRANCH.replace("/", "-"))
    os.chdir(arrebol_dir_name)
    os.system("mvn clean install -DskipTests")
    os.chdir("..")
    return arrebol_dir_name

def install_web_ui():
    print("---> Installing Web UI")
    url = "https://github.com/emanueljoivo/iwi/archive/{}.zip".format(WEB_UI_BRANCH)
    download_repository(url)

    iwi_dir_name = "iwi-{}".format(WEB_UI_BRANCH.replace("/", "-"))
    os.chdir(iwi_dir_name)
    os.system("npm install")
    os.chdir("..")
    return iwi_dir_name

oauth_storage_service_token_url="oauth_storage_service_token_url"
oauth_storage_service_client_id="oauth_storage_service_client_id"
oauth_storage_service_client_secret="oauth_storage_service_client_secret"

storage_service_host_url="storage_service_host_url"
iguassu_service_host_url="iguassu_service_host_url"
arrebol_service_host_url="arrebol_service_host_url"

job_state_monitor_period="job_state_monitor_period"
session_monitor_period="session_monitor_period"
job_submission_monitor_period="job_submission_monitor_period"

def datastore_properties():
    properties = {}
    print("Datastore Service Configuration Properties")
    properties[oauth_storage_service_token_url]=input(oauth_storage_service_token_url + "=")
    properties[oauth_storage_service_client_id]=input(oauth_storage_service_client_id + "=")
    properties[oauth_storage_service_client_secret]=input(oauth_storage_service_client_secret + "=")
    return properties

def service_host_addresses():
    addresses = {}
    print("Service Host Addresses")
    print("Requires http prefix 'http://")
    addresses[storage_service_host_url]=input(storage_service_host_url + "=")
    addresses[iguassu_service_host_url]=input(iguassu_service_host_url + "=")
    addresses[arrebol_service_host_url]=input(arrebol_service_host_url + "=")
    print('\n')
    return addresses

def monitors_periods():
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


def main():
    print("---> Installing Iguassu System Service\n")
    print("Configure the following settings...\n")
    ds_properties = datastore_properties()
    host_addresses = service_host_addresses()
    monitors_periods_ = monitors_periods()
    confs = {**ds_properties, **host_addresses, **monitors_periods_}

    iguassu_dir_name = install_iguassu()
    arrebol_dir_name = install_arrebol()
    web_ui_dir_name = install_web_ui()

    write_conf_file(confs, iguassu_dir_name, "iguassu.conf")
    write_conf_file(confs, web_ui_dir_name, ".env")

if __name__ == "__main__":
    main()