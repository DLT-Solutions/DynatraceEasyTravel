#!/bin/sh
# 
# Script to start easyTravel for the Demo-In-The-Cloud functionality
# 
# It reads the host-information from the Amazon Cloud User-Data and 
# uses this to configure the weblauncher for starting procedures
# remotely.
#
# Copyright 2011 dynaTrace Software 
# All rights reserved.
# This software is provided under the  dynaTrace BSD License.
# A copy of this license is available from dynaTrace Software.

USER_DATA=`curl http://169.254.169.254/latest/user-data`
if [ -z "$USER_DATA" ]
then
  echo "User Data was not supplied. Do Nothing"
  exit 1
fi

echo "Starting weblauncher with user-data: $USER_DATA"

# read values from user-data
export customer_frontend=`echo $USER_DATA|grep "CustomerFrontend="|sed "s#.*CustomerFrontend=\([^ ]*\).*#\1#g"`
export customer_frontend_public=`echo $USER_DATA|grep "CustomerFrontendPublic="|sed "s#.*CustomerFrontendPublic=\([^ ]*\).*#\1#g"`
export customer_frontend_public_dns=`echo $USER_DATA|grep "CustomerFrontendPublicDNS="|sed "s#.*CustomerFrontendPublicDNS=\([^ ]*\).*#\1#g"`
export business_backend=`echo $USER_DATA|grep "BusinessBackend="|sed "s#.*BusinessBackend=\([^ ]*\).*#\1#g"`
export business_backend_public=`echo $USER_DATA|grep "BusinessBackendPublic="|sed "s#.*BusinessBackendPublic=\([^ ]*\).*#\1#g"`
export business_backend_public_dns=`echo $USER_DATA|grep "BusinessBackendPublicDNS="|sed "s#.*BusinessBackendPublicDNS=\([^ ]*\).*#\1#g"`
export credit_card_authorization=`echo $USER_DATA|grep "BusinessBackend="|sed "s#.*BusinessBackend=\([^ ]*\).*#\1#g"`
export payment_backend=`echo $USER_DATA|grep "PaymentBackend="|sed "s#.*PaymentBackend=\([^ ]*\).*#\1#g"`
export b2b_frontend=`echo $USER_DATA|grep "B2BFrontend="|sed "s#.*B2BFrontend=\([^ ]*\).*#\1#g"`
export b2b_frontend_public=`echo $USER_DATA|grep "B2BFrontendPublic="|sed "s#.*B2BFrontendPublic=\([^ ]*\).*#\1#g"`
export b2b_frontend_public_dns=`echo $USER_DATA|grep "B2BFrontendPublicDNS="|sed "s#.*B2BFrontendPublicDNS=\([^ ]*\).*#\1#g"`
export dynaTraceServer=`echo $USER_DATA|grep "dynaTraceServer="|sed "s#.*dynaTraceServer=\([^ ]*\).*#\1#g"`
export dynaTraceServerPublic=`echo $USER_DATA|grep "dynaTraceServerPublic="|sed "s#.*dynaTraceServerPublic=\([^ ]*\).*#\1#g"`
export dynaTraceServerPublicDNS=`echo $USER_DATA|grep "dynaTraceServerPublicDNS="|sed "s#.*dynaTraceServerPublicDNS=\([^ ]*\).*#\1#g"`
export databaseHost=`ec2-metadata --local-ipv4|sed "s#.*local-ipv4: \([^ ]*\).*#\1#g"`
export launcher_public=`ec2-metadata --public-ipv4|sed "s#.*public-ipv4: \([^ ]*\).*#\1#g"`

# set default .NET GUID to 4.1, use 4.0 GUID if the dynaTrace Server reports version 4.0.x
export GUID={DA7CFC47-3E35-4c4e-B495-534F93B28683}
curl --user admin:admin --insecure https://${dynaTraceServer}/rest/management/version | grep -q 4.0
export DYNATRACE_VERSION_4=$?
if [ DYNATRACE_VERSION_4 -eq 0 ]
then
	export GUID={333A026A-B413-486f-91A6-A33D8C9874D6} 
fi

export PROP="       
		-Dcom.dynatrace.easytravel.host.customer_frontend=${customer_frontend} \
        -Dcom.dynatrace.easytravel.host.customer_frontend.public=http://${customer_frontend_public} \
        -Dcom.dynatrace.easytravel.host.business_backend=${business_backend} \
        -Dcom.dynatrace.easytravel.host.credit_card_authorization=${credit_card_authorization} \
        -Dcom.dynatrace.easytravel.host.payment_backend=${payment_backend} \
        -Dcom.dynatrace.easytravel.host.b2b_frontend=${b2b_frontend} \
        -Dcom.dynatrace.easytravel.host.b2b_frontend.public=http://${b2b_frontend_public} \
		-Dcom.dynatrace.easytravel.host.apache_httpd=${business_backend} \
		-Dcom.dynatrace.easytravel.host.apache_httpd_php=${business_backend} \
		-Dcom.dynatrace.easytravel.host.mysql_content_creator=${business_backend} \
		-Dcom.dynatrace.easytravel.host.inprocess_mysql=${business_backend} \
		-Dcom.dynatrace.easytravel.host.cassandra=localhost \
		-Dcom.dynatrace.easytravel.host.third_party_server=localhost \
        \
        -Dconfig.backendHost=${business_backend} \
        -Dconfig.paymentBackendHost=${payment_backend} \
        -Dconfig.dtServer=${dynaTraceServer} \
        -Dconfig.internalDatabaseHost=${databaseHost} \
        -Dconfig.dtServerWebURL=https://${dynaTraceServerPublic}/ \
        -Dconfig.b2bFrontendPortRangeStart=80 \
        -Dconfig.b2bFrontendPortRangeEnd=80 \
        -Dconfig.bootPlugins=SocketNativeApplication,DummyNativeApplication.NET,DotNetPaymentService,DatabaseCleanup \
        -DremotingHost=${payment_backend} \
        -Dconfig.paymentBackendEnvArgs=DT_WAIT=60,RUXIT_WAIT=5,COR_ENABLE_PROFILING=0x1,COR_PROFILER=${GUID} \
        -Dconfig.b2bFrontendEnvArgs=DT_WAIT=60,RUXIT_WAIT=5,COR_ENABLE_PROFILING=0x1,COR_PROFILER=${GUID} \
		-Dconfig.mysqlHost=localhost \
		-Dconfig.thirdpartyHost=localhost \
        \
        -Dconfig.apacheFrontendPublicUrl=http://${business_backend_public} \
        -Dcom.dynatrace.easytravel.host.apache_customer.public=http://${business_backend_public} \
        -Dconfig.apacheB2BFrontendPublicUrl=http://${business_backend_public}:8999 \
        -Dcom.dynatrace.easytravel.host.apache_b2b.public=http://${business_backend_public}:8999 \
		-Dconfig.apacheWebServerHost=${business_backend} \
		-Dconfig.apacheWebServerPort=8080 \
		-Dconfig.apacheWebServerB2bHost=${business_backend} \
		-Dconfig.apacheWebServerB2bPort=8999 \
        -Dconfig.apacheWebServerVirtualIp=*:8080 \
        -Dconfig.apacheWebServerB2bVirtualIp=*:8999 \
        \
		-Dconfig.baseLoadDefault=10 \
		-DsyncProcessTimeoutMs=60000 \
		-Dconfig.frontendJavaopts=-Xmx200m \
        "

echo "Using the following properties: $PROP" 

# start weblauncher with all necessary properties replaced
cd `dirname $0`/weblauncher
COVERAGE=$PROP ./weblauncher.sh $*

# Note: we do not need to change the CustomerFrontend default port as we use port-forwarding from 80 to 8080

# Note: we use port 8080 for Apache on business-backend host as we use port-forwarding from 80 to 8080

# Note: The VirutalIp settings above are just a workaround to allow Apache to run on ip-based remote hosts, we should actually fix UrlUtils.isInternetUrl() to not trigger on ip-addresses!

# Note: would it work to switch Apache to port 443?
#        -Dconfig.apacheWebServerPort=443
#        -Dconfig.apacheFrontendPublicUrl=http://${launcher_public}:443 \
#				-Dconfig.thirdpartyUrl=http://${hostname}:8092/
#				-Dconfig.thirdpartySocialMediaHost=${hostname}
#				-Dconfig.thirdpartyCdnHost=${hostname}" />
