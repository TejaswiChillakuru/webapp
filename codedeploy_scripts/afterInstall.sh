#!/bin/bash

sudo systemctl start tomcat9

sudo rm -rf /home/ubuntu/assignment2-0.0.1-SNAPSHOT.jar

sudo chown tomcat:tomcat /home/ubuntu/assignment2-0.0.1-SNAPSHOT.jar


sudo rm -rf /var/lib/tomcat9/logs/catalina*
sudo rm -rf /var/lib/tomcat9/logs/*.log
sudo rm -rf /var/lib/tomcat9/logs/*.txt