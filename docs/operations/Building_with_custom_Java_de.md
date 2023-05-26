---
title: "Bauen mit speziellen OpenJDK-Versionen"
---

# Bauen mit speziellen OpenJDK-Versionen

Seit Jenkins Dogu Version 2.361.1 ist die Standard-OpenJDK-Version 11 oder 17. Wenn Sie weiterhin ältere OpenJDK z.B. 8 oder 11 für Ihre Builds
verwenden möchten, gehen Sie wie folgt vor.

## Projekt-Builds

In Standard-Java-Build-Projekten können Sie das JDK einfach über die Projektkonfiguration in Jenkins ändern; wählen Sie
einfach "OpenJDK-8" oder "OpenJDK-11" in der Kategorie "JDK".

## Pipeline-Builds

Bei Pipeline-Builds müssen Sie Ihr Pipeline-Skript anpassen, um OpenJDK 8 oder 11 zu verwenden.

### Deklarative Syntax

In Pipelines mit deklarativer Syntax können Sie den Abschnitt `tools` hinzufügen, um das richtige JDK zu verwenden, zum
Beispiel

```
stage("Java-Version abrufen"){
  tools {
    jdk "OpenJDK-8"
  }
  steps{
    sh 'java -version'
  }
}
```

### Scripted syntax

In Pipelines mit skriptgesteuerter Syntax können Sie das Schlüsselwort `tool` verwenden, um das richtige JDK zu
verwenden, zum Beispiel:

```
stage("Java-Version holen")
  def java_home = tool 'OpenJDK-8'
  steps{
    sh "'${java_home}/bin/java' -version"
  }
```

### Bauen mit Docker

Sie haben auch die Möglichkeit, Docker für Ihre Builds zu verwenden, zum Beispiel:

```
agent {
  docker { image 'openjdk:8-jdk' }
}
steps {
  sh 'java -version'
}
```

## Maven-Builds

Um OpenJDK 8 in Ihren Maven-Builds zu verwenden, initialisieren Sie es auf folgende Weise:

```
def javaHome = tool 'OpenJDK-8'
Maven mvn = new MavenWrapper(this, javaHome)
```

### Maven in Docker

Unter Verwendung der [ces-build-lib](https://github.com/cloudogu/ces-build-lib) können Sie Ihr Maven-Projekt einfach in
Docker bauen:

```
Maven mvn = new MavenInDocker(this, "3.5.0-jdk-8")
mvn ...
```

oder

```
Maven mvn = new MavenWrapper(this)
new Docker(this).image('openjdk:8-jdk').mountJenkinsUser().inside{
  mvn ...
}
```