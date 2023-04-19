@echo off

echo Starting easyTravel Configuration UI
pushd .\dist
javaw -Djava.net.preferIPv4Stack=true ^
	-Xmx768m ^
	-jar com.dynatrace.easytravel.launcher.jar
popd
