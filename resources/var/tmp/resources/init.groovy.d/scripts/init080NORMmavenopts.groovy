import groovy.json.JsonSlurper
import jenkins.model.*;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;

def jenkins = Jenkins.getInstance();

File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/EcoSystem.groovy")
Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
ecoSystem = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()

// Additional truststore options are set in .mavenrc file
def opts = "-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true "
def found = false

final ETCD_CONFIGURED_KEY = "configured"
boolean isConfigured = ecoSystem.getDoguConfig(ETCD_CONFIGURED_KEY).toBoolean()


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
