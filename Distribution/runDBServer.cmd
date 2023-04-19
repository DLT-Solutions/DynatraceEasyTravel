@echo off

set DERBY_HOME=..\ThirdPartyLibraries\Apache\Derby
rem set CLASSPATH=$DERBY_HOME/lib/derbytools.jar:$DERBY_HOME/lib/derbynet.jar

call %DERBY_HOME%\bin\setNetworkServerCP.bat

rem call %DERBY_HOME%\bin\startNetworkServer.bat -p 1572
call %DERBY_HOME%\bin\startNetworkServer.bat
