import jenkins.model.*
import hudson.model.*
import hudson.tools.*

def inst = Jenkins.getInstance();
def desc = inst.getDescriptor("hudson.model.JDK");
def installedJDKs = JDK[];
installedJDKs = desc.getInstallations();
print "init100jdkautoinstall: start jdk installation\n"

def JDK_8_NAME = "OpenJDK-8";
// add more jdk-entries to the map to install multiple jdks by default
def requestedJDKVersions = [ (JDK_8_NAME): "java-1.8-openjdk"];

// the installations consists of all already installed jdks and all jdks definied in requestedJDKVersions
def installations = [];

for (jdk in requestedJDKVersions) {
    def installation = new JDK(jdk.key, "/usr/lib/jvm/${jdk.value}")
    installations.push(installation)
}

for (jdk in installedJDKs){
    print("init100jdkautoinstall: found installation ${jdk.getName()}\n")
    // do not add requestedJDKVersions
    if (jdk.getName().equals(JDK_8_NAME)){
        print("init100jdkautoinstall: JDK ${JDK_8_NAME} already installed\n")
    }else{
        installations.push(jdk)
    }
}

// add all installations and save them
desc.setInstallations(installations.toArray(new JDK[0]))
desc.save()
print("init100jdkautoinstall: Installation completed successfully\n")
