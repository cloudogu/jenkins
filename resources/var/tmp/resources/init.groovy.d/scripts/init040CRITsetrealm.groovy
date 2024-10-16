package scripts

import hudson.model.*
import jenkins.model.*
import org.jenkinsci.plugins.cas.CasSecurityRealm
import org.jenkinsci.plugins.cas.protocols.Cas30Protocol

File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
ecoSystem = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()

String fqdn = ecoSystem.getGlobalConfig("fqdn")
def protocol = new Cas30Protocol("groups,roles", "cn", "mail", true, false, "^https://${fqdn}/.*\$")
def realm = new CasSecurityRealm("https://${fqdn}/cas", protocol, false, true, true)

def instance = Jenkins.getInstance()
instance.setSecurityRealm(realm)
instance.setDisableRememberMe(true)
instance.save()
