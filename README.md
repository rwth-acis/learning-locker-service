Learning Locker Service
===========================================
This repository is part of the bachelor thesis 'A Multimodal Mentoring Cockpit for Tutor Support'.
It is a las2peer Service, that enables the connection between MobSOS and Learning Locker 
Other related repositories for the bachelor thesis can be found here: [Mentoring Cockpit](https://github.com/rwth-acis/Mentoring-Cockpit)


Learning Locker configuration
--------------------------
The [mobsos-data-processing service](https://github.com/rwth-acis/mobsos-data-processing/tree/bachelor-thesis-philipp-roytburg) stores the messages in a MySQL database. When an xAPI statement from a proxy service (e.g. [moodle-data-proxy](https://github.com/rwth-acis/moodle-data-proxy)) this service is invoked which passes the xAPI statement to a Learning Record Store (LRS).

In Learning Locker an LRS is configured under Settings > Store > Add new.
And under Settings > Client a corresponding client can be configured with the authentication.

Service setup
-------------
To set up the service configure the [property file](etc/i5.las2peer.services.learningLockerService.LearningLockerService.properties) file with your LRS domain and the corresponding authentication.
```INI
lrsDomain = http://exampleDomain/data/xAPI/statements
lrsAuth = exampleAuth
lrsAdminId = AdminClientID
```

Build
--------
Execute the following command on your shell:

```shell
./gradlew build 
```

Start
--------

To start the learning-locker service, follow the [Starting-A-las2peer-Network tutorial](https://github.com/rwth-acis/las2peer-Template-Project/wiki/Starting-A-las2peer-Network) and bootstrap your service to a [mobsos-data-processing service](https://github.com/rwth-acis/mobsos-data-processing/tree/bachelor-thesis-philipp-roytburg).

How to run using Docker
-------------------

First build the image:
```bash
docker build . -t learning-locker
```

Then you can run the image like this:

```bash
docker run -e LRS_DOMAIN=lrsDomain -e LRS_AUTH=lrsAuth -p port:9011 learning-locker
```

Replace *lrsDomain* with the domain of your LRS, *lrsAuth* with the corresponding authentication, lrsAdminId with the client ID of the admin (Stored in LRS) and *port* with a free port in your network.

### Node Launcher Variables

Set [las2peer node launcher options](https://github.com/rwth-acis/las2peer-Template-Project/wiki/L2pNodeLauncher-Commands#at-start-up) with these variables.
The las2peer port is fixed at *9011*.

| Variable | Default | Description |
|----------|---------|-------------|
| BOOTSTRAP | unset | Set the --bootstrap option to bootstrap with existing nodes. The container will wait for any bootstrap node to be available before continuing. |
| SERVICE_PASSPHRASE | Passphrase | Set the second argument in *startService('<service@version>', '<SERVICE_PASSPHRASE>')*. |

