@echo off

echo Starting easyTravel Commandline Launcher
pushd .\dist
java -Djava.net.preferIPv4Stack=true ^
	-Xmx768m ^
	-Dcom.dynatrace.easytravel.agent.lookup.dir="C:\Program Files\dynaTrace" ^
	-cp com.dynatrace.easytravel.launcher.jar com.dynatrace.easytravel.launcher.CommandlineLauncher -propertyfile "../dist/resources/easyTravelConfig.properties"
popd
