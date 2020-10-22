<img src="https://cloudogu.com/images/dogus/jenkins.png" alt="jenkins logo" height="100px">


[![GitHub license](https://img.shields.io/github/license/cloudogu/jenkins.svg)](https://github.com/cloudogu/jenkins/blob/master/LICENSE)
[![GitHub release](https://img.shields.io/github/release/cloudogu/jenkins.svg)](https://github.com/cloudogu/jenkins/releases)

# Jenkins Dogu

## About this Dogu

**Name:** official/jenkins

**Description:** [Jenkins](https://en.wikipedia.org/wiki/Jenkins_(software)) is an open source automation server written in Java. Jenkins helps to automate the non-human part of the software development process with continuous integration and facilitating technical aspects of continuous delivery.

**Website:** https://jenkins-ci.org

**Dependencies:** cas, nginx, postfix

## Installation Ecosystem
```
cesapp install official/jenkins

cesapp start jenkins
```

## Building with OpenJDK 8

Since Jenkins Dogu version 2.222.1-2 the default OpenJDK version is 11. If you want to keep using OpenJDK 8 for your builds, use the following steps.

### Project builds

In standard Java build projects you can easily change the JDK via the project's configuration in Jenkins; just choose "OpenJDK-8" in the "JDK" category.

### Pipeline builds

In Pipeline builds you have to adapt your pipeline script to use OpenJDK 8.

#### Declarative syntax

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

#### Scripted syntax

In pipelines with scripted syntax, you can use the `tool` key word to use the correct jdk, for example:

```
stage("Get java version")
  def java_home = tool 'OpenJDK-8'
  steps{
    sh "'${java_home}/bin/java' -version"
  }
```

#### Building with Docker

You also have the option to use Docker for your builds, for example:

```
agent {
  docker { image 'openjdk:8-jdk' }
}
steps {
  sh 'java -version'
}
```

### Maven-Builds

To use OpenJDK 8 in your maven builds, initialize it the following way:

```
def javaHome = tool 'OpenJDK-8'
Maven mvn = new MavenWrapper(this, javaHome)
```

#### Maven in Docker

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

## Post-initialization scripts
At the end of the Jenkins startup process, all scripts located inside /var/tmp/resources/init.groovy.d are executed.
You can add your own scripts by saving them inside the `custom.init.groovy.d` volume.

### System critical scripts
To mark a script as system **critical** add the **prefix** **CRIT** after the script number. A critical script can be defined if an error leads to a jenkins instance that e.g. can not be reached or can not be logged in to.

**Example:** `init030CRITinstallplugins.groovy`

### Non-critical scripts (Normal)
To mark a script as **non-critical** add the **prefix** **NORM** after the script number. Modifications which only apply to additional features of Jenkins are usually non-critical (NORM) e.g. configuring a JDK or Maven.

**Example:** `init100NORMmavenautoinstall.groovy`
