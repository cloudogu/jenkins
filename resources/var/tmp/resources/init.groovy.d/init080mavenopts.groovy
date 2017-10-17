import jenkins.model.*;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;

def jenkins = Jenkins.getInstance();

def opts = "-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true "
// Additional truststore options are set in .mavenrc file

def found = false;
def globalNodeProperties = jenkins.getGlobalNodeProperties();
for ( def prop : globalNodeProperties){
  if (prop instanceof EnvironmentVariablesNodeProperty){
    def env = prop.getEnvVars();
    env.put("MAVEN_OPTS", opts);
    found = true;
  }
}

if (!found){
  def envProp = new EnvironmentVariablesNodeProperty(new Entry("MAVEN_OPTS", opts));
  globalNodeProperties.add(envProp);
}

jenkins.save();
