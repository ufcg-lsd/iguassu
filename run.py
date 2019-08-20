import os

IGUASSU_DIR_PATH=""
ARREBOL_DIR_PATH=""
WEB_UI_DIR_PATH=""

def run_iguassu():
    os.chdir(IGUASSU_DIR_PATH)
    os.system("bash bin/start-iguassu-service.sh")
    os.chdir("..")

def run_arrebol():
    os.chdir(ARREBOL_DIR_PATH)
    os.system("bash bin/start-service.sh")
    os.chdir("..")

def run_web_ui():
    os.chdir(WEB_UI_DIR_PATH)
    os.system("yarn run serve --port 8082 &")
    os.chdir("..")

if not IGUASSU_DIR_PATH or not ARREBOL_DIR_PATH or not WEB_UI_DIR_PATH:
    print("Please, check the values of dir path from services!")
else:
    run_iguassu()
    run_arrebol()
    run_web_ui()