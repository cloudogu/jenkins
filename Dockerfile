# cesi/scm
FROM registry.cloudogu.com/official/java:11.0.5-1

LABEL NAME="official/jenkins" \
      VERSION="2.222.1-3" \
      maintainer="sebastian.sdorra@cloudogu.com"
# Dockerfile based on https://github.com/cloudbees/jenkins-ci.org-docker/blob/f313389f8ab728d7b4207da36804ea54415c830b/1.580.1/Dockerfile

    # jenkins home configuration
ENV JENKINS_HOME=/var/lib/jenkins \
    # temporary directory for builds
    WORKSPACE_TMP=/tmp \
    # mark as webapp for nginx
    SERVICE_TAGS=webapp \
    # jenkins version
    JENKINS_VERSION=2.222.1 \
    # glibc for alpine version
    GLIBC_VERSION=2.28-r0 \
    SHA256_GLIB_APK="f0a00f56fdee9dc888bafec0bf8f54fb188e99b5346032251abb79ef9c99f079" \
    SHA256_GLIB_BIN_APK="b9a0d8359b12a9768f6378156f160d40f8e432e78e0b2aabc9d0a81e216e7f49" \
    SHA256_GLIB_I18N_APK="948aa0a87b2b93cef561d31c02060a162d592a3545af56171c3f8b0d6f918a48" \
    # additional java version for legacy builds
    ADDITIONAL_OPENJDK_VERSION="8.242.08-r0"

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
