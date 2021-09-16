# Additional SSL certificates

## Introduction

SSL certificates help secure encrypted communications between different services and servers. If a certificate cannot be verified by the _Root Certificate Authority (CA)_ certificates, then this may indicate an attack. A second possibility for failed certificate verification could be a self-signed certificate (such as that of a second CES instance).

In such cases, tools such as `git` or `curl` refuse to work because a safely encrypted communication to the desired services is not possible. There are often several possible solutions to this problem.

One of these approaches is to disable SSL certificate verification altogether. This approach has the strong disadvantage that often no certificates are checked at all. Malicious attacks would thus no longer be detectable.

A second solution approach is to recognize the affected certificates as secure. The services and tools can then secure their regular communications so that attacks from outside can be sensibly detected.

This document describes the second approach.

## Importing additional certificates

Inside the dogus, for the regular execution of Jenkins, there is the possibility to add additional certificates from the `etcd`.

There are some conditions to successfully find the certificates and apply them to the dogu.

1. the certificates must be in PEM format.
2. the certificates must be present in `etcd` below `/config/_global/certificate/additional/`.
   - The key name (also called _alias_) is used for addressing and dogu-internal storage and should not contain any spaces.
   - It would make sense to use the FQDN of the service (e.g. `service.example.com`), so that a certificate can be removed more easily later.
   - A key can have more than one certificate for a service. Certificates in PEM format have textual markers that can be used to separate the certificates again.
3. the key name under which the certificate was stored must be stored in `etcd` under `/config/_global/certificate/additional/toc`.
   - Certificates of different services must be separated with a single space character


Example configuration in `etcd`:

```
config/
└─ _global/
   └─ certificate/
      └─ additional/
         ├─ toc          -> "example.com localserver2 server3"
         ├─ example.com  -> "-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----"
         ├─ localserver2 -> "-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----"
         └─ server3      -> "-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----"
```

## Storing additional certificates in the Jenkins dogu

After a successful import, the certificates will be in these locations:

name | file path | stored certificates
-----|-----------|----------------------
CA certificate collection | `/var/lib/jenkins/ca-certificates.crt` | Both the standard CA certificates of the operating system from `/etc/ssl/certs/` and the additional certificates
Java Truststore | `/var/lib/jenkins/truststore.jks` | Both the operating system's default CA certificates from `/etc/ssl/certs/` and the additional certificates
Subversion single certificates | `/var/lib/jenkins/.subversion/cert-$alias-00` | additional certificates; if there are several certificates per alias, there will be several files which differ in the counter suffix
