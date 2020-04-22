import jenkins.model.*
import hudson.model.*

def jenkinsJDKName = "OpenJDK-8"
def jdkVersion = "java-1.8-openjdk"
def inst = Jenkins.getInstance()

def desc = inst.getDescriptor("hudson.model.JDK")

JDK jdk = new JDK(jenkinsJDKName, "/usr/lib/jvm/$jdkVersion")

desc.setInstallations(jdk)

desc.save();
inst.save()
println("$jdkVersion successfully installed.")
