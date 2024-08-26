# Release Notes

Im Folgenden finden Sie die Release Notes für das Jenkins-Dogu. 

Technische Details zu einem Release finden Sie im zugehörigen [Changelog](https://docs.cloudogu.com/de/docs/dogus/jenkins/CHANGELOG/).

## Release 2.452.4-1
* Das Dogu bietet nun die Jenkins-Version 2.452.4 an. Die Release Notes von Jenkins finden Sie [hier](https://https://www.jenkins.io/changelog-stable/2.452.4/).
* Die Standard JVM im Jenkins ist nun JDK17. JDK11 wird weiterhin unterstützt, mehr Informationen finden Sie [hier](https://docs.cloudogu.com/de/docs/dogus/jenkins/operations/Building_with_custom_Java/).
* Der CVE [CVE-2024-43044](https://nvd.nist.gov/vuln/detail/CVE-2024-43044) wird mit dem Update behoben.

### Breaking Changes
* Durch das Upgrade auf JDK17 müssen die Build Nodes (Agents) ebenfalls auf JDK17 geupdated werden. Weitere Informationen finden Sie [hier](https://www.jenkins.io/doc/book/platform-information/upgrade-java-to-17/#jvm-version-on-agents).
* JDK8 wird vom Jenkins Dogu nicht mehr unterstützt. Hierdurch können keine Projekte mehr mit Java-Version 8 auf dem Jenkins Dogu selbst gebaut werden. Bitte verwenden Sie dedizierte Build Nodes (Agents) falls Sie Java-Version 8 weiterhin nutzen wollen.

## Release 2.452.2-2
* Behebung von kritischem CVE-2024-41110 in Bibliotheksabhängigkeiten. Diese Schwachstelle konnte jedoch nicht aktiv ausgenutzt werden.

## Release 2.452.2-1

* Das Dogu bietet nun die Jenkins-Version 2.452.2 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog/#v2.452).
* Der CVE [CVE-2023-48795](https://www.jenkins.io/security/advisory/2024-04-17/) wird mit dem Update behoben

## Release 2.440.2-1

* Das Dogu bietet nun die Jenkins-Version 2.440.2 an. Die Release Notes von Jenkins finden Sie [hier](https://www.jenkins.io/changelog/#v2.440).