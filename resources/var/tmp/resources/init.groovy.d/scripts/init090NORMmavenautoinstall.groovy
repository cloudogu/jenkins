// creates a global tool installer for maven.

// This script creates a maven installer with the name defined below
// or updates it, if it has not been altered by a Jenkins administrator.
// Maven installations with other names are ignored

// based on https://wiki.jenkins.io/display/JENKINS/Add+a+Maven+Installation%2C+Tool+Installation%2C+Modify+System+Config
// more info https://github.com/glenjamin/jenkins-groovy-examples/blob/master/README.md

import jenkins.model.*
import hudson.*
import hudson.model.*
import hudson.tasks.*
import hudson.tools.*

// the name M3 is chosen, because of the GitHub + Maven Pipeline sample
def mavenName = "M3"
def targetVersion = "3.9.0"

List<hudson.tasks.Maven$MavenInstallation> getMavenInstallationsWithName(String name, List<hudson.tasks.Maven$MavenInstallation> installations){
    List<hudson.tasks.Maven$MavenInstallation> installationsWithName = []
        installations?.each { installation ->
        if (installation.toString() == "MavenInstallation[$name]") {
            installationsWithName.add(installation)
        }
    }
    return installationsWithName
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
    return (property.installers.size() == 1 && (property.installers[0] instanceof hudson.tasks.Maven$MavenInstaller))
}

// Gets Maven version as String if only one Maven installer is defined in property
String getSingleMavenInstallerVersion(def property){
    return property.installers[0].id
}

List getNonTargetM3Installations(def targetVersion, hudson.tasks.Maven$MavenInstallation mavenInstallationWithCorrectName) {
    def toRemove = []
    def versions = []
    // find version ids of the installation, if any
    mavenInstallationWithCorrectName.getProperties().each { property ->
        // Only get id if there is only one maven installer.
        // More than one installer indicates settings modification by the user,
        // which we do not want to remove
        if (propertyHasOneMavenInstallerOnly(property)) {
            versions.add(getSingleMavenInstallerVersion(property))
        }
    }
    // remember it as to be removed
    if (!versions.isEmpty() && !versions.contains(targetVersion)) {
        toRemove.add(mavenInstallationWithCorrectName)
    }
    return toRemove
}

void removeNonTargetM3Installations(List toRemove){
    def mavenInstallations = Jenkins.instance.getExtensionList(hudson.tasks.Maven.DescriptorImpl.class)[0]
    def mavenInstallationsList = (mavenInstallations.installations as List)
    // remove all remembered installations
    toRemove.each { installation -> mavenInstallationsList.remove(installation) }

    mavenInstallations.installations = mavenInstallationsList
    mavenInstallations.save()
    Jenkins.instance.save()
}

List<hudson.tasks.Maven$MavenInstallation> getInstallationsWithCorrectName(String name){
    def mavenInstallations = Jenkins.instance.getExtensionList(hudson.tasks.Maven.DescriptorImpl.class)[0]
    def mavenInstallationsList = (mavenInstallations.installations as List)
    def installationsWithCorrectName = getMavenInstallationsWithName(name, mavenInstallationsList)
    return installationsWithCorrectName
}


//// function definitions ready, work is done now:

List<hudson.tasks.Maven$MavenInstallation> mavenInstallationsWithCorrectName = getInstallationsWithCorrectName(mavenName)
switch (mavenInstallationsWithCorrectName.size()) {
    case 0:
        println "Adding a new Maven installation with name $mavenName"
        addMavenToInstallations(
            createMavenInstallation(mavenName, targetVersion)
        )
        break;
    case 1:
        def nonTargetMavenInstallations = getNonTargetM3Installations(targetVersion, mavenInstallationsWithCorrectName[0])
        if(!nonTargetMavenInstallations.isEmpty()){
            println "Removing old Maven installation with name $mavenName"
            removeNonTargetM3Installations(nonTargetMavenInstallations)
            println "Adding a new Maven installation with name $mavenName and version $targetVersion"
            addMavenToInstallations(
                    createMavenInstallation(mavenName, targetVersion)
            )
        }
        break;
    default:
        println "There is more than one installation with the name $mavenName present. Doing nothing."
        break;
}
