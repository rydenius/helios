#!/bin/bash -xe

# Look up the ip address of
IPADDRESS=$(ip addr | grep inet | grep eth0 | tr '/' ' ' |  awk '{ print $2 }')

# Start etcd
/etcd $ETCD_OPTS &

# Write skydns configuration and retry for 30 seconds until successful
for i in {1..30}; do
	if curl --retry 30 -XPUT http://127.0.0.1:4001/v2/keys/skydns/config \
		-d value='{"dns_addr":"0.0.0.0:53", "ttl":3600, "nameservers": ["8.8.8.8:53","8.8.4.4:53"], "domain":"local."}'; then
		break
	fi
	sleep 1
done

# Create A record for the standalone host
curl -XPUT http://127.0.0.1:4001/v2/keys/skydns/local/standalone \
    -d value="{\"host\":\"$HOST_ADDRESS\"}"

/skydns $SKYDNS_OPTS &

/usr/share/zookeeper/bin/zkServer.sh start &

# Start agent
mkdir -p /agent
cd /agent
java -cp '/*' \
-Xmx128m \
-Djava.net.preferIPv4Stack=true \
com.spotify.helios.agent.AgentMain \
--name ${HELIOS_NAME} \
--service-registrar-plugin /usr/share/helios/lib/plugins/helios-skydns-0.1.jar \
--id standalone-host \
--dns $IPADDRESS \
--domain 'local.' \
--service-registry "http://127.0.0.1:4001" \
--env SPOTIFY_POD='local.' \
--env SPOTIFY_DOMAIN='local.' \
--env HELIOS_HOST_ADDRESS=$HOST_ADDRESS \
&

# Start master
mkdir -p /master
cd /master
java -cp '/*' \
-Xmx128m \
-Djava.net.preferIPv4Stack=true \
com.spotify.helios.master.MasterMain \
--service-registrar-plugin /usr/share/helios/lib/plugins/helios-skydns-0.1.jar \
&

# Sleep or execute command line
if [ -z "$@" ]; then
	while :; do sleep 1; done
else
	cd /
	"$@"
fi
