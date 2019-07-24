# Learning Locker Service
This repository is part of the bachelor thesis 'A Multimodal Mentoring Cockpit for Tutor Support'.
It is a las2peer Service, that enables the connection between MobSOS and Learning Locker 


# MobSOS to Learning Locker
The MobSOS data processing services stores the MobSOS messages in a MySQL database. When an xAPI statement is stored in the remarks of a message a new statement, a service is invoked which passes the xAPI statement onto an LRS. See [property file](etc/i5.las2peer.services.learningLockerService.LearningLockerService.properties) to configure the address and authentication of the LRS.