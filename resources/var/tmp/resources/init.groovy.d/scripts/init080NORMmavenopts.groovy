import groovy.json.JsonSlurper
import jenkins.model.*;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;

def jenkins = Jenkins.getInstance();

def getValueFromEtcd(String key){
  String ip = new File("/etc/ces/node_master").getText("UTF-8").trim();
  URL url = new URL("http://[${ip}]:4001/v2/keys/${key}");
  def json = new JsonSlurper().parseText(url.text)
  return json.node.value
}

def getJenkinsConfigurationState(String key){
  try {
    return getValueFromEtcd(key).toString()
  } catch (FileNotFoundException ex) {
    return "false"
  }
}
// Additional truststore options are set in .mavenrc file
def opts = "-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true "
def found = false

final ETCD_CONFIGURED_KEY = "config/jenkins/configured"
boolean isConfigured = getJenkinsConfigurationState(ETCD_CONFIGURED_KEY).toBoolean()


if (!isConfigured) {
    def globalNodeProperties = jenkins.getGlobalNodeProperties()
    for (def prop : globalNodeProperties) {
        if (prop instanceof EnvironmentVariablesNodeProperty) {
            def env = prop.getEnvVars()
            env.put("MAVEN_OPTS", opts)
            found = true
        }
    }
    if (!found) {
        def envProp = new EnvironmentVariablesNodeProperty(new Entry("MAVEN_OPTS", opts));
        globalNodeProperties.add(envProp);
    }
}


jenkins.save();
