# Jenkins Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Changed
- Export port 50000 for inbound agent usage; #178

## [v2.462.2-1] - 2024-09-30
### Added
- Ability to Block-list plugins that should not be installed on the jenkins dogu
  - Block-listed plugins will be automatically removed when found to be installed on dogu start and restart

### Changed
- Upgrade Jenkins to 2.462.2; #171
- Upgrade ces-build-lib to version 2.4.0
- Upgrade dogu-build-lib to version 2.5.0

## [v2.452.4-2] - 2024-09-18
### Changed
- Relicense to AGPL-3.0-only

## [v2.452.4-1] - 2024-08-26
### Changed
- Upgrade Jenkins to 2.452.4
- Use OpenJDK 17 as default JVM (#161)
  - Update docs for "Building with custom OpenJDK versions"

### Removed
- OpenJDK 8 is no longer supported on jenkins controller node - please use dedicated agents to build legacy projects

### Security
- Fix CVE-2024-43044

## [v2.452.2-2] - 2024-08-15
### Changed
- [#166] Upgrade OpenJDK to 11.0.24-1
- Upgrade Alpine to 3.20.2-1
- The previous glibc compatibility layer was changed to Alpine's `gcompat` library
  - this change was made due to incompatibilities with the previous `sgerrand` glibc layer which is no more supported
    for newer Alpine versions

### Security
- [#166] close CVE-2024-41110

## [v2.452.2-1] - 2024-07-10
### Changed
- Upgrade Jenkins to 2.452.2; #164

## [v2.440.2-1] - 2024-03-25
### Changed
- Upgrade Jenkins to 2.440.2; #158

## [v2.426.3-2] - 2024-03-13
### Fixed
- System admin email address will be overwritten with default value (#156)

## [v2.426.3-1] - 2024-01-26
### Changed
- Upgrade Jenkins to 2.426.3; #154
  - Fix CVE-2024-23897
- Upgrade openjdk8 additional package to 8.392.08

## [v2.414.3-1] - 2023-10-23
### Changed
- Upgrade Jenkins to 2.414.3 (#151)
  - Fix CVE-2023-36478 and CVE-2023-44487

## [v2.414.2-1] - 2023-09-25
### Added
- Azure agent setup [documentation](docs/operations/azure_agents_en.md)

### Changed
- Update Jenkins to 2.414.2 (#149)
- Update makefiles to 8.4.0

## [v2.401.3-1] - 2023-07-31
### Added
- Allow the configuration of the log level for different loggers (#147)

### Changed
- Upgrade to Jenkins 2.401.3; #147

## [v2.401.2-1] - 2023-07-24

This release does not contain any changes! It just fixes the version after the upgrade to Jenkins 2.401.2.

## [v2.401.1-2] - 2023-07-20
### Changed
- Upgrade to Jenkins 2.401.2 (#144)
- Update dogu-build-lib to latest version to v2.2.0 (#144)

### Added
- Add possibility to configure additional java flags (#129)

## [v2.401.1-1] - 2023-07-10
### Changed
- Upgrade to Jenkins 2.401.1; #139
- Upgrade java base image to  11.0.19-1
- Upgrade ces-build-lib to 1.65.0
- Change dogu-build-lib-version to `v2.2.0`

## [v2.387.1-6] - 2023-06-27
### Added
- [#142] Configuration options for resource requirements
- [#142] Defaults for CPU and memory requests

## [v2.387.1-5] - 2023-06-05
### Changed
- Use java 11 instead of 17 because of plugin incompatibility; #137

## [v2.387.1-4] - 2023-05-30
### Fixed
- Add missing additional OpenJDK11 11.0.19_p7-r0; #135

## [v2.387.1-3] - 2023-05-26
### Change
- Upgraded to java base image 17.0.6-2; #133

## [v2.387.1-2] - 2023-05-08
### Added
- Add trivy scan in Jenkins pipeline

### Changed
- Upgrade to java base image 11.0.18-1
- Upgrade additional OpenJDK8 to 8.372.07-r0
- Upgrade glibc to 2.35-r1
- Upgrade ces-build-lib to 1.64.2
- Upgrade dogu-build-lib to 2.1.0

## [v2.387.1-1] - 2023-03-13
### Changed
- [#127] Upgrade Jenkins to `2.387.1`
- Upgrade `ces-build-lib` to `1.62.0`
- Upgrade `dogu-build-lib` to `v2.0.0`

## [v2.375.2-2] - 2023-02-17
### Changed
- Upgrade `M3` maven to 3.9.0; #125

## [v2.375.2-1] - 2023-01-17
### Changed
- Upgrade to Jenkins 2.375.2; #123
- Upgrade dogu-build-lib to v1.8.0; #120

## [v2.361.1-2] - 2022-10-12
### Changed
- [#119] Upgrade `dogu-build-lib` to version `1.7.0`.

### Fixed
- [#119] Fix script to create authentication strategy. This resolves the problem that no user could log into the dogu
   after changing the CES global admin group.

## [v2.361.1-1] - 2022-10-05
### Changed
- Upgrade to Jenkins 3.361.1 (#117)
- Upgrade additional OpenJDK8 to 8.345.01-r0
- Upgrade ces-build-lib to v1.56.0
- Upgrade makefiles to version 7.0.1

### Added
- Markdown link check in Jenkinsfile
- Bats stage in Jenkinsfile

## [v2.346.1-1] - 2022-06-27
### Changed
- Upgrade Jenkins to lts version 2.346.1 (#115)
  - This will fix CVE-2022-22950 and CVE-2022-22965 among other things (#110)
- Update openjdk8 version to 8.322.06-r0

## [v2.332.1-3] - 2022-04-21
### Fixed
- Fixed an issue that would break the jenkins startup scripts if the version of the matrix-auth plugin was below 3.0  #113

## [v2.332.1-2] - 2022-04-19
### Changed
- update initscript `init050CRITsetauthorization.groovy` to use the latest changes from the auth-matrix plugin (#111)

## [v2.332.1-1] - 2022-04-07
### Added
- Install pipeline-stage-view plugin on Dogu start

### Changed
- Upgrade to Jenkins 2.332.1 #106

## [v2.319.2-3] - 2022-04-05
### Changed
- Upgrade java base image to 11.0.14-3

### Fixed
- Upgrade zlib to fix CVE-2018-25032; #108

## [v2.319.2-2] - 2022-02-07
### Changed
- Update to OpenJDK 11.0.14 (Base image: [v11.0.14-1](https://github.com/cloudogu/java/releases/tag/v11.0.14-1))

## [v2.319.2-1] - 2022-01-14
### Changed
- Upgrade to Jenkins 2.319.2 #101
- Upgrade dogu-build-lib to 1.6.0

### Fixed
- Fix certificate creation for subversion at startup
- Patch JDK to 8.302.08-r1 to fix the jenkins build

## [v2.303.3-1] - 2021-11-10
### Changed
- Upgrade to Jenkins 2.303.3; #98
- Upgrade dogu.json format to v2
- Upgrade to dogu-integration-test-lib 1.0.0 and Cypress 8.7.0

## [v2.303.2-1] - 2021-10-07
### Changed
- Upgrade to Jenkins 2.303.2; #96

## [v2.289.2-2] - 2021-09-16
### Added
- Add support for additional SSL certificates to the Java truststore so that communication external services with self-signed certificates is no longer rejected (#94)
- Add support for additional SSL certificate to curl and subversion (#94)

### Changed
- Upgrade to Java base image 11.0.11-2

## [v2.289.2-1] - 2021-07-27
### Changed
- changed integrationTest framework to cypress/cucumber; #90
- Upgrade to Jenkins 2.289.2; #90

## [v2.277.4-2] - 2021-07-09
### Changed
- Configure cas plugin so that proxy tickets can be used #91

## [v2.277.4-1] - 2021-06-02
### Changed
- Upgrade to Jenkins 2.277.4; #88
- Upgrade glibc to 2.33
- Configure OpenJDK-11 tool in addition to OpenJDK-8

## [v2.277.3-2] - 2021-05-18
### Changed
- Upgrade `M3` maven to 3.8.1; #86

## [v2.277.3-1] - 2021-04-29
### Changed
- Upgrade to Jenkins 2.277.3 LTS; #84

## [v2.263.3-1] - 2021-02-01
### Changed
- Upgrade to Jenkins 2.263.3 LTS; #81

## [v2.263.2-1] - 2021-01-21
### Changed
- Upgrade to Jenkins 2.263.2 LTS; #79
- Update dogu-build-lib to `v1.1.1`
- Update zalenium-build-lib to `v2.1.0`
- Toggle video recording with build parameter (#76)

### Added
- Check if changelog has been extended on a Jenkins build for a pull request branch

## [v2.249.3-2] - 2020-12-14
### Added
- Ability to set memory limit via `cesapp edit-config`
- Ability to configure the `MaxRamPercentage` and `MinRamPercentage` for the Jenkins process inside the container via `cesapp edit-conf` (#74)

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
