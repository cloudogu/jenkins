MAKEFILES_VERSION=7.10.0

.DEFAULT_GOAL:=dogu-release

include build/make/variables.mk
include build/make/self-update.mk
include build/make/clean.mk
include build/make/release.mk
include build/make/bats.mk
include build/make/version-sha.mk

JENKINS_VERSION=$(shell grep JENKINS_VERSION= Dockerfile | sed 's/.*JENKINS_VERSION=\([^ ]*\).*/\1/g')
GLIBC_VERSION=$(shell grep GLIBC_VERSION= Dockerfile | sed 's/.*GLIBC_VERSION=\([^ ]*\).*/\1/g')

.PHONY: sums
sums: ## Print out all versions
	@echo "Jenkins version"
	@make --no-print-directory sha-sum SHA_SUM_URL=https://mirrors.jenkins-ci.org/war-stable/${JENKINS_VERSION}/jenkins.war
	@echo "GCLib-APK"
	@make --no-print-directory sha-sum SHA_SUM_URL="https://github.com/sgerrand/alpine-pkg-glibc/releases/download/${GLIBC_VERSION}/glibc-${GLIBC_VERSION}.apk"
	@echo "GCLib-bin-APK"
	@make --no-print-directory sha-sum SHA_SUM_URL="https://github.com/sgerrand/alpine-pkg-glibc/releases/download/${GLIBC_VERSION}/glibc-bin-${GLIBC_VERSION}.apk"
	@echo "GCLib-i18n-APK"
	@make --no-print-directory sha-sum SHA_SUM_URL="https://github.com/sgerrand/alpine-pkg-glibc/releases/download/${GLIBC_VERSION}/glibc-i18n-${GLIBC_VERSION}.apk"

