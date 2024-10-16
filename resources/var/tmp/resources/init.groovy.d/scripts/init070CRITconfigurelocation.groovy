package scripts

import jenkins.model.*

// https://github.com/r-hub/rhub-jenkins/blob/master/docker-entrypoint.sh#L117

File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
ecoSystem = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()

def isInvalidEmail(email) {
	def emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/
	return !(email =~ emailRegex)
}

def instance = Jenkins.getInstance()

// configure jenkins location
String emailAddress
String configuredMailAddress
try {
	configuredMailAddress = ecoSystem.getGlobalConfig("mail_address")
} catch (FileNotFoundException ex) {
  println "could not find mail_address configuration in registry"
}
if (configuredMailAddress != null && configuredMailAddress.length() > 0) {
	emailAddress = configuredMailAddress
} else {
	emailAddress = "jenkins@" + ecoSystem.getGlobalConfig("domain")
}
String fqdn = ecoSystem.getGlobalConfig("fqdn")

def location = JenkinsLocationConfiguration.get()

if (isInvalidEmail(location.getAdminAddress())) {
	location.setAdminAddress(emailAddress)
}

location.setUrl("https://${fqdn}/jenkins")
location.save()

instance.save()