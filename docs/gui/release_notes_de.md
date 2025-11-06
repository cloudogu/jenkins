# Release Notes

Im Folgenden finden Sie die Release Notes für das Jenkins-Dogu.

Technische Details zu einem Release finden Sie im zugehörigen [Changelog](https://docs.cloudogu.com/de/docs/dogus/jenkins/CHANGELOG/).

## [Unreleased]

## [v2.528.1-2] - 2025-11-06
### Changed
* Hinzufügen des optionalen OpenJDK 17 in version `17.0.17_p10`
* Aktualisieren des optionalen OpenJDK 11 zu version `11.0.29_p7`

## [v2.528.1-1] - 2025-10-20
### Changed
* Das Dogu bietet nun die Jenkins-Version 2.528.1 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog/2.528.1/)

## [v2.516.3-2] - 2025-10-14
### Changed
* Im Dogu wurden nur technische Änderungen vorgenommen. Details sind im Changelog aufgeführt.  

## [v2.516.3-1] - 2025-09-19
### Changed
* Das Dogu bietet nun die Jenkins-Version 2.516.3 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog/2.516.3/)

## [v2.516.2-1] - 2025-08-21
### Changed
* Das Dogu bietet nun die Jenkins-Version 2.516.2 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog/2.516.2/)

## [v2.516.1-1] - 2025-08-04
### Changed 
* Das Dogu bietet nun die Jenkins-Version 2.516.1 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog/2.516.1/)

## [v2.504.3-2] - 2025-07-31
### Changed
* Projektbasierte Matrix-Zugriffssteuerung wird bei einem Neustart des Dogus immer gesetzt
  * Ist in der Jenkinsverwaltung eine andere Zugriffssteuerung als die Projekt-Matrix definiert, so wird diese bei einem Dogu-Neustart wieder hergestellt.
  * Die im System definierte Admin-Gruppe wird im Jenkins mit Administrator-Rechten versehen 

## [v2.504.3-1] - 2025-07-01
### Changed 
* Das Dogu bietet nun die Jenkins-Version 2.504.3 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog/2.504.3/)

## [v2.504.2-1] - 2025-06-06
### Changed 
* Das Dogu bietet nun die Jenkins-Version 2.504.2 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog/2.504.2/)

## [v2.504.1-1] - 2025-05-19
### Changed 
* Das Dogu bietet nun die Jenkins-Version 2.504.1 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog/2.504.1/)

## [v2.492.3-5] - 2025-04-24
### Changed
- Die Verwendung von Speicher und CPU wurden für die Kubernetes-Multinode-Umgebung optimiert.

## [v2.492.3-4] - 2025-04-15
### Added
* Das Dogu kann Proxyausnahmen, konfigurierbar mit der globalen Konfiguration `/proxy/no_proxy_hosts`, verarbeiten.

## [v2.492.3-3] - 2025-04-11
### Changed
* Zusätzliche Logger werden nun gesammelt in einem Konfigurationseintrag verwaltet
    * logging/additional_loggers: { "logger-path": "level"}

## [v2.492.3-2] - 2025-04-10 - Eingestelltes Release
### Changed
* Zusätzliche Logger werden nun gesammelt in einem Konfigurationseintrag verwaltet
  * logging/additional_loggers: { "logger-path": "level"}

## [v2.492.3-1] - 2025-04-10
### Changed
* Das Dogu bietet nun die Jenkins-Version 2.492.3 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog-stable/2.492.3/).
### [Security](https://www.jenkins.io/security/advisory/2025-04-02)
- Fix [CVE-2024-3622](https://nvd.nist.gov/vuln/detail/CVE-2024-3622) 
- Fix [CVE-2024-3623](https://nvd.nist.gov/vuln/detail/CVE-2024-3623) 
- Fix [CVE-2024-3624](https://nvd.nist.gov/vuln/detail/CVE-2024-3624)
- Fix [CVE-2024-3625](https://nvd.nist.gov/vuln/detail/CVE-2024-3625)
- Fix [CVE-2024-3626](https://nvd.nist.gov/vuln/detail/CVE-2024-3626)
- Fix [CVE-2024-3627](https://nvd.nist.gov/vuln/detail/CVE-2024-3627)

## [v2.492.2-1] - 2025-04-09
### Changed
* Das Dogu bietet nun die Jenkins-Version 2.492.2 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog-stable/2.492.2/).

## [v2.492.1-0] - 2025-03-26
### Changed
* Das Dogu bietet nun die Jenkins-Version 2.492.1 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog-stable/2.492.1/).

## [v2.479.3-2] - 2025-02-21
### Changed
* Standardplugins können nicht in die Blocklist aufgenommen werden
* OpenJDK 11 für Migrationsskripte in Version 11.0.26_p4-r0 

## [v2.479.3-1] - 2025-02-04
* Migrationskript für die Aktualisierung des CAS-Plugins vor dem Jenkins-Service-Start
* Das Dogu bietet nun die Jenkins-Version 2.479.3 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog-stable/2.479.3/).

## [v2.479.2-2] - 2025-01-22
* Das CAS-Plugin muss nun mindestens die Version 1.7.0 haben.

## [v2.479.2-1] - 2025-01-14
* Das Dogu bietet nun die Jenkins-Version 2.479.2 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog-stable/2.479.2/).
* Maven-Installation auf Version 3.9.9 aktualisiert

## [v2.462.3-3] - 2024-12-23
### Changed
* Die internen Makefiles wurden aktualisiert, um die Versionierung der Release-Notes zu vereinheitlichen.
* Eine Versionsprüfung für das CAS-Dogu wurde hinzugefügt, da Jenkins ab v2.462.3-1 eine CAS-Version >=7.0.5.1-4 benötigt für die Erstellung von Serviceaccounts

## 2.462.3-2
* Es wurde ein Bugfix behoben, bei dem die Einträge aus der Dogu-Konfiguration für updateSiteUrl nicht in Jenkins übernommen wurden.
* Es wurde ein Bugfix behoben, bei dem mehrere Einträge für JDK 17 in den Jenkins Tools bestanden.
* Die OpenJDK11 Version in Jenkins wurde geupdated.

## 2.462.3-1
* Das Dogu bietet nun die Jenkins-Version 2.462.3 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog-stable/2.462.3/).
* Das Dogu bietet nun die Möglichkeit Port 50000 so freizuschalten, dass sich Worker Agents über diesen Port verbinden können.
  * Weitere Infos zu den nötigen Konfigurationsschritten finden sich in [TCP_agent_listener_port_de.md](../operations/TCP_agent_listener_port_de.md).

## 2.462.2-1
* Das Dogu bietet nun die Jenkins-Version 2.462.2 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog-stable/2.462.2/).
* Das Dogu bietet mit /var/lib/jenkins/init.groovy.d/plugin-blocklist.json eine Block-Liste für Plugins in Jenkins an.
  * Versuche Plugins aus der Block-Liste zu installieren, resultieren in einer automatischen Entfernung der Plugins beim Neustart
  * Diese Liste kann über den etcd-key blocked.plugins ebenfalls gepflegt werden, indem eine Komma getrennte Liste an PluginIds übergeben wird

## 2.452.4-2
* Die Cloudogu-eigenen Quellen werden von der MIT-Lizenz auf die AGPL-3.0-only relizensiert.

## 2.452.4-1
* Das Dogu bietet nun die Jenkins-Version 2.452.4 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog-stable/2.452.4/).
* Die Standard JVM im Jenkins ist nun JDK17. JDK11 wird weiterhin unterstützt, mehr Informationen finden Sie [hier](https://docs.cloudogu.com/de/docs/dogus/jenkins/operations/Building_with_custom_Java/).
* Der CVE [CVE-2024-43044](https://nvd.nist.gov/vuln/detail/CVE-2024-43044) wird mit dem Update behoben.

### Breaking Changes
* Durch das Upgrade auf JDK17 müssen die Build Nodes (Agents) ebenfalls auf JDK17 geupdated werden. Weitere Informationen finden Sie [hier](https://www.jenkins.io/doc/book/platform-information/upgrade-java-to-17/#jvm-version-on-agents).
* JDK8 wird vom Jenkins Dogu nicht mehr unterstützt. Hierdurch können keine Projekte mehr mit Java-Version 8 auf dem Jenkins Dogu selbst gebaut werden. Bitte verwenden Sie dedizierte Build Nodes (Agents) falls Sie Java-Version 8 weiterhin nutzen wollen.

## 2.452.2-2
* Behebung von kritischem CVE-2024-41110 in Bibliotheksabhängigkeiten. Diese Schwachstelle konnte jedoch nicht aktiv ausgenutzt werden.

## 2.452.2-1

* Das Dogu bietet nun die Jenkins-Version 2.452.2 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog/#v2.452).
* Der CVE [CVE-2023-48795](https://www.jenkins.io/security/advisory/2024-04-17/) wird mit dem Update behoben

## 2.440.2-1

* Das Dogu bietet nun die Jenkins-Version 2.440.2 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog/#v2.440).
