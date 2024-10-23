package scripts

import jenkins.model.*
import hudson.model.*
import hudson.tools.*

def inst = Jenkins.getInstance()
def desc = inst.getDescriptor('hudson.model.JDK')
def installedJDKs = JDK[]
installedJDKs = desc.getInstallations()
print 'Starting JDK configuration\n'

String JDK_8_NAME = 'OpenJDK-8'
String JDK_11_NAME = 'OpenJDK-11'
String JDK_17_NAME = 'OpenJDK-17'
// add more jdk-entries to the map to install multiple jdks by default
def requestedJDKVersions = [ (JDK_17_NAME): 'java-17-openjdk', (JDK_11_NAME): 'java-11-openjdk']

// the installations consists of all already installed jdks and all jdks defined in requestedJDKVersions
def installations = []

for (jdk in requestedJDKVersions) {
    def installation = new JDK(jdk.key, "/usr/lib/jvm/${jdk.value}")
    print("Implementing/Keeping JDK configuration ${jdk.key}\n")
    installations.push(installation)
}

for (jdk in installedJDKs) {
    print("Installed JDK configuration ${jdk.getName()}\n")
    // do not add requestedJDKVersions
    if (!(jdk.getName().equals(JDK_11_NAME)) && !(jdk.getName().equals(JDK_8_NAME))) {
        print("Keeping JDK configuration ${jdk.getName()}\n")
        installations.push(jdk)
    }
}

// add all installations and save them
desc.setInstallations(installations.toArray(new JDK[0]))
desc.save()
print('Configuration completed successfully\n')