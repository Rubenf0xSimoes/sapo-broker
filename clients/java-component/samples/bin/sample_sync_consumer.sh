#!/bin/sh

cd $(dirname $0)

classpath="../conf:../lib/*"

java -server \
-Xverify:none -Xms16M -Xmx16M \
-Djava.awt.headless=true \
-Djava.net.preferIPv4Stack=true \
-Djava.net.preferIPv6Addresses=false \
-Dfile.encoding=UTF-8 \
-cp $classpath \
pt.com.broker.client.sample.QueueSyncConsumer $*
#pt.com.broker.client.sample.Consumer -n /test/foo
