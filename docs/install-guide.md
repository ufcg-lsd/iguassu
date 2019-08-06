# Install Iguassu
To simplify the getting started experience, we have a script that will download dependencies and set up the components in any Debian-based environment. All you have to do is fill the configuration files with the correct information.

## Web UI
At this point, we have the system ready to run Jobs. Next, we'll learn how to submit and track its execution in the web client.
### Opening the Web UI
As soon as Iguassu is running, the web client will be running as well. It is hosted on port 8082.  Visit http://localhost:8082 to open the Iguassu UI.
### Authentication and Authorization
Iguassu uses the OAuth2 authentication model, an implementation offered by the ownCloud storage service.
[Here](https://doc.owncloud.com/server/admin_manual/configuration/server/security/oauth2.html) you can find the documentation. This service is required for data transfer, either for input or output available as clauses in job definition. For this reason it is convenient to use it also as authentication service for now. To log in simply authorize Iguassu in your own ownCloud account.

## Remote Deploy
We have a framework for remote deployment using Ansible. Like the installation script, you only need to indicate the hosts where you want Iguassu instances and correctly populate the configuration files.

We've now concluded the getting started guide.

