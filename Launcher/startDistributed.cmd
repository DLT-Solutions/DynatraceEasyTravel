rem ========================================================
rem Copyright 2011 dynaTrace Software 
rem All rights reserved.
rem This software is provided under the  dynaTrace BSD License.
rem A copy of this license is available from dynaTrace Software.
rem 
rem This is a small helper script which helps starting easyTravel 
rem distributed across multiple machines. 
rem 
rem Just edit the location of the easyTravel Installation and 
rem specify hostname or ip-address of the procedures that you
rem would like to start on different machines


rem ================== TODO ===============================
rem Specify where easyTravel WebLauncher is available 
cd weblauncher

rem ================== TODO ===============================
rem Set the hostname/IP-Address of the machine where the 
rem master-launcher is executing, 
rem don't specify localhost here!
set WEBLAUNCHER=set-this-property

rem ================== TODO ===============================
rem Set the hostname/IP-Address of the machine where the 
rem dynaTrace Server is running
set DTSERVER=localhost

rem ================== TODO ===============================
rem Specify where each procedure should be started
rem use localhost for running it locally
set FRONTEND=localhost
set BACKEND=localhost
set DOTNETBACKEND=localhost
set DOTNETFRONTEND=localhost
set APACHE_HTTPD=localhost
set NGINX=localhost
set CASSANDRA=localhost
set THIRD_PARTY_SERVER=localhost

rem Specify additional hosts where commandline launcher 
rem listens for manual start-requests (i.e. right click on 
rem "Stop" button. Use comma (,) as separator for multiple hosts.
rem This is rarely needed, only if you have want to create custom
rem scenarios manually 
set ADDITIONAL_HOSTS=

..\jre\bin\java -Dcom.dynatrace.easytravel.install.dir.correction=..  ^
-Dcom.dynatrace.easytravel.host.customer_frontend=%FRONTEND% ^
-Dcom.dynatrace.easytravel.host.business_backend=%BACKEND% ^
-Dcom.dynatrace.easytravel.host.credit_card_authorization=%BACKEND% ^
-Dcom.dynatrace.easytravel.host.payment_backend=%DOTNETBACKEND% ^
-Dcom.dynatrace.easytravel.host.b2b_frontend=%DOTNETFRONTEND% ^
-Dcom.dynatrace.easytravel.host.additional=%ADDITIONAL_HOSTS% ^
-Dcom.dynatrace.easytravel.host.apache_httpd=%APACHE_HTTPD% ^
-Dcom.dynatrace.easytravel.host.apache_httpd_php=%APACHE_HTTPD% ^
-Dcom.dynatrace.easytravel.host.nginx=%NGINX% ^
-Dcom.dynatrace.easytravel.host.mysql_content_creator=%APACHE_HTTPD% ^
-Dcom.dynatrace.easytravel.host.inprocess_mysql=%APACHE_HTTPD% ^
-Dcom.dynatrace.easytravel.host.cassandra=%CASSANDRA% ^
-Dcom.dynatrace.easytravel.host.third_party_server=%THIRD_PARTY_SERVER% ^
-Dconfig.dtServer=%DTSERVER% ^
-Dconfig.backendHost=%BACKEND% ^
-Dconfig.paymentBackendHost=%DOTNETBACKEND% ^
-Dconfig.internalDatabaseHost=%WEBLAUNCHER% ^
-Dconfig.apacheWebServerHost=%APACHE_HTTPD% ^
-Dconfig.apacheWebServerB2bHost=%APACHE_HTTPD% ^
-Dconfig.thirdpartyHost=%THIRD_PARTY_SERVER% ^
-Dconfig.bootPlugins=NamedPipeNativeApplication,DummyNativeApplication.NET,DotNetPaymentService,DatabaseCleanup ^
-DremotingHost=%DOTNETBACKEND% ^
-Dconfig.mysqlHost=localhost ^
-Dconfig.thirdpartyUrl=http://%THIRD_PARTY_SERVER%:8092/ ^
-Dconfig.thirdpartySocialMediaHost=%THIRD_PARTY_SERVER% ^
-Dconfig.thirdpartyCdnHost=%THIRD_PARTY_SERVER% ^
-Dorg.eclipse.rap.rwt.enableUITests=true ^
-Djava.net.preferIPv4Stack=true ^
-Djava.security.auth.login.config=../resources/login-module.config ^
-Xmx768m ^
-jar ..\com.dynatrace.easytravel.weblauncher.jar %1 %2 %3 %4 %5 %6 %7 %8 %9 

pause
cd ..
