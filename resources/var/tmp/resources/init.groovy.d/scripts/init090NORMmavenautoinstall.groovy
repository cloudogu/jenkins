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
def targetVersion = "3.6.3"

List<hudson.tasks.Maven$MavenInstallation> getInstallationsWithName(String name, List<hudson.tasks.Maven$MavenInstallation> installations){
    List<hudson.tasks.Maven$MavenInstallation> installationsWithName = []
        installations?.each { installation ->
        if (installation.toString().contains(name)) {
            installationsWithName.add(installation)
        }
    }
    return installationsWithName
}

Collection<String> getInstalledMavenVersionsWithName(def mavenName) {
    def versions = []

    def installations = Jenkins.instance.getDescriptor("hudson.tasks.Maven").getInstallations()

    def m3Installations = getInstallationsWithName(mavenName, installations as List)
    m3Installations?.each { installation ->
        installation.getProperties().each { property ->
            property.installers.each { installer ->
                if (installer instanceof hudson.tasks.Maven$MavenInstaller){
                    versions.add(installer.id)
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
    boolean noOtherInstallerDetected = true
    property.installers.each { installer ->
        if (installer instanceof hudson.tasks.Maven$MavenInstaller){
            numberOfMavenInstallers++
        } else {
            noOtherInstallerDetected = false
        }
    }
    return noOtherInstallerDetected && numberOfMavenInstallers == 1
}

// Gets Maven version as String if only one Maven installer is defined in property
String getSingleMavenInstallerVersion(def property){
    return property.installers[0].id
}

List getNonTargetM3Installations(def mavenName, def targetVersion) {
    def mavenInstallations = Jenkins.instance.getExtensionList(hudson.tasks.Maven.DescriptorImpl.class)[0]
    def mavenInstallationsList = (mavenInstallations.installations as List)
    def toRemove = []

    // iterate over every maven installation with $mavenName name
    def installationsWithCorrectName = getInstallationsWithName(mavenName, mavenInstallationsList)
    installationsWithCorrectName.each { installation ->
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
        if (!versions.isEmpty() && !versions.contains(targetVersion)) {
            toRemove.add(installation)
        }
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

int getAmountOfInstallationsWithCorrectName(String name){
    def mavenInstallations = Jenkins.instance.getExtensionList(hudson.tasks.Maven.DescriptorImpl.class)[0]
    def mavenInstallationsList = (mavenInstallations.installations as List)
    def installationsWithCorrectName = getInstallationsWithName(name, mavenInstallationsList)
    return installationsWithCorrectName.size()
}


//// function definitions ready, work is done now:

int amountOfMavenInstallationsWithCorrectName = getAmountOfInstallationsWithCorrectName(mavenName)
switch (amountOfMavenInstallationsWithCorrectName) {
    case 0:
        println "Adding a new Maven installation with name $mavenName"
        addMavenToInstallations(
            createMavenInstallation(mavenName, targetVersion)
        )
        break;
    case 1:
        def nonTargetMavenInstallations = getNonTargetM3Installations(mavenName, targetVersion)
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
