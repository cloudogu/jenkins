package scripts

import jenkins.model.Jenkins
import hudson.security.csrf.DefaultCrumbIssuer

def getDoguctlWrapper() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    doguctlWrapper = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return doguctlWrapper
}

doguctl = getDoguctlWrapper()

final CONFIGURED_KEY = "configured"
boolean isConfigured = doguctl.getDoguConfig(CONFIGURED_KEY).toBoolean()

if (!isConfigured) {
    def jenkins = Jenkins.getInstance()
    // the constructor parameter defines if proxy compatibility should be enabled
    jenkins.setCrumbIssuer(new DefaultCrumbIssuer(false))
    jenkins.save()
}