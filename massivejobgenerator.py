import os
import requests
import time
import os.path

req = requests.Session()


jobs_dir = "/home/igorvcs/git/arrebol/massivejobs"
cloud = "servers.lsd.ufcg.edu.br"


if not os.path.exists(jobs_dir):
    os.makedirs(jobs_dir)

def create_job(suffix, path, cloud):
   jdf_file = open(path+"/job"+str(suffix), "a")
   jdf_file.write("job: \n")
   jdf_file.write("label: job"+str(suffix) + "\n")
   jdf_file.write("requirements: Glue2RAM >= 1024 AND Glue2CloudComputeManagerID==\""+cloud+ "\"\n")
   jdf_file.write("task:\n")
   jdf_file.write("sleep 10\n")


def create_multiple_jobs(quantity):
   for i in range(0, quantity):
       create_job(i, jobs_dir, cloud)


def postjob(suffix, path) :
    
    n = req.get("http://##.##.##.##:##/arrebol/nonce")
    header = { "X-auth-credentials":"{ username: #####,  password: #####, nonce: "+ n.text+" }"}
    r = req.post("http://##.##.##.##:##/arrebol/job",
         files={
             "jdffilepath": ("", path+"/job"+str(suffix)),
             "X-auth-credentials": ("", "{ username: #####,  password: #####, nonce: "+ n.text+ "}")}, headers=header)
    return r


def main():
   quantity = 200
   create_multiple_jobs(quantity)
   for i in range(0, quantity):
       print(postjob(i, jobs_dir).text)



if __name__ == "__main__":
    main()
