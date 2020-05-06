// creates a global tool installer for maven.

// based on https://wiki.jenkins.io/display/JENKINS/Add+a+Maven+Installation%2C+Tool+Installation%2C+Modify+System+Config
// more info https://github.com/glenjamin/jenkins-groovy-examples/blob/master/README.md

import jenkins.model.*
import hudson.*
import hudson.model.*
import hudson.tasks.*
import hudson.tools.*

// the name M3 is chosen, because of the GitHub + Maven Pipeline sample
def mavenName = "M3"
def targetVersion = "3.6.3"

Collection<String> installedM3Versions(def mavenName) {
    def versions = []

    Jenkins.instance.getDescriptor("hudson.tasks.Maven").getInstallations().each { i ->
        if (i.toString().contains(mavenName)) {
            i.getProperties().each { p ->
                p.installers.each { inst ->
                    versions.add(inst.id)
                }
            }
        }
    }

    return versions
}

static def createMavenInstallation(def mavenName, def mavenVersion) {
    def mvnInstaller = new Maven.MavenInstaller(mavenVersion)
    def instSourcProp = new InstallSourceProperty([mvnInstaller])
    return new Maven.MavenInstallation(mavenName, null, [instSourcProp])
}

def addMavenToInstallations(def installation) {
    mavenInstallations = Jenkins.instance.getExtensionList(hudson.tasks.Maven.DescriptorImpl.class)[0]
    mavenInstallationsList = (mavenInstallations.installations as List)
    mavenInstallationsList.add(installation)
    mavenInstallations.installations = mavenInstallationsList
    mavenInstallations.save()
    Jenkins.instance.save()
}

def removeNonTargetM3Installations(def mavenName, def targetVersion) {
    def mavenInstallations = Jenkins.instance.getExtensionList(hudson.tasks.Maven.DescriptorImpl.class)[0]
    def mavenInstallationsList = (mavenInstallations.installations as List)

    def toRemove = []

    // iterate over every maven installation
    mavenInstallationsList.each { i ->
        // check only for installations named $mavenName
        if (i.toString().contains(mavenName)) {
            def versions = []

            // find version ids of the installation
            i.getProperties().each { p ->
                p.installers.each { inst ->
                    versions.add(inst.id)
                }
            }

            // remember it as to be removed
            if (!versions.contains(targetVersion)) {
                toRemove.add(i)
            }
        }
    }

    // remove all remembered installations
    toRemove.each { tr -> mavenInstallationsList.remove(tr) }

    mavenInstallations.installations = mavenInstallationsList
    mavenInstallations.save()
    Jenkins.instance.save()
}

removeNonTargetM3Installations(mavenName, targetVersion)

if (!installedM3Versions(mavenName).contains(targetVersion)) {
    addMavenToInstallations(
            createMavenInstallation(mavenName, targetVersion)
    )
}
