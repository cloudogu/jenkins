# Zeitzone konfigurieren

Die Zeitzone des Jenkins-Dogus kann über zusätzliche Java-Parameter konfiguriert werden.

Bearbeiten Sie die Konfiguration des Jenkins-Dogus:

```bash
cesapp edit-config jenkins
```

Setzen Sie den Konfigurationsschlüssel `additional_java_args` auf den gewünschten JVM-Parameter für die Zeitzone, zum Beispiel:

```
additional_java_args: "-Duser.timezone=Europe/Berlin"
```

Nach der Änderung muss das Jenkins-Dogu neu gestartet werden, damit die neuen Java-Parameter übernommen werden.

