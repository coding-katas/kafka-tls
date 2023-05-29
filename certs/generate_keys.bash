#!/bin/bash

if [[ -z "$1" ]]; then
  nodes="dns:instance-1,dns:instance-2"
  echo "No node instances specified, defaulting to $nodes"
else
  nodes="$1"
fi

rm clientcert.cer keystore* truststore* -f
# Generating the keystore
keytool -genkeypair -alias server-cert -keyalg RSA -keystore keystore -dname "C=FR; ST=GYAN; L=PARIS; O=ORG; OU=UNIT; CN=client" -keypass password -storepass password -ext san=dns:localhost,$nodes
# Generating .jks
keytool -importkeystore -srckeystore keystore -destkeystore keystore.jks  -keypass password -srcstorepass password -deststorepass password  -validity 1460
keytool -export -alias server-cert -keystore keystore -file clientcert.cer -storepass password
keytool -import -alias server-cert -file clientcert.cer -keystore truststore -keypass password -srcstorepass password -deststorepass password -noprompt
keytool -importkeystore -srckeystore truststore -destkeystore truststore.jks  -keypass password -srcstorepass password -deststorepass password
keytool -list -keystore truststore.jks -v -storepass password
# generating .p12
keytool -importkeystore -srckeystore keystore -destkeystore keystore.p12 -deststoretype pkcs12 -keypass password -srcstorepass password -deststorepass password  -validity 1460
keytool -importkeystore -srckeystore truststore -destkeystore truststore.p12 -deststoretype pkcs12 -keypass password -srcstorepass password -deststorepass password
keytool -list -storetype PKCS12 -keystore truststore.p12 -v -storepass password
# generating .pem
openssl pkcs12 -in keystore.p12 -out keystore.pem -passin pass:password -passout pass:password
openssl pkcs12 -in truststore.p12 -out truststore.pem -passin pass:password -passout pass:password
openssl pkcs12 -in keystore.p12 -nodes -nocerts -out rsakey.pem  -passin pass:password -passout pass:password
sed -n '/-----BEGIN CERTIFICATE-----/,/-----END CERTIFICATE-----/p' keystore.pem > certificate.cer
sed -n '/-----BEGIN ENCRYPTED PRIVATE KEY-----/,/-----END ENCRYPTED PRIVATE KEY-----/p' keystore.pem > privatekey.pem



