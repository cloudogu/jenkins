---
title: Building with OpenJDK 8
---

# Building with OpenJDK 8

Since Jenkins Dogu version 2.222.1-2 the default OpenJDK version is 11. If you want to keep using OpenJDK 8 for your
builds, use the following steps.

## Project builds

In standard Java build projects you can easily change the JDK via the project's configuration in Jenkins; just choose "
OpenJDK-8" in the "JDK" category.

## Pipeline builds

In Pipeline builds you have to adapt your pipeline script to use OpenJDK 8.

### Declarative syntax

In pipelines with declarative syntax, you can add the `tools` section to use the correct jdk, for example:

```
stage("Get java version"){
  tools {
    jdk "OpenJDK-8"
  }
  steps{
    sh 'java -version'
  }
}
```

### Scripted syntax

In pipelines with scripted syntax, you can use the `tool` key word to use the correct jdk, for example:

```
stage("Get java version")
  def java_home = tool 'OpenJDK-8'
  steps{
    sh "'${java_home}/bin/java' -version"
  }
```

### Building with Docker

You also have the option to use Docker for your builds, for example:

```
agent {
  docker { image 'openjdk:8-jdk' }
}
steps {
  sh 'java -version'
}
```

## Maven-Builds

To use OpenJDK 8 in your maven builds, initialize it the following way:

```
def javaHome = tool 'OpenJDK-8'
Maven mvn = new MavenWrapper(this, javaHome)
```

### Maven in Docker

Using the [ces-build-lib](https://github.com/cloudogu/ces-build-lib), you can easily build your maven project in Docker:

```
Maven mvn = new MavenInDocker(this, "3.5.0-jdk-8")
mvn ...
```

or

```
Maven mvn = new MavenWrapper(this)
new Docker(this).image('openjdk:8-jdk').mountJenkinsUser().inside{
  mvn ...
}
```