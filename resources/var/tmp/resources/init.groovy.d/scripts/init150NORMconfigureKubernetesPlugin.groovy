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
    def cloudName = "kubernetes"

    def kubernetesCloud = jenkins.clouds.getByName(cloudName)

    if (kubernetesCloud == null) {
        kubernetesCloud = new KubernetesCloud(
                cloudName
        )
    }

    def crt = new File("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt").text

    kubernetesCloud.setNamespace("ecosystem")
    kubernetesCloud.setServerUrl("kubernetes.default.svc.cluster.local")
    kubernetesCloud.setServerCertificate(crt)
    kubernetesCloud.setPodLabels([new PodLabel("cloudogu.com/pod-kind", "jenkins-build")])
    kubernetesCloud.setJenkinsUrl("http://jenkins.ecosystem.svc.cluster.local:8080/jenkins")

    jenkins.clouds.add(kubernetesCloud)
    jenkins.save()
}
