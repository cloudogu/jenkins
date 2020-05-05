// creates a global tool installer for maven.

import jenkins.model.*
import hudson.*
import hudson.model.*
import hudson.tasks.*
import hudson.tools.*

// the name M3 is chosen, because of the GitHub + Maven Pipeline sample
def mavenName = "M3"
def targetVersion = "3.6.3"

Collection<String> installedM3Versions() {
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

def m3Versions = installedM3Versions()
if (!m3Versions.contains(targetVersion)) {
    addMavenToInstallations(
            createMavenInstallation(mavenName, targetVersion)
    )
}
