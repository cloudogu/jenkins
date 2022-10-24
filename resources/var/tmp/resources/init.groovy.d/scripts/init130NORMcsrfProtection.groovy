import groovy.json.JsonSlurper
import jenkins.model.Jenkins
import hudson.security.csrf.DefaultCrumbIssuer

def getValueFromEtcd(String key){
  String ip = new File("/etc/ces/node_master").getText("UTF-8").trim();
  URL url = new URL("http://${ip}:4001/v2/keys/${key}");
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

final ETCD_CONFIGURED_KEY = "config/jenkins/configured"
boolean isConfigured = getJenkinsConfigurationState(ETCD_CONFIGURED_KEY).toBoolean()

if (!isConfigured) {
    def jenkins = Jenkins.getInstance()
    // the constructor parameter defines if proxy compatibility should be enabled
    jenkins.setCrumbIssuer(new DefaultCrumbIssuer(false))
    jenkins.save()
}