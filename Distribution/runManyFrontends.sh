#!/bin/sh

COUNT=$1
shift

cd dist/customer

COUNTER=0
while [  $COUNTER -lt $COUNT ]; do
 echo Starting frontend number $COUNTER
# java "-agentpath:C:\\Program Files\\dynaTrace\\dynaTrace 4.0.0\\agent\\lib64\\dtagent.dll=name=CustomerFrontend_easyTravel,wait=5,server=perftest1:9999"
 
 java "-agentpath:C:\\data\\trunk\\jloadtrace\\agent\\lib64\\dtagent.dll=name=CustomerFrontend_easyTravel,wait=5,server=localhost:9998" \
  -Djava.util.logging.config.file=../resources/logging.properties \
  -Xmx96m \
  -jar ../com.dynatrace.easytravel.customer.frontend.jar > ../frontend.$COUNTER.log 2>&1 &

 sleep 5
 
 let COUNTER=COUNTER+1 
done
