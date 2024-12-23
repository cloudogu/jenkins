# Release Notes

Im Folgenden finden Sie die Release Notes für das Jenkins-Dogu. 

Technische Details zu einem Release finden Sie im zugehörigen [Changelog](https://docs.cloudogu.com/de/docs/dogus/jenkins/CHANGELOG/).

## [Unreleased]

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
