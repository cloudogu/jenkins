# Zeitzone konfigurieren

Die Zeitzone des Jenkins-Dogus kann über zusätzliche Java-Parameter konfiguriert werden.

Bearbeiten Sie die Konfiguration des Jenkins-Dogus:

`kubectl edit configmap -n ecosystem jenkins-config`
````yaml
    data:
      config.yaml: |
        additional_java_args: "-Duser.timezone=Europe/Berlin"
````

