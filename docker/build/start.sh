#!/bin/bash -e

/usr/share/zookeeper/bin/zkServer.sh start &

mkdir -p /agent
cd /agent
java -cp '/*' \
-Djava.net.preferIPv4Stack=true \
com.spotify.helios.agent.AgentMain \
--name ${HELIOS_NAME} \
&

mkdir -p /master
cd /master
java -cp '/*' \
-Djava.net.preferIPv4Stack=true \
com.spotify.helios.master.MasterMain
