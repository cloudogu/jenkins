# Release Notes

Below you will find the release notes for the Jenkins Dogu.

Technical details on a release can be found in the corresponding [Changelog](https://docs.cloudogu.com/en/docs/dogus/jenkins/CHANGELOG/).

## [Unreleased]

## [v2.516.3-2] - 2025-10-14
### Changed
* Only technical changes were made in this Dogu. For more details, see the changelog.

## [v2.516.3-1] - 2025-09-19
### Changed
* The Dogu now provides the Jenkins version 2.516.3. You can find the Jenkins release notes [here](https://www.jenkins.io/changelog/2.516.3).

## [v2.516.2-1] - 2025-08-21
### Changed
* The Dogu now provides the Jenkins version 2.516.2. You can find the Jenkins release notes [here](https://www.jenkins.io/changelog/2.516.2).

## [v2.516.1-1] - 2025-08-04
### Changed
* The Dogu now provides the Jenkins version 2.516.1. You can find the Jenkins release notes [here](https://www.jenkins.io/changelog/2.516.1).

## [v2.504.3-2] - 2025-07-31
### Changed
* Project-based matrix access control is always applied when the Dogu is restarted.
  * If a different access control than the project matrix is defined in the Jenkins configuration, it will be restored upon a Dogu restart.
  * The admin group defined in the system is granted administrator rights in Jenkins.

## [v2.504.3-1] - 2025-07-01
### Changed
* The Dogu now provides the Jenkins version 2.504.3. You can find the Jenkins release notes [here](https://www.jenkins.io/changelog/2.504.3).

## [v2.504.2-1] - 2025-06-06
### Changed
* The Dogu now provides the Jenkins version 2.504.2. You can find the Jenkins release notes [here](https://www.jenkins.io/changelog/2.504.2/).

## [v2.504.1-1] - 2025-05-19
### Changed
* The Dogu now provides the Jenkins version 2.504.1. You can find the Jenkins release notes [here](https://www.jenkins.io/changelog/2.504.1/).

## [v2.492.3-5] - 2025-04-24
### Changed
- Usage of memory and CPU was optimized for the Kubernetes Mutlinode environment.

## [v2.492.3-4] - 2025-04-15
### Added
* The dogu can handle proxy exclusions, configurable via global config `proxy/no_proxy_hosts`.

## [v2.492.3-3] - 2025-04-11
### Changed
* Additional loggers are now managed together in a unified configuration-entry
    * logging/additional_loggers: { "logger-path": "level"}

## [v2.492.3-2] - 2025-04-10 - Discontinued Release
### Changed
* Additional loggers are now managed together in a unified configuration-entry
  * logging/additional_loggers: { "logger-path": "level"}

## [v2.492.3-1] - 2025-04-10
### Changed
* The Dogu now provides the Jenkins version 2.492.3. You can find the Jenkins release notes [here](https://www.jenkins.io/changelog-stable/2.492.3/).
### [Security](https://www.jenkins.io/security/advisory/2025-04-02)
- Fix [CVE-2024-3622](https://nvd.nist.gov/vuln/detail/CVE-2024-3622) 
- Fix [CVE-2024-3623](https://nvd.nist.gov/vuln/detail/CVE-2024-3623) 
- Fix [CVE-2024-3624](https://nvd.nist.gov/vuln/detail/CVE-2024-3624)
- Fix [CVE-2024-3625](https://nvd.nist.gov/vuln/detail/CVE-2024-3625)
- Fix [CVE-2024-3626](https://nvd.nist.gov/vuln/detail/CVE-2024-3626)
- Fix [CVE-2024-3627](https://nvd.nist.gov/vuln/detail/CVE-2024-3627)

## [v2.492.2-1] - 2025-04-09
### Changed
* The Dogu now provides the Jenkins version 2.492.2. You can find the Jenkins release notes [here](https://www.jenkins.io/changelog-stable/2.492.2/).

## [v2.492.1-0] - 2025-03-26
### Changed
* The Dogu now provides the Jenkins version 2.492.1. You can find the Jenkins release notes [here](https://www.jenkins.io/changelog-stable/2.492.1/).

## [v2.479.3-2] - 2025-02-21
### Changed
* Default-Plugins can not be added to the blocklist
* Use OpenJDK 11.0.26-p4-r0 from Alpine for migrationscripts

## [v2.479.3-1] - 2025-02-04
* The Dogu now provides the Jenkins version 2.479.3. You can find the Jenkins release notes [here](https://www.jenkins.io/changelog-stable/2.479.3/).
* Migration-script for upgrading CAS-Plugin to 1.7.0 before Jenkins-Service-Start

## [v2.479.2-2] - 2025-01-22
* The CAS-Plugin has to be at least version 1.7.0.

## [v2.479.2-1] - 2025-01-14
* The Dogu now provides the Jenkins version 2.479.2. You can find the Jenkins release notes [here](https://www.jenkins.io/changelog-stable/2.479.2/).
* Update Maven-Installation to version 3.9.9

## [v2.462.3-3] - 2024-12-23
### Changed
* The internal makefiles have been updated to standardize the versioning of the release notes.
* A version check has been added for the CAS dogu, as Jenkins since version v2.462.3-1 needs the CAS dogu in version >=7.0.5.1-4 to create service accounts

## 2.462.3-2
* Fixed a bug where the entries from the Dogu configuration for updateSiteUrl were not applied to Jenkins.
* Fixed a bug where multiple entries for JDK 17 exists in the Jenkins tools.
* The OpenJDK11 version in Jenkins has been updated.

## 2.462.3-1
* The Dogu now provides the Jenkins version 2.462.3. You can find the Jenkins release notes [here](https://www.jenkins.io/changelog-stable/2.462.3/).
* The Dogu is now capable to use port 50000 for inbound agent connections.
  * For further information on how to set this up see [TCP_agent_listener_port_en.md](../operations/TCP_agent_listener_port_en.md).

## 2.462.2-1
* The Dogu now provides the Jenkins version 2.462.2. You can find the Jenkins release notes [here](https://www.jenkins.io/changelog-stable/2.462.2/).
* With /var/lib/jenkins/init.groovy.d/plugin-blocklist.json, the Dogu offers a block list for plugins in Jenkins.
    * Attempts to install plugins from the block list will result in an automatic removal after a restart
    * This list can also be maintained via the etcd-key blocked.plugins by passing a comma-separated list of PluginIds

## 2.452.4-2
* Relicense own code to AGPL-3-only

## 2.452.4-1
* The Dogu now provides the Jenkins version 2.452.4. You can find the Jenkins release notes [here](https://www.jenkins.io/changelog-stable/2.452.4/).
* The default JVM in Jenkins is now JDK17. JDK8 and JDK11 are still supported, for more information see [here](https://docs.cloudogu.com/en/docs/dogus/jenkins/operations/Building_with_custom_Java/).
* The CVE [CVE-2024-43044](https://nvd.nist.gov/vuln/detail/CVE-2024-43044) will be fixed with this update.

### Breaking Changes
* By upgrading to JDK17, the build nodes (agents) must also be updated to JDK17. Further information can be found [here](https://www.jenkins.io/doc/book/platform-information/upgrade-java-to-17/#jvm-version-on-agents).
* JDK8 is no longer supported by the Jenkins Dogu. This means that projects with Java version 8 can no longer be built on the Jenkins Dogu itself. Please use dedicated build nodes (agents) if you want to continue using Java version 8.

## 2.452.2-2
* Fix of critical CVE-2024-41110 in library dependencies. This vulnerability could not be actively exploited, though.

## 2.452.2-1

* The Dogu now offers the Jenkins version 2.452.2. The Jenkins release notes can be found [here](https://www.jenkins.io/changelog/#v2.452).
* The CVE [CVE-2023-48795](https://www.jenkins.io/security/advisory/2024-04-17/) will be fixed with the update

## 2.440.2-1

* The Dogu now offers the Jenkins version 2.440.2. The release notes of Jenkins can be found [here](https://www.jenkins.io/changelog/#v2.440).
