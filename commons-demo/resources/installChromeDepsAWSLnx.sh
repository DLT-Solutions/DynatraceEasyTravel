#!/bin/bash

#run this script in easytravel-2.0.0-x64/chrome32 directory
#copy contents of the libs directory to the lib64 directory: sudo cp libs/* /lib64

TMP_DIR=tmp
CHROME_BIN=chrome
LIBS=libs

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:`realpath ${LIBS}`

mkdir -p ${LIBS}
mkdir -p ${TMP_DIR}
cd ${TMP_DIR}

# Loop through and install missing dependencies.
while true
do
    finished=true
    # Loop through each of the missing libraries for this round.
    while read -r line
    do
        if [[ $line == *"/"* ]]; then
            # Extract the filename when a path is present (e.g. /lib64/).
            file=`echo $line | sed 's>.*/\([^/:]*\):.*>\1>'`
        else
            # Extract the filename for missing libraries without a path.
            file=`echo $line | awk '{print $1;}'`
        fi
        # We'll require an empty round before completing.
        finished=false

        echo "Finding dependency for ${file}"

        # Find the URL for the Centos 7 RPM containing this library.
        urls=$(repoquery --repofrompath=centos,http://mirror.centos.org/centos/7/os/`arch` \
            --repoid=centos -q --qf="%{location}" --whatprovides $file | \
            sed s/x86_64.rpm$/`arch`.rpm/ | \
            sed s/i686.rpm$/`arch`.rpm/g
        )

        while read -r url; do
            echo "loading $line"
        
            # Download the RPM.
            wget "${url}" -O ${file}.rpm

            # Extract it and remove it.
            rpm2cpio ${file}.rpm | cpio -idmv
            rm ${file}.rpm

            # Copy it over to our library directory and clean up.
            find . | grep /${file} | xargs -n1 -I{} sudo cp {} ../${LIBS}
            rm -rf *
        done <<< "$urls"
    done < <(ldd ../${CHROME_BIN} 2>&1 | grep -e "no version information" -e "not found")

    # Break once no new files have been copied in a loop.
    if [ "$finished" = true ]; then
        break
    fi

    #fix libfontconfig.so.1
    wget http://mirror.centos.org/centos/7/os/x86_64/Packages/fontconfig-2.13.0-4.3.el7.x86_64.rpm
    rpm2cpio fontconfig-2.13.0-4.3.el7.x86_64.rpm | cpio -idmv
    cp usr/lib64/libfontconfig.so.1 ../${LIBS}
    rm -rf *

done
cd ..
rm ${TMP_DIR}