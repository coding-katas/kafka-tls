#!/bin/bash

if [[ -z "$1" ]]; then
  nodes="dns:instance-1,dns:instance-2,dns:instance-3"
  echo "No node instances specified, defaulting to $nodes"
else
  nodes="$1"
fi

rm clientcert.cer keystore* truststore* -f
keytool -genkeypair -alias server-cert -keyalg RSA -keystore keystore -dname "C=FR; ST=GYAN; L=PARIS; O=ORG; OU=UNIT; CN=client" -keypass password -storepass password -ext san=dns:localhost,$nodes
keytool -importkeystore -srckeystore keystore -destkeystore keystore.jks  -keypass password -srcstorepass password -deststorepass password  -validity 1460
keytool -export -alias server-cert -keystore keystore -file clientcert.cer -storepass password
keytool -import -alias server-cert -file clientcert.cer -keystore truststore -keypass password -srcstorepass password -deststorepass password -noprompt
keytool -importkeystore -srckeystore truststore -destkeystore truststore.jks  -keypass password -srcstorepass password -deststorepass password
keytool -list -keystore truststore.jks -v -storepass password
