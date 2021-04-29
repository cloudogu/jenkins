---
title: "Post-Initialisierungs-Skripte"
---

# Post-Initialisierungs-Skripte

Am Ende des Jenkins-Startvorgangs werden alle Skripte, die sich in /var/tmp/resources/init.groovy.d befinden,
ausgeführt. Sie können Ihre eigenen Skripte hinzufügen, indem Sie sie im Volume `custom.init.groovy.d` speichern.

## Systemkritische Skripte

Um ein Skript als **systemkritisch** zu kennzeichnen, fügen Sie das **Präfix** **CRIT** hinter der Skriptnummer hinzu.
Ein kritisches Skript kann definiert werden, wenn ein Fehler dazu führt, dass eine Jenkins-Instanz z. B. nicht erreicht
werden kann oder nicht angemeldet werden kann.

**Beispiel:** `init030CRITinstallplugins.groovy`

## Unkritische Skripte (Normal)

Um ein Skript als **unkritisch** zu markieren, fügen Sie das **Präfix** **NORM** nach der Skriptnummer hinzu.
Änderungen, die sich nur auf zusätzliche Funktionen von Jenkins beziehen, sind in der Regel unkritisch (NORM), z. B. das
Konfigurieren eines JDK oder von Maven.

Wenn Skripte weder als **CRIT** noch als **NORM** markiert sind, werden sie als unkritisch behandelt.

**Beispiel:** `init100NORMavenautoinstall.groovy`
