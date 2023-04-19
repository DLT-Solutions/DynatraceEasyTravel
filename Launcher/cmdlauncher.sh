#!/bin/sh

unset http_proxy

cd `dirname $0`

# prefer the packaged JRE, but use a default one if the packaged one is not available
JAVA_BIN=jre/bin/java
if [ ! -x $JAVA_BIN ]
then
	echo "Packaged JRE not found under ../jre, trying to use java from `which java`"
	JAVA_BIN=java
fi 

$JAVA_BIN -Xmx768m "$@" \
	-jar com.dynatrace.easytravel.cmdlauncher.jar -propertyfile "resources/easyTravelConfig.properties" -noautostart
