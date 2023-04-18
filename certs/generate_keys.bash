keytool -genkeypair -alias server-cert -keyalg RSA -keystore keystore -dname "C=FR; ST=GYAN; L=PARIS; O=ORG; OU=UNIT; CN=client" -keypass password -storepass password -ext san=dns:localhost,dns:kafka
keytool -importkeystore -srckeystore keystore -destkeystore keystore.jks  -keypass password -srcstorepass password -deststorepass password  -validity 1460
keytool -export -alias server-cert -keystore keystore -file clientcert.cer -storepass password
keytool -import -alias server-cert -file clientcert.cer -keystore truststore -keypass password -srcstorepass password -deststorepass password -noprompt
keytool -importkeystore -srckeystore truststore -destkeystore truststore.jks  -keypass password -srcstorepass password -deststorepass password
keytool -list -keystore truststore.jks -v -storepass password
