# Log-Level konfigurieren

Es ist möglich, das Log-Level für den Jenkins über die Konfiguration zu steuern. Diese Konfiguration wird bei jedem Start
des Jenkins aktualisiert.

## Konfiguration des root-Loggers

Das Log-Level für den root-Logger wird über den Konfigurationsschlüssel `/config/jenkins/logging/root` eingestellt. Valide
Werte sind `ERROR, WARN, INFO, DEBUG`. Wurde keine Einstellung vorgenommen, wird `WARN` als Standardwert verwendet.

Die Konfiguration kann über die Anwendung `cesapp` mittels des Befehls `edit-config` angepasst werden.

```shell
cesapp edit-config jenkins
```

Alternativ kann die Einstellung auch über die Anwendung `etcdctl` vorgenommen werden.

```shell
etcdctl set /config/jenkins/logging/root INFO
```

## Konfiguration anderer Logger

Neben dem root-Logger können auch weitere Logger über die Konfiguration des Jenkins-Dogus eingestellt werden. Dafür ist 
unter `/config/jenkins/logging` ein Eintrag für jeden gewünschten Logger zu hinterlegen.

Schema: `/config/jenkins/logging/<logger-name> <log-level>`
Beispiel: `/config/jenkins/logging/org.apache.sshd WARN`
