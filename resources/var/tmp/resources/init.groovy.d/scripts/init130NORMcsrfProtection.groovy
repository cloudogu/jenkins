import groovy.json.JsonSlurper
import jenkins.model.Jenkins
import hudson.security.csrf.DefaultCrumbIssuer

File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/EcoSystem.groovy")
Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
ecoSystem = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()

final CONFIGURED_KEY = "configured"
boolean isConfigured = ecoSystem.getDoguConfig(CONFIGURED_KEY).toBoolean()

if (!isConfigured) {
    def jenkins = Jenkins.getInstance()
    // the constructor parameter defines if proxy compatibility should be enabled
    jenkins.setCrumbIssuer(new DefaultCrumbIssuer(false))
    jenkins.save()
}