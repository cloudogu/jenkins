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

Collection<String> getInstalledMavenVersionsWithName(def mavenName) {
    def versions = []

    def installations = Jenkins.instance.getDescriptor("hudson.tasks.Maven").getInstallations()

    installations?.each { installation ->
        if (installation.toString().contains(mavenName)) {
            installation.getProperties().each { property ->
                property.installers.each { installer ->
                    if (installer instanceof hudson.tasks.Maven$MavenInstaller){
                        versions.add(installer.id)
                    }
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

void addMavenToInstallations(def installation) {
    mavenInstallations = Jenkins.instance.getExtensionList(hudson.tasks.Maven.DescriptorImpl.class)[0]
    mavenInstallationsList = (mavenInstallations.installations as List)
    mavenInstallationsList.add(installation)
    mavenInstallations.installations = mavenInstallationsList
    mavenInstallations.save()
    Jenkins.instance.save()
}

// Checks if property has only one hudson.tasks.Maven$MavenInstaller installer and no other installers.
// Other possible installers are hudson.tools.ZipExtractionInstaller,
// hudson.tools.CommandInstaller or hudson.tools.BatchCommandInstaller.
boolean propertyHasOneMavenInstallerOnly(def property) {
    int numberOfMavenInstallers = 0
    property.installers.each { installer ->
        if (installer instanceof hudson.tasks.Maven$MavenInstaller){
            numberOfMavenInstallers++
        } else {
            return false
        }
    }
    return numberOfMavenInstallers == 1
}

// Gets Maven version as String if only one Maven installer is defined in property
String getSingleMavenInstallerVersion(def property){
    return property.installers[0].id
}

void removeNonTargetM3Installations(def mavenName, def targetVersion) {
    def mavenInstallations = Jenkins.instance.getExtensionList(hudson.tasks.Maven.DescriptorImpl.class)[0]
    def mavenInstallationsList = (mavenInstallations.installations as List)

    def toRemove = []

    // iterate over every maven installation
    mavenInstallationsList.each { installation ->
        // check only for installations named $mavenName
        if (installation.toString().contains(mavenName)) {
            def versions = []

            // find version ids of the installation, if any
            installation.getProperties().each { property ->
                // Only get id if there is only one maven installer.
                // More than one installer indicates settings modification by the user,
                // which we do not want to remove
                if (propertyHasOneMavenInstallerOnly(property)) {
                    versions.add(getSingleMavenInstallerVersion(property))
                }
            }

            // remember it as to be removed
            if (!versions.contains(targetVersion)) {
                toRemove.add(installation)
            }
        }
    }

    // remove all remembered installations
    toRemove.each { installation -> mavenInstallationsList.remove(installation) }

    mavenInstallations.installations = mavenInstallationsList
    mavenInstallations.save()
    Jenkins.instance.save()
}


//// function definitions ready, work is done now:

removeNonTargetM3Installations(mavenName, targetVersion)

if (!getInstalledMavenVersionsWithName(mavenName).contains(targetVersion)) {
    addMavenToInstallations(
            createMavenInstallation(mavenName, targetVersion)
    )
}
