## Install Singularity with Vagrant
Follow the instructions to create a virtual machine that runs all Singularity dependencies to ease development and prevent littering your local environment with executables and configuration files. All Mesos components (Mesos master and slave) and all Singularity dependencies (zookeeper and mysql) will be automatically installed and started inside the VM.

This setup is only meant for developing purposes. In a production environment you will need to run each component in a different host and run multiple instances for certain components for high availability.

The setup steps have been tested on mac computers running MAC OS X 10.9.x but they should as well work on any recent Linux distribution.

Install [Vagrant](http://www.vagrantup.com/downloads.html)

Install [Virtualbox](https://www.virtualbox.org/wiki/Downloads)

Open a command shell and run the following commands to install the required vagrant plugins with the specified order (If vagrant version 1.5 or later is used *vagrant-omnibus* should be installed before *vagrant-berkshelf* because omnibus causes a downgrade of berkshelf version back to 1.3.7 which in turn will cause 'vagrant up' to fail).

```
$ vagrant plugin install vagrant-omnibus
$ vagrant plugin install vagrant-berkshelf --plugin-version=2.0.1
$ vagrant plugin install vagrant-hostsupdater
```

Clone Singularity from *github.com* in you preferred directory and go into the *vagrant* directory inside the cloned project:

```
$ cd my_home/tests
$ git clone git@github.com:HubSpot/Singularity.git
$ cd Singularity/vagrant
$ ls
```

Look for the provided *Vagrantfile* that contains the required vagrant commands for setting up a *VirtualBox* VM with all required software. The utilized vagrant provisioner for performing the installation is *chef-solo* along with the *Berkshelf* plugin for handling the required chef recipe. The provided *Berksfile* contains information about the *singularity* chef recipe that builds the VM. To start building the VM run the following command: 

```
$ vagrant up develop
```

**if you just run `vagrant up` it will bring up a VM with singularity installed inside the VM which is not what you want when you develop singularity***

This command will first setup and then bring up the virtual machine. The first time you run this, you should be patient because it needs to download a basic Linux image and then install all the required software packages as well as build and install Singularity. When this is done the first time, every other time that you run *vagrant up*, it will take only a few seconds to boot the virtual machine up. 

During the installation your local machine hosts file is configured with the VM IP (so you can access it as *vagrant-singularity*) and you will be asked to provide your password.

When vagrant command finishes check that everything has been installed successfully executing the following steps:

First verify that Zookeeper is running by logging into the virtual machine and using the zookeeper command line tool to connect to the zookeeper server and list the available nodes:
```
$ vagrant ssh
$ sudo /usr/share/zookeeper/bin/zkCli.sh -server localhost:2181

When connected execute the following command to list the root nodes:
ls /

You should see the following listing:
[singularity, mesos, zookeeper]

type 'quit' to exit zookeper console
```
 
Then verify that the mesos cluster is running and the Mesos UI is accessible at:

[http://vagrant-singularity:5050/](http://vagrant-singularity:5050/)

Verify that mysql server is running: 

```
$ mysql -u root -p

specify *mesos7mysql* as password

then check if singularity database has been created:

mysql> show databases;

You should something like the following:
+--------------------+
| Database           |
+--------------------+
| information_schema |
| mysql              |
| performance_schema |
| singularity        |
+--------------------+

type 'exit' to exit mysql client console
```

Build static files of Singularity UI:
```
$ npm install
```

The compiled static files are placed in [`../SingularityService/src/main/resources/`](../SingularityService/src/main/resources/) and can be packaged with maven.

Build singularity with maven:

```
$ mvn clean package
```

The mysql database Singularity will use to keep historical data has been already created in the VM but you also need to create the mysql tables required by singularity using the singularity service jar that maven has just built (it uses the liquibase library to perform table migrations): 

```
$ java -jar SingularityService/target/SingularityService-*-SNAPSHOT.jar db migrate ./vagrant_singularity.yaml --migrations ./migrations.sql
```

Now you are ready to start Singularity using the provided `vagrant_singularity.yaml` config: 

```
$ java -jar SingularityService/target/SingularityService-*-SNAPSHOT.jar server ./vagrant_singularity.yaml
```

Verify that Singularity is running:

[http://localhost:7092/](http://localhost:7092/)

If everything went well you will see the following screen:
![Singularity UI first run](images/SingularityUI_First_Run.png)

Enter your username to let Singularity populate a personalized dashboard and go to [Deploy Examples](Singularity_Deploy_Examples.md) to find out how to deploy some test projects.
