# Dokumentation

Jenkins ist ein Automatisierungsserver, der alle nicht-menschlichen Aspekte der Softwareentwicklung automatisieren kann. So bietet er alle Voraussetzungen für Continuous Integration oder auch Continuous Deployment. Über Plugins kann die Anwendung vielseitig erweitert werden.

Die offizielle Dokumentation dieser Applikation findet sich hier: https://jenkins.io/doc/

## Administrationshinweise: CAS Plugin

Im Bereich *Jenkins verwalten* sind über den Unterpunkt *Plugins verwalten* alle installierten Jenkins-Plugins abrufbar. Diese werden im Reiter *Installiert* angezeigt.

Benutzer mit Administrationsberechtigungen können an dieser Stelle Plugins entsprechend ihrer Abhängigkeiten deinstallieren. Hierbei ist besonders darauf zu achten, dass das **CAS Plugin betriebsnotwendig** ist und damit niemals deinstalliert werden darf. Sollte dieses dennoch entfernt werden, ist ein Start des Jenkins Dogus nicht mehr möglich.

![CAS Plugin im Jenkins](figures/Jenkins_CAS.png)
