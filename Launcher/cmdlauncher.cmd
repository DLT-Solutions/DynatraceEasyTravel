jre\bin\java -Djava.net.preferIPv4Stack=true ^
	-Xmx768m %* ^
	-jar com.dynatrace.easytravel.cmdlauncher.jar ^
	-propertyfile "resources/easyTravelConfig.properties" -noautostart
