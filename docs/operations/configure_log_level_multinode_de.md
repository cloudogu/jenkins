# Log-Level konfigurieren

Es ist möglich, das Log-Level für den Jenkins über die Konfiguration zu steuern. Diese Konfiguration wird bei jedem Start
des Jenkins aktualisiert.

## Konfiguration des root-Loggers

Das Log-Level für den root-Logger wird über den Konfigurationsschlüssel `logging/root` eingestellt. Valide
Werte sind `ERROR, WARN, INFO, DEBUG`. Wurde keine Einstellung vorgenommen, wird `WARN` als Standardwert verwendet.

Die Konfiguration kann über kubectl angepasst werden:

`kubectl edit configmap -n ecosystem jenkins-config`
````yaml
    data:
      config.yaml: |
        logging:
          root: INFO
````


## Konfiguration anderer Logger

Neben dem root-Logger können auch weitere Logger über die Konfiguration des Jenkins-Dogus eingestellt werden. Dafür ist 
unter `logging` ein Eintrag für jeden gewünschten Logger zu hinterlegen.

Schema: `logging/<logger-name> <log-level>`
Beispiel: `logging/org.apache.sshd WARN`
