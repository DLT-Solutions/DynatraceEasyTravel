# ========================================================
# Copyright 2011 dynaTrace Software 
# All rights reserved.
# This software is provided under the  dynaTrace BSD License.
# A copy of this license is available from dynaTrace Software.
# 
# This is a small helper script which helps starting easyTravel 
# distributed across multiple machines. 
# 
# Just edit the location of the easyTravel Installation and 
# specify hostname or ip-address of the procedures that you
# would like to start on different machines


# ================== TODO ===============================
# Specify where easyTravel WebLauncher is available 
cd weblauncher

# ================== TODO ===============================
# Set the hostname/IP-Address of the machine where the 
# master-launcher is executing, 
# don't specify localhost here!
export WEBLAUNCHER=set-this-property

# ================== TODO ===============================
# Set the hostname/IP-Address of the machine where the 
# dynaTrace Server is running
export DTSERVER=localhost

# ================== TODO ===============================
# Specify where each procedure should be started
# use localhost for running it locally
export FRONTEND=localhost
export BACKEND=localhost
export DOTNETBACKEND=localhost
export DOTNETFRONTEND=localhost
export APACHE_HTTPD=localhost
export NGINX=localhost
export CASSANDRA=localhost
export THIRD_PARTY_SERVER=localhost

# Specify additional hosts where commandline launcher 
# listens for manual start-requests (i.e. right click on 
# "Stop" button. Use comma (,) as separator for multiple hosts.
# This is rarely needed, only if you have want to create custom
# scenarios manually 
export ADDITIONAL_HOSTS=

# prefer the packaged JRE, but use a default one if the packaged one is not available
JAVA_BIN=../jre/bin/java
if [ ! -x $JAVA_BIN ]
then
	echo "Packaged JRE not found under ../jre, trying to use java from `which java`"
	JAVA_BIN=java
fi 

$JAVA_BIN -Dcom.dynatrace.easytravel.install.dir.correction=..  \
	-Dcom.dynatrace.easytravel.host.customer_frontend=$FRONTEND \
	-Dcom.dynatrace.easytravel.host.business_backend=$BACKEND \
	-Dcom.dynatrace.easytravel.host.credit_card_authorization=$BACKEND \
	-Dcom.dynatrace.easytravel.host.payment_backend=$DOTNETBACKEND \
	-Dcom.dynatrace.easytravel.host.b2b_frontend=$DOTNETFRONTEND \
	-Dcom.dynatrace.easytravel.host.additional=$ADDITIONAL_HOSTS \
	-Dcom.dynatrace.easytravel.host.apache_httpd=$APACHE_HTTPD \
	-Dcom.dynatrace.easytravel.host.apache_httpd_php=$APACHE_HTTPD \
	-Dcom.dynatrace.easytravel.host.nginx=$NGINX \
	-Dcom.dynatrace.easytravel.host.mysql_content_creator=$APACHE_HTTPD \
	-Dcom.dynatrace.easytravel.host.inprocess_mysql=$APACHE_HTTPD \
	-Dcom.dynatrace.easytravel.host.cassandra=$CASSANDRA \
	-Dcom.dynatrace.easytravel.host.third_party_server=$THIRD_PARTY_SERVER \
	-Dconfig.dtServer=$DTSERVER \
	-Dconfig.backendHost=$BACKEND \
	-Dconfig.paymentBackendHost=$DOTNETBACKEND \
	-Dconfig.internalDatabaseHost=$WEBLAUNCHER \
	-Dconfig.apacheWebServerHost=$APACHE_HTTPD \
	-Dconfig.apacheWebServerB2bHost=$APACHE_HTTPD \
	-Dconfig.thirdpartyHost=$THIRD_PARTY_SERVER \
	-Dconfig.bootPlugins=NamedPipeNativeApplication,DummyNativeApplication.NET,DotNetPaymentService,DatabaseCleanup \
	-DremotingHost=$DOTNETBACKEND \
	-Dconfig.mysqlHost=localhost \
	-Dconfig.thirdpartyUrl=http://$THIRD_PARTY_SERVER:8092/ \
	-Dconfig.thirdpartySocialMediaHost=$THIRD_PARTY_SERVER \
	-Dconfig.thirdpartyCdnHost=$THIRD_PARTY_SERVER \
	-Dorg.eclipse.rap.rwt.enableUITests=true \
	-Xmx768m \
	-jar ../com.dynatrace.easytravel.weblauncher.jar $*
