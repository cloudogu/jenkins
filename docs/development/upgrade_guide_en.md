# Upgrade base image

If the base image has to be upgraded and the java version changes, jenkins still needs the previous openjdk version
to support legacy builds. Download the old openjdk version with `apk add openjdk<major version>=<version>` in
the Dockerfile and extend the version in the openjdk installation groovy script `init100NORMjdkautoinstall.groovy`.