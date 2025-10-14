# Stage 1: get doguctl from Cloudogu base (Alpine 3.22)
FROM registry.cloudogu.com/official/java:21.0.5-1 AS doguctl

RUN echo "Just retrieve doguctl from this image!"

# Stage 2: main JRE (Alpine 3.22)
FROM eclipse-temurin:21-jre-alpine-3.22

LABEL NAME="official/jenkins" \
      VERSION="2.516.3-1" \
      maintainer="hello@cloudogu.com"

ENV JENKINS_HOME=/var/lib/jenkins \
    WORKSPACE_TMP=/tmp \
    SERVICE_TAGS=webapp \
    SERVICE_8080_TAGS="webapp" \
    SERVICE_8080_NAME="jenkins" \
    JENKINS_VERSION="2.516.3" \
    SHA256_JENKINS_WAR="81b3abcc0f24cea48e74effe152f69dc5f0d880edc0c2737c61446b3c5992c00" \
    ADDITIONAL_OPENJDK11_VERSION="11.0.28_p6-r0"

# copy doguctl + helper scripts into PATH
COPY --from=doguctl /usr/bin/doguctl /usr/bin/doguctl
COPY --from=doguctl /usr/bin/create-ca-certificates.sh /usr/bin/create-ca-certificates.sh
COPY --from=doguctl /usr/bin/create_truststore.sh /usr/bin/create_truststore.sh
RUN chmod 0755 /usr/bin/doguctl /usr/bin/create-ca-certificates.sh /usr/bin/create_truststore.sh

# bring in bash (startup.sh uses bashisms) + first resource copy
RUN apk add --no-cache bash
COPY resources/ /
# after installing bash / copying scripts, before switching USER
RUN sh -lc 'mkdir -p "$JAVA_HOME/jre/lib/security" \
  && [ -f "$JAVA_HOME/lib/security/cacerts" ] \
  && ln -sf "$JAVA_HOME/lib/security/cacerts" "$JAVA_HOME/jre/lib/security/cacerts"'

# Jenkins is ran with user `jenkins`, uid = 1000
# If you bind mount a volume from host/volume from a data container,
# ensure you use same uid
RUN set -o errexit \
 && set -o nounset \
 && set -o pipefail \
 && apk update \
 && apk upgrade \
 && addgroup -S -g 1000 jenkins \
 && adduser -S -h "$JENKINS_HOME" -s /bin/bash -G jenkins -u 1000 jenkins \
 # install coreutils, ttf-dejavu, openssh and scm clients
 # coreutils and ttf-dejavu is required because of java.awt.headless problem:
 # - https://wiki.jenkins.io/display/JENKINS/Jenkins+got+java.awt.headless+problem
 && apk add --no-cache coreutils ttf-dejavu openssh-client git subversion mercurial curl gcompat \
 && apk add openjdk11="$ADDITIONAL_OPENJDK11_VERSION" \
 # could use ADD but this one does not check Last-Modified header
 # see https://github.com/docker/docker/issues/8331
 && curl -L https://mirrors.jenkins-ci.org/war-stable/${JENKINS_VERSION}/jenkins.war -o /jenkins.war \
 && echo "${SHA256_JENKINS_WAR} *jenkins.war" | sha256sum -c - \
 # set git system ca-certificates
 && git config --system http.sslCaInfo /var/lib/jenkins/ca-certificates.crt \
 # set mercurial system ca-certificates
 && mkdir /etc/mercurial \
 && printf "[web]\ncacerts = /var/lib/jenkins/ca-certificates.crt\n" > /etc/mercurial/hgrc \
 # set subversion system ca-certificates
 && mkdir /etc/subversion \
 && printf "[global]\nssl-authority-files=/var/lib/jenkins/ca-certificates.crt\n" > /etc/subversion/server \
 # install glibc for alpine
 # make sure that jenkins is able to execute Oracle JDK, which can be installed over the global tool installer
 && apk add --no-cache libstdc++ gcompat

 RUN (/usr/glibc-compat/bin/localedef --force --inputfile POSIX --charmap UTF-8 C.UTF-8 || true )

# Jenkins home directoy is a volume, so configuration and build history
# can be persisted and survive image upgrades
VOLUME /var/lib/jenkins

# add jenkins config file template, including changes for cas plugin and mailConfiguration
COPY ./resources /

# ensure startup.sh has correct line endings & perms AFTER the final COPY
RUN sed -i 's/\r$//' /startup.sh && chmod +x /startup.sh

# prove doguctl is runnable at build time
RUN doguctl version >/dev/null || true

# switch to jenkins user
USER jenkins

# for main web interface:
EXPOSE 8080 50000

HEALTHCHECK --interval=5s CMD doguctl healthy jenkins || exit 1

# start jenkins
CMD ["/startup.sh"]
