package scripts

import jenkins.model.*

// https://github.com/r-hub/rhub-jenkins/blob/master/docker-entrypoint.sh#L117


def getDoguctlWrapper() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    doguctlWrapper = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return doguctlWrapper
}

doguctl = getDoguctlWrapper()

def isInvalidEmail(email) {
	def emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/
	return !(email =~ emailRegex)
}

def instance = Jenkins.getInstance()

// configure jenkins location
String emailAddress
String configuredMailAddress
try {
	configuredMailAddress = doguctl.getGlobalConfig("mail_address")
} catch (FileNotFoundException ex) {
  println "could not find mail_address configuration in registry"
}
if (configuredMailAddress != null && configuredMailAddress.length() > 0) {
	emailAddress = configuredMailAddress
} else {
	emailAddress = "jenkins@" + doguctl.getGlobalConfig("domain")
}
String fqdn = doguctl.getGlobalConfig("fqdn")

def location = JenkinsLocationConfiguration.get()

if (isInvalidEmail(location.getAdminAddress())) {
	location.setAdminAddress(emailAddress)
}

location.setUrl("https://${fqdn}/jenkins")
location.save()

instance.save()