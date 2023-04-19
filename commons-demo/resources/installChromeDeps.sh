#!/bin/bash
#Run this script in easytravel-2.0.0-x64/resources directory as root

CENTOS_PACKAGES="alsa-lib gtk3 flac libXScrnSaver libxslt mesa-libgbm-devel nss"
CENTOS7_PACKAGES="$CENTOS_PACKAGES minizip"
CENTOS8_PACKAGES="$CENTOS_PACKAGES libX11-xcb minizip1.2"
DEBIAN_PACKAGES="libasound2 libatk-bridge2.0-0 libatk1.0-0 libc6 libcairo2 libcups2 libgdk-pixbuf2.0-0 libgtk-3-0 libnspr4 libnss3 libxss1 xdg-utils libminizip-dev libgbm-dev libflac8 libxslt1-dev"
CHROME_DIR="../chrome/chrome-sandbox"

if [ -f /etc/os-release ]
then

	. /etc/os-release
	VER=${VERSION_ID%.*}
	chown root:root $CHROME_DIR && chmod 4755 $CHROME_DIR

	if [[ "$ID $VER" = @(debian 9|debian 10|ubuntu 18|ubuntu 20|ubuntu 22) ]]
 	then

		apt-get update
		apt-get install -y $DEBIAN_PACKAGES

	elif [ "$ID $VER" = "centos 7" ]
	then

		yum install -y $CENTOS7_PACKAGES

	elif [ "$ID $VER" = "centos 8" ]
	then

		dnf install -y dnf-plugins-core epel-release
		dnf config-manager --set-enabled powertools
		dnf install -y $CENTOS8_PACKAGES

	else

		echo "Sorry, $PRETTY_NAME is not supported."

	fi

else

	echo "Sorry, OS not supported."

fi


