rem first look for ..\jre\bin\java
set LAUNCHER_JAVA=..\jre\bin\java.exe

IF EXIST %LAUNCHER_JAVA% GOTO FOUNDJRE

rem if this is not available, use JAVA_HOME
set LAUNCHER_JAVA=%JAVA_HOME%\bin\java.exe

IF EXIST %LAUNCHER_JAVA% GOTO FOUNDJRE

rem still not found? Use only "java"
set LAUNCHER_JAVA=java

:FOUNDJRE

IF ["%COVERAGE%"] == [""] GOTO AFTER_ENV
set COVERAGE_ARG=%COVERAGE:${id}=weblauncher%

:AFTER_ENV

%LAUNCHER_JAVA% -Djava.net.preferIPv4Stack=true ^
	-Xmx768m ^
	%COVERAGE_ARG% %LAUNCHER_ARGS% -Dcom.dynatrace.easytravel.install.dir.correction=.. -Dorg.eclipse.rap.rwt.enableUITests=true ^ -Djava.security.auth.login.config=../resources/login-module.config ^
	-jar ..\com.dynatrace.easytravel.weblauncher.jar %*
