---
title: "Building with custom OpenJDK versions"
---

# Building with custom OpenJDK versions

Since Jenkins Dogu version 2.361.1, the default OpenJDK version is 11 or 17. If you still want to use older OpenJDK version e.g. 11 for your builds do the following.

## Project builds

For standard Java build projects, you can easily change the JDK via the project configuration in Jenkins. To do this, simply select "OpenJDK-11" in the "JDK" category.
## Pipeline builds

Pipeline builds require you to customize your pipeline script to use OpenJDK 11.

### Declarative syntax

In pipelines with declarative syntax, you can add the `tools` section to use the correct JDK.
For example:

```
stage("Get Java version"){
  tools {
    jdk "OpenJDK-11"
  }
  steps{
    sh 'java -version'
  }
}
```

### Scripted syntax

In pipelines with scripted syntax, you can use the keyword `tool` to use the correct JDK.
For example:

```
stage("Get Java version")
  def java_home = tool 'OpenJDK-11'
  steps{
    sh "'${java_home}/bin/java' -version"
  }
```

### Building with Docker

You also have the option to use Docker for your builds, for example:

```
agent {
  docker { image 'openjdk:11-jdk' }
}
steps {
  sh 'java -version'
}
```

## Maven builds

To use OpenJDK 11 in your Maven builds, initialize it in the following way:

```
def javaHome = tool 'OpenJDK-11'
Maven mvn = new MavenWrapper(this, javaHome)
```

### Maven in Docker

Using the [ces-build-lib](https://github.com/cloudogu/ces-build-lib), you can easily build your Maven project in Docker:

```
Maven mvn = new MavenInDocker(this, "3.5.4-jdk-11")
mvn ...
```

or

```
Maven mvn = new MavenWrapper(this)
new Docker(this).image('openjdk:11-jdk').mountJenkinsUser().inside{
  mvn ...
}
```
