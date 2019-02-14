![alt logo](assets/IGUASSU-LG.png)

# Iguassu
## What is Iguassu?
  Iguassu is a tool for monitoring and executing jobs in a multi-cloud environment federated by the [fogbow middleware](http://www.fogbowcloud.org). Iguassu allows the user to harness cloud resources without bothering about the details of the cloud infrastructure.

  Iguassu has two main components:
  - **Submission Service**: The submission Service is the daemon responsible for receiving job submission and monitoring requests and interacting with the **fogbow middleware** to execute the jobs in the federated cloud resources. The submission services runs a **REST** interface accessed by one client: **Iguassu Dashboard**.
  - **Iguassu Dashboard**: Its a web application that shows status information about the jobs controlled by a **Submission Service**.    

  This document describes how to install and configure the **Submission Service**. To install and configure **Iguassu Dashboard**, go to [its repository](https://github.com/ufcg-lsd/iguassu-dashboard).

## How to use it?

### Writing a job

A job description file, or jdf for short, is a plain text file that contains a job description. Each job description file describes a single job. You can edit a jdf in the text editor of your choice. By convention, we use the .jdf extension to name all job description files.

A jdf file has two types of clauses in it: the job and the task clauses. You use the first type to specify common attributes
and commands for the entire job and the other one to specify particular attributes and commands to the tasks that comprise your parallel application.

Clause | Description
---- | --------------------
job: |
label: | A desciptive name for the job.
sched: | a common root directory to be used by all tasks
init: | Common job initiation, a list of commands to be executed first in all tasks of the job
final: | Common job finalization, a list of commands to be executed last in all tasks of the job 
task: | A list of commands to be executed in this particular part of the job, it is executed between init and final

#### The Job clause

A job clause contains a (possibly empty) list of sub-clauses. For instance, the requirements sub-clause encompasses the list of requirements that need to be fulfilled by a worker node, so that it can be selected to run tasks of the job, while the label sub-clause associates a name to the job. This sub-clause is useful for tracking the execution of the job and also to associate the name of the output files to the job that has generated it.

The sched subclauses sets a new directory as a "root" to all directories mentioned on other clauses, enabling you to use paths relative to this directory

Below we present an example of a jdf, which defines a very simple job named myjob1. It requires worker nodes to run in the cloud of the federation member named **memberOne**. Also, the worker nodes must have the mem attribute set to a number greater or equal to 1024 and .

    job:
    label: myjob1
    requirements : Glue2RAM >= 1024 AND Glue2CloudComputeManagerID==memberOne
    task:  mytask

As we mentioned before, all sub-clauses of a job clause are optional. If the label sub-clause does not exist in the jdf, an internal job id is used to identify it. If there is no requirements sub-clause, the Broker assumes that all worker nodes in your grid are able to run the tasks of your job. If there is no sched subclause, all paths are seen as absolute

Besides label and requirements sub-clauses, you may define default descriptions for all tasks of a job. This is further explained below.

#### The Init clause

The Init clause is the first of the three definition clauses of a task. The ones responible for describing the behavior of a job. The Init clause is shared between all tasks of a given job and is comprised by a list of commands, which can be code to be executed remotely or two special commands for moving files the PUT and GET commands, which will be explained in a specific sections

#### The Final clause

As with the Init clause, the final clause is part of the definition of a task, it is also shared between all tasks, the diference being that it is executed after the task clause commands, it has the same functionality and usability of the Init clause

#### The Task clause

There is one task clause to describe each task of a job. The number of tasks in a job is determined by the number of task clauses in the jdf. The task clause is used to describe the main behavior of a task. That said, the remain of it's behavior is identical to the Init and Final clauses

#### Special operators PUT and GET
This two commands are used to copy files from and to the file driver (ex: owncloud) where you can host files that you will produce or will use when run your task. the syntax is as follows: .
```
 PUT localfile remotefile - where the localfile is visible to the scheduler that will run the job
 GET remotefile localfile - where the localfile is writable to the scheduler that will run the job 
```

## How to configure the **Submission Service**?

To configure the **Submission Service** one should edit two configuration files. In the first file, **iguassu.conf**, it is mandatory to assign the port which the **Submission Service** daemon will wait for HTTP requests after it has been started:

```
 rest_server_port=port
```

In the second file, **sched.conf**, it is possible to tune the behaviour of the **Submission Service**.

To start the **Submission Service**, run the script:

```bash
bash bin/start-iguassu-service
```
