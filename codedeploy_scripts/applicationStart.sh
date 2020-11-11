#!/bin/bash
cd /home/ubuntu/target
echo "Tejaswi">> sam.txt
sudo java -jar *.jar
echo "Tejaswi">> sam1.txt

sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/home/ubuntu/cloudwatch-config.json -s
