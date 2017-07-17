# loom-tm-adapter
Loom adapter for HPE's The Machine.  For more information on Loom look at its [github repo](https://github.com/HewlettPackard/loom).  This adapter is packaged for deployment by [loom-tm](https://github.com/HewlettPackard/loom-tm)

## Building using maven
Install Java 1.8

Install maven v3.3.9 upwards.

To build:

```
$ mvn clean install
```

Note that to ensure that the latest SNAPSHOT dependencies are being used the `-U` option should be used:

```sh
$ mvn -U clean install
```

## Authentication

If an email address is provided when asked for a username during login, the user will be authenticated against the Enterprise Directory configured in the properties file.
The SSL connection to the LDAP service will fail unless a trust store is provided containing its X.509 certificate.
Given a certificate, e.g.`cert.cer`, a trust store can be created so:

```
$ keytool –import -file cert.cer -keystore truststore
```

To tell the JVM where the trust store is use set the `javax.net.ssl.trustStore` system property, e.g.:

```
$ java -Djavax.net.ssl.trustStore=truststore ...
```

If the trust store is not present, or a valid CA certificate cannot be found the following exception may be seen: `javax.naming.CommunicationException: simple bind failed: ldap:636 [Root exception is javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target]`

