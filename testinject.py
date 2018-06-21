import requests
import time
import os.path

req = requests.Session()


#Gets all jobs, useful for the ocasional debugging
def getalljobs() :
    n = req.get("http://##.##.##.##:#####/arrebol/nonce")
    header = { "X-auth-credentials":"{ username: ########, password: #######, nonce: "+ n.text+" }"}
    r = req.get("http://##.##.##.##:#####/arrebol/job", headers=header)
    return r

#Posts the sleep.jdf job, it's a simple job that asks the target machine to sleep por 10 seconds, with no in-out operations
def postsleepjob() :
    n = req.get("http://##.##.##.##:#####/arrebol/nonce")
    header = { "X-auth-credentials":"{ username: ########, password: #######, nonce: "+ n.text+" }"}
    r = req.post("http://##.##.##.##:#####/arrebol/job",
         files={
             "jdffilepath": ("", "/home/igorvcs/git/arrebol/sleep.jdf"),
             "X-auth-credentials": ("", "{ username: ########, password: #######, nonce: "+ n.text+ "}")}, headers=header)
    return r

#Gets the info on the sleepjob, job233 it's the label name of this job
def getsleepjob() :
    n = req.get("http://##.##.##.##:#####/arrebol/nonce")
    header = { "X-auth-credentials":"{ username: ########, password: #######, nonce: "+ n.text+" }"}
    r= req.get("http://##.##.##.##:#####/arrebol/job/job233", headers=header)
    return r


#Deletes the sleepjob from arrebol, cleaning the system for future tests, since a job isn't removed unless asked and no two jobs can have the same label
def deletesleepjob() :
    n = req.get("http://##.##.##.##:#####/arrebol/nonce")
    header = { "X-auth-credentials":"{ username: ########, password: #######, nonce: "+ n.text+" }"}
    r= req.delete("http://##.##.##.##:#####/arrebol/job/job233",
       files={
              "X-auth-credentials": ("", "{ username: ########, password: #######, nonce: "+ n.text+ "}")}, headers=header)
    return r

#Posts the inout.jdf job, it's a simple job that asks arrebol to transfer a file to target machine
# get that file back and rename it, then sleep por 10 seconds, this is for testing in-out operations
def postinoutjob() :
    n = req.get("http://##.##.##.##:#####/arrebol/nonce")
    header = { "X-auth-credentials":"{ username: ########, password: #######, nonce: "+ n.text+" }"}
    r = req.post("http://##.##.##.##:#####/arrebol/job",
         files={
             "jdffilepath": ("", "/home/igorvcs/git/arrebol/inout.jdf"),
             "X-auth-credentials": ("", "{ username: ########, password: #######, nonce: "+ n.text+ "}")}, headers=header)
    return r

#Gets the info on the inoutjob, job555 it's the label name of this job
def getinoutjob() :
    n = req.get("http://##.##.##.##:#####/arrebol/nonce")
    header = { "X-auth-credentials":"{ username: ########, password: #######, nonce: "+ n.text+" }"}
    r= req.get("http://##.##.##.##:#####/arrebol/job/job555", headers=header)
    return r

#Deletes the inoutjob from arrebol, cleaning the system for future tests, since a job isn't removed unless asked and no two jobs can have the same label
def deleteinoutjob() :
    n = req.get("http://##.##.##.##:#####/arrebol/nonce")
    header = { "X-auth-credentials":"{ username: ########, password: #######, nonce: "+ n.text+" }"}
    r= req.delete("http://##.##.##.##:#####/arrebol/job/job555",
       files={
              "X-auth-credentials": ("", "{ username: ########, password: #######, nonce: "+ n.text+ "}")}, headers=header)
    return r



def checksleepjob(reqstring) :
    if "COMPLETED" in reqstring:
        return True
    return False

def checkinoutjob(reqstring) :
   if "COMPLETED" in reqstring:
        if os.path.isfile("/tmp/fakesim2.jdf") :
            return True
   return False

def positivesleepresult() :
    print "PASSOU SLEEP!"

def negativesleepresult() :
    print "NAO PASSOU SLEEP!"

def positiveinoutresult() :
    print "PASSOU INOUT!"

def negativeinoutresult() :
    print "NAO PASSOU INOUT!"

def main():
    postsleepjob()
    time.sleep(900)
    t = getsleepjob()
    if checksleepjob(t.content):
        positivesleepresult()
    else :
        negativesleepresult()
    deletesleepjob()
    postinoutjob()
    time.sleep(900)
    t = getinoutjob()
    if checkinoutjob(t.content):
        positiveinoutresult()
    else :
        negativeinoutresult()
    deleteinoutjob()





if __name__ == "__main__":
    main()
