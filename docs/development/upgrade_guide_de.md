# Basis-Image aktualisieren

Wenn das Basis-Image aktualisiert werden muss und sich die Java-Version ändert, benötigt Jenkins immer noch die vorherige openjdk-Version
um Legacy-Builds zu unterstützen. Laden Sie die alte openjdk-Version mit `apk add openjdk<major version>=<version>` in
der Dockerdatei herunter und erweitern Sie die Version im openjdk-Installationsgroovy-Skript `init100NORMjdkautoinstall.groovy`.