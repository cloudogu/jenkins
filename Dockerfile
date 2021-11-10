# cesi/scm
FROM registry.cloudogu.com/official/java:11.0.11-2

LABEL NAME="official/jenkins" \
      VERSION="2.303.2-1" \
      maintainer="hello@cloudogu.com"

    # jenkins home configuration
ENV JENKINS_HOME=/var/lib/jenkins \
    # temporary directory for builds
    WORKSPACE_TMP=/tmp \
    # mark as webapp for nginx
    SERVICE_TAGS=webapp \
    # jenkins version
    JENKINS_VERSION=2.303.3 \
    # glibc for alpine version
    GLIBC_VERSION=2.33-r0 \
    SHA256_GLIB_APK="3ce2b708b17841bc5978da0fa337fcb90fec5907daa396585db68805754322e0" \
    SHA256_GLIB_BIN_APK="f862ce6d61b294859f3facd1dae347e7bc5e36714649a37d2a785e7d7a3af84e" \
    SHA256_GLIB_I18N_APK="be3a55e6366a2ddaecf17203a7e71966757dca47a25ce34b5f3d6dd1e1efee55" \
    SHA256_JENKINS_WAR="8a6ae7367755b3f31a050faa945f7a3991abdb43d941c7294cac890c1e2779d8" \
    # additional java version for legacy builds
    ADDITIONAL_OPENJDK_VERSION="8.282.08-r1"

# Jenkins is ran with user `jenkins`, uid = 1000
# If you bind mount a volume from host/volume from a data container,
# ensure you use same uid
RUN set -x \
 && addgroup -S -g 1000 jenkins \
 && adduser -S -h "$JENKINS_HOME" -s /bin/bash -G jenkins -u 1000 jenkins \
 # install coreutils, ttf-dejavu, openssh and scm clients
 # coreutils and ttf-dejavu is required because of java.awt.headless problem:
 # - https://wiki.jenkins.io/display/JENKINS/Jenkins+got+java.awt.headless+problem
 && apk add --no-cache coreutils ttf-dejavu openssh-client git subversion mercurial \
 && apk add openjdk8="$ADDITIONAL_OPENJDK_VERSION" \
 # could use ADD but this one does not check Last-Modified header
 # see https://github.com/docker/docker/issues/8331
 && curl -L http://mirrors.jenkins-ci.org/war-stable/${JENKINS_VERSION}/jenkins.war -o /jenkins.war \
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
 && apk add --no-cache libstdc++ \
 && curl -Lo /tmp/glibc.apk "https://github.com/sgerrand/alpine-pkg-glibc/releases/download/${GLIBC_VERSION}/glibc-${GLIBC_VERSION}.apk" \
 && echo "${SHA256_GLIB_APK} */tmp/glibc.apk" |sha256sum -c - \
 && apk add --no-cache --allow-untrusted /tmp/glibc.apk \
 && curl -Lo /tmp/glibc-bin.apk "https://github.com/sgerrand/alpine-pkg-glibc/releases/download/${GLIBC_VERSION}/glibc-bin-${GLIBC_VERSION}.apk" \
 && echo "${SHA256_GLIB_BIN_APK} */tmp/glibc-bin.apk" |sha256sum -c - \
 && apk add --no-cache --allow-untrusted /tmp/glibc-bin.apk \
 && curl -Lo /tmp/glibc-i18n.apk "https://github.com/sgerrand/alpine-pkg-glibc/releases/download/${GLIBC_VERSION}/glibc-i18n-${GLIBC_VERSION}.apk" \
 && echo "${SHA256_GLIB_I18N_APK} */tmp/glibc-i18n.apk" |sha256sum -c - \
 && apk add --no-cache --allow-untrusted /tmp/glibc-i18n.apk \
 # do not abort https://github.com/sgerrand/alpine-pkg-glibc/issues/5
 && (/usr/glibc-compat/bin/localedef --force --inputfile POSIX --charmap UTF-8 C.UTF-8 || true ) \
 && echo "export LANG=C.UTF-8" > /etc/profile.d/locale.sh \
 && /usr/glibc-compat/sbin/ldconfig /lib /usr/glibc-compat/lib \
 # cleanup
 && rm -rf /tmp/* /var/cache/apk/*

# Jenkins home directoy is a volume, so configuration and build history
# can be persisted and survive image upgrades
VOLUME /var/lib/jenkins

# add jenkins config file template, including changes for cas plugin and mailConfiguration
COPY ./resources /

# switch to jenkins user
USER jenkins

# for main web interface:
EXPOSE 8080

HEALTHCHECK CMD doguctl healthy jenkins || exit 1

# start jenkins
CMD ["/startup.sh"]
