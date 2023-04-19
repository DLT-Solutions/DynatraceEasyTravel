#!/bin/bash

export DERBY_HOME=../ThirdPartyLibraries/Apache/Derby
#export CLASSPATH=$DERBY_HOME/lib/derbytools.jar:$DERBY_HOME/lib/derbynet.jar

. $DERBY_HOME/bin/setNetworkServerCP

$DERBY_HOME/bin/startNetworkServer
