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

    def crt = new File("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt").text

    def kubernetesCloud = new KubernetesCloud(
            "kubernetes"
    )

    kubernetesCloud.setNamespace("ecosystem")
    kubernetesCloud.setServerUrl("kubernetes.default.svc.cluster.local")
    kubernetesCloud.setPodLabels([new PodLabel("cloudogu.com/pod-kind", "jenkins-build")])
    kubernetesCloud.setServerCertificate(crt)

    jenkins.clouds.add(kubernetesCloud)
    jenkins.save()
}
