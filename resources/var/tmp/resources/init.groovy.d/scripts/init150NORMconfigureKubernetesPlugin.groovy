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

if (doguctl.isMultinode()) {
    def kubernetesCloud = new KubernetesCloud("kubernetes")

    kubernetesCloud.setServerUrl("kubernetes.default.svc.cluster.local")
    def crt = new File("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt").text
    kubernetesCloud.setServerCertificate(crt)
    kubernetesCloud.setNamespace("ecosystem")
    kubernetesCloud.setPodLabels([new PodLabel("cloudogu.com/pod-kind", "jenkins-build")])
    kubernetesCloud.setJenkinsUrl("http://jenkins.ecosystem.svc.cluster.local:8080/jenkins")
    kubernetesCloud.setWebSocket(true)

    jenkins.clouds.replace(kubernetesCloud)
    jenkins.save()
}
