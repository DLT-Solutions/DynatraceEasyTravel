#!/bin/sh

cd `dirname $0`

# prefer the packaged JRE, but use a default one if the packaged one is not available
JAVA_BIN=../jre/bin/java
if [ ! -x $JAVA_BIN ]
then
	echo "Packaged JRE not found under ../jre, trying to use java from `which java`"
	JAVA_BIN=java
fi 

$JAVA_BIN -Xmx768m \
	$COVERAGE -Dcom.dynatrace.easytravel.install.dir.correction=.. -Dorg.eclipse.rap.rwt.enableUITests=true \
	-Djava.security.auth.login.config=../resources/login-module.config \
	-jar ../com.dynatrace.easytravel.weblauncher.jar "$@"
