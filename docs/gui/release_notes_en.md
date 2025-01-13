# Release Notes

Below you will find the release notes for the Jenkins Dogu.

Technical details on a release can be found in the corresponding [Changelog](https://docs.cloudogu.com/en/docs/dogus/jenkins/CHANGELOG/).

## [Unreleased]
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
