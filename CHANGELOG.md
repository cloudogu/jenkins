# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Ability to set memory limit via `cesapp edit-config`
- Ability to configure the `MaxRamPercentage` and `MinRamPercentage` for the CAS process inside the container via `cesapp edit-conf` (#74)

## [v2.249.3-1] - 2020-11-16
### Changed
- Upgrade to Jenkins 2.249.3 LTS; #72
- Upgrade java base image to 11.0.5-4
- If the CES admin group changes, the old admin group loses all permissions in Jenkins now

Attention! DO NOT change the CES global admin group and upgrade the dogu at the same time!
If you have changed the global admin group, restart the Jenkins dogu before upgrading it!

### Fixed
- The init090NORMmavenautoinstall.groovy script does no longer crash when the default `M3` maven installer has been adjusted by the Jenkins administrator; #70

## [v2.235.5-2] - 2020-10-22
### Changed
- Split init scripts into critical and normal ones and only stop Jenkins startup if critical ones fail; #68

## [v2.235.5-1] - 2020-09-04
### Changed
- Upgrade to Jenkins 2.235.5 LTS; #66
- Upgrade java base image to 11.0.5-3

## [v2.235.2-2] - 2020-08-19
### Fixed 
- Fix overwriting of custom installed jdks in `init100jdkautoinstall.groovy`

## [v2.235.2-1] - 2020-07-16
### Changed
- Upgrade to Jenkins 2.235.2 LTS; #61
- Upgrade java base image to 11.0.5-2

### Added
- Add optional dogu upgrade test to Jenkins pipeline

## [v2.222.4-1] - 2020-06-10
### Changed
- Upgrade to Jenkins 2.222.4; #57

## [v2.222.1-3] - 2020-05-07
### Changed
- The tool M3 gets automatically upgraded from 3.5 to 3.6.3 on startup

## [v2.222.1-2] - 2020-04-22

**Attention:** From this release on, OpenJDK 11 is the default Java Development Kit version!
If you want to keep using OpenJDK 8 in legacy builds, you have to follow the steps explained in the [README](https://github.com/cloudogu/jenkins/blob/develop/README.md#building-with-openjdk-8)

### Changed
- Update to OpenJDK 11 (Base image: [v11.0.5-1](https://github.com/cloudogu/java/releases/tag/v11.0.5-1)) #51

### Added
- OpenJDK-8 for legacy builds

## [v2.222.1-1] - 2020-04-21
### Added
- Added scm-manager plugin
- Volume for /tmp directory. This volume is not included in backups!

### Changed
- Upgrade to Jenkins 2.222.1; #52
- Upgrade java base image to 8u242-1

## [v2.190.3-3] - 2020-02-26
### Added
- config key additional.plugins which may contain a comma separated list with plugin names that are installed on startup

## [v2.190.3-2] - 2020-01-24
### Changed
* Apply updateSite changes on every start (#45)

## [v2.190.3-1] - 2019-12-10
### Changed
- Changed Jenkins version to 2.190.3
- Changed Java version to 8u222-1
