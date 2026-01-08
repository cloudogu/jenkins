package scripts

import jenkins.model.*
import org.csanchez.jenkins.plugins.kubernetes.*

def jenkins = Jenkins.instance

def getDoguctlWrapper() {
    File sourceFile = new File("/var/lib/jenkins/init.groovy.d/lib/Doguctl.groovy")
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
    doguctlWrapper = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance()
    return doguctlWrapper
}

doguctl = getDoguctlWrapper()
switch (doguctl.getDoguConfig("tcp_inbound_agent_port")){
    case "static":
        jenkins.setSlaveAgentPort(50000)
        break
    case "dynamic":
        jenkins.setSlaveAgentPort(0)
        break
    default:
        // disabled
        jenkins.setSlaveAgentPort(-1)
}