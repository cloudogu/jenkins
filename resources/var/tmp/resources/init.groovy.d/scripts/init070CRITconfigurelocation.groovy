import jenkins.model.*;
import groovy.json.JsonSlurper;

// https://github.com/r-hub/rhub-jenkins/blob/master/docker-entrypoint.sh#L117

def getValueFromEtcd(String key){
	String ip = new File("/etc/ces/node_master").getText("UTF-8").trim();
	URL url = new URL("http://${ip}:4001/v2/keys/${key}");
	def json = new JsonSlurper().parseText(url.text)
	return json.node.value
}

def isInvalidEmail(email) {
	def emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/
	return !(email =~ emailRegex)
}

def instance = Jenkins.getInstance();

// configure jenkins location
String emailAddress;
String configuredMailAddress;
try {
	configuredMailAddress = getValueFromEtcd("config/_global/mail_address");
} catch (FileNotFoundException ex) {
  println "could not find mail_address configuration in registry"
}
if (configuredMailAddress != null && configuredMailAddress.length() > 0) {
	emailAddress = configuredMailAddress;
} else {
	emailAddress = "jenkins@" + getValueFromEtcd("config/_global/domain");
}
String fqdn = getValueFromEtcd("config/_global/fqdn");

def location = JenkinsLocationConfiguration.get()

if (isInvalidEmail(location.getAdminAddress())) {
	location.setAdminAddress(emailAddress);
}

location.setUrl("https://${fqdn}/jenkins");
location.save()

instance.save();